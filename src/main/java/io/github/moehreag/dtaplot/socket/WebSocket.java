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
import java.util.Timer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import io.github.moehreag.dtaplot.Discovery;
import io.github.moehreag.dtaplot.TextPaneUtil;
import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.socket.ws.Storage;
import lombok.Getter;
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

	private Timer timer;
	private java.net.http.WebSocket socket;

	private static final WebSocket INSTANCE = new WebSocket();

	private Consumer<Collection<Map<String, Value<?>>>> consumer;

	@Getter
	private static boolean connected;

	private WebSocket(){}

	public static void disconnect(){
		INSTANCE.close();
	}

	public static void read(Consumer<Collection<Map<String, Value<?>>>> consumer) {
		if (connected){
			LOGGER.warn("Websocket already connected!");
			return;
		}
		try {
			INSTANCE.consumer = consumer;
			INSTANCE.load();
		} catch (Exception e) {
			LOGGER.error("Error while reading websocket data", e);
		}
	}

	public void load() {
		InetSocketAddress a = Discovery.getHeatpump(null);
		socket = HttpClient.newHttpClient().newWebSocketBuilder().subprotocols("Lux_WS")
				.buildAsync(URI.create("ws://" + a.getHostString() + ":" + 8214), new WebSocketClient(this))
				.join();
		connected = true;
	}

	@RequiredArgsConstructor
	private static class WebSocketClient implements java.net.http.WebSocket.Listener {

		private final StringBuffer messageBuffer = new StringBuffer();
		private final WebSocket connection;

		@Override
		public void onOpen(java.net.http.WebSocket webSocket) {
			CompletableFuture.runAsync(() -> {
				LOGGER.info("Connection opened!");
				JDialog dialog = new JDialog((Window) null, tr("dialog.password"));

				JTextPane instructions = new JTextPane();
				TextPaneUtil.hideCaret(instructions);
				instructions.setEditable(false);
				instructions.setText(tr("dialog.password.instruction"));
				instructions.setFont(new JLabel().getFont());
				dialog.add(instructions, BorderLayout.NORTH);
				JPanel center = new JPanel();
				JTextField input = new JTextField();

				center.setLayout(new BorderLayout());
				center.add(Box.createRigidArea(new Dimension(20, 35)), BorderLayout.SOUTH);
				dialog.add(Box.createRigidArea(new Dimension(20, 10)), BorderLayout.WEST);
				dialog.add(Box.createRigidArea(new Dimension(20, 10)), BorderLayout.EAST);
				center.add(input);
				center.add(Box.createRigidArea(new Dimension(20, 35)), BorderLayout.NORTH);
				dialog.add(center);

				input.setMaximumSize(new Dimension(input.getWidth(), input.getFont().getSize() + 3));

				JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				JButton cancel = new JButton(new AbstractAction(tr("action.cancel")) {
					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
						connection.close();
					}
				});

				JButton done = new JButton(tr("action.select"));
				done.addActionListener(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {

						String pw = input.getText().isBlank() ? "0" : input.getText();

						LOGGER.info("Sending login..");
						connection.send("LOGIN;" + pw);
						LOGGER.info("Login sent!");
						connection.timer = new Timer();
						connection.timer.schedule(new TimerTask() {
							@Override
							public void run() {
								connection.send("REFRESH");
							}
						}, 10, 1000);


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
				input.requestFocus();
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
			LOGGER.info("Socket closed! " + statusCode + " " + reason);
			connection.close();
			return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
		}
	}

	private void send(String content) {
		socket.sendText(content, true);
		socket.request(1);
	}


	public void close() {
		LOGGER.info("Closing!");
		if (socket != null) {
			socket.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "ok");
			socket = null;
		}
		timer.cancel();
		timer.purge();
		connected = false;
	}

	public void parseResponse(CharSequence message) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource source = new InputSource(new StringReader(message.toString()));
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
					if (consumer != null) {
						consumer.accept(Set.of(Storage.getMerged()));
					}
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
					LOGGER.info(doc.getDocumentElement().getTagName());
					NodeList list = doc.getDocumentElement().getChildNodes();
					for (int i = 0; i < list.getLength(); i++) {
						Node node = list.item(i);
						String name = node.getNodeName();
						LOGGER.info(name);
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
