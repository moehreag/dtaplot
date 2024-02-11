package io.github.moehreag.dtaplot.socket;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.moehreag.dtaplot.DataLoader;
import io.github.moehreag.dtaplot.Discovery;
import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.socket.ws.Storage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WebSocket {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocket.class.getSimpleName());

	private final Timer timer = new Timer();
	private java.net.http.WebSocket socket;

	public static void main(String[] args) {
		try {
			new WebSocket((webSocket, m) -> {
				webSocket.close();
				DataLoader.getInstance().save(m, Path.of("wsparams.json"));
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final BiConsumer<WebSocket, Collection<Map<String, Value<?>>>> consumer;

	public WebSocket(BiConsumer<WebSocket, Collection<Map<String, Value<?>>>> consumer) {
		this.consumer = consumer;
	}

	public static void read(Consumer<Collection<Map<String, Value<?>>>> consumer) {
		try {
			WebSocket c = new WebSocket((webSocket, maps) -> consumer.accept(maps));
			c.load();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void load() {
		InetSocketAddress a = Discovery.getHeatpump(null);
		System.out.println(a);
		socket = HttpClient.newHttpClient().newWebSocketBuilder().subprotocols("Lux_WS")
				.buildAsync(URI.create("ws://" + a.getHostString() + ":" + 8214), new WebSocketClient(this))
				.join();
	}

	@RequiredArgsConstructor
	private static class WebSocketClient implements java.net.http.WebSocket.Listener {

		private final StringBuffer messageBuffer = new StringBuffer();
		private final WebSocket connection;

		@Override
		public void onOpen(java.net.http.WebSocket webSocket) {
			CompletableFuture.runAsync(() -> {
				System.out.println("Connection opened!");
				JDialog dialog = new JDialog((Window) null, "Enter Password");

				dialog.add(new JLabel("Enter the password for your heatpump below:"), BorderLayout.NORTH);
				JPanel center = new JPanel();
				JTextField input = new JTextField();

				center.setLayout(new BorderLayout());
				center.add(Box.createRigidArea(new Dimension(20, 40)), BorderLayout.SOUTH);
				dialog.add(Box.createRigidArea(new Dimension(20, 10)), BorderLayout.WEST);
				dialog.add(Box.createRigidArea(new Dimension(20, 10)), BorderLayout.EAST);
				center.add(input);
				center.add(Box.createRigidArea(new Dimension(20, 40)), BorderLayout.NORTH);
				dialog.add(center);

				input.setMaximumSize(new Dimension(input.getWidth(), input.getFont().getSize() + 3));

				JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				JButton cancel = new JButton(new AbstractAction(tr("action.cancel")) {
					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
					}
				});

				JButton done = new JButton(tr("action.select"));
				done.addActionListener(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {

						String pw = input.getText().isBlank() ? "0" : input.getText();

						System.out.println("Sending login..");
						connection.send("LOGIN;" + pw);
						System.out.println("Login sent!");
						connection.timer.schedule(new TimerTask() {
							@Override
							public void run() {
								connection.send("REFRESH");
							}
						}, 1000, 1000);


						dialog.setVisible(false);
						dialog.dispose();
					}
				});

				footer.add(done);
				footer.add(cancel);
				dialog.add(footer, BorderLayout.SOUTH);

				dialog.setSize(400, 200);
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			});
		}

		@Override
		public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
			messageBuffer.append(data);
			if (last) {
				connection.parseResponse(messageBuffer.toString());
				messageBuffer.delete(0, messageBuffer.length());
			}
			return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
		}

		@Override
		public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
			System.out.println("Socket closed! " + statusCode + " " + reason);
			connection.close();
			return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
		}
	}

	private void send(String content) {
		socket.sendText(content, true);
		socket.request(1);
	}


	public void close() {
		System.out.println("Closing!");
		if (socket != null) {
			socket.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "ok");
			socket = null;
		}
		timer.cancel();
	}

	public void parseResponse(CharSequence message) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource source = new InputSource();
			source.setCharacterStream(new StringReader(message.toString()));
			Document doc = builder.parse(source);

			String rootName = doc.getDocumentElement().getTagName();

			switch (rootName) {
				case "values":
					NodeList items = doc.getDocumentElement().getChildNodes();
					for (int i = 0; i < items.getLength(); i++) {
						Node node = items.item(i);
						String id = node.getAttributes().getNamedItem("id").getNodeValue();
						String value = node.getFirstChild().getTextContent();
						Storage.idValueMap.put(id, value);
					}
					consumer.accept(this, Set.of(Storage.getMerged()));
					break;
				case "Navigation":
					NodeList nodes = doc.getDocumentElement().getElementsByTagName("item");
					for (int i = 0; i < nodes.getLength(); i++) {
						Node node = nodes.item(i);
						if (node.getFirstChild().getTextContent().equals("Informationen")) {
							String id = node.getAttributes().getNamedItem("id").getNodeValue();
							send("GET;" + id);
							return;
						}
					}
					break;
				case "Content":
					NodeList entries = doc.getDocumentElement().getElementsByTagName("item");
					for (int i = 0; i < entries.getLength(); i++) {
						Node node = entries.item(i);
						String id = node.getAttributes().getNamedItem("id").getNodeValue();
						String name = node.getFirstChild().getTextContent();
						Storage.idNameMap.put(id, name);
					}
					break;
				default:
					/* unhandled case? */
					System.out.println(doc.getDocumentElement().getTagName());
					NodeList list = doc.getDocumentElement().getChildNodes();
					for (int i = 0; i < list.getLength(); i++) {
						Node node = list.item(i);
						String name = node.getNodeName();
						System.out.println(name);
					}
					break;

			}

		} catch (ParserConfigurationException | IOException | SAXException e) {
			LOGGER.error("Failed to parse XML", e);
		}
	}

	private static String tr(String key, Object... args) {
		return Translations.translate(key, args);
	}
}
