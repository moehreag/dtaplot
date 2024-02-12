package io.github.moehreag.dtaplot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The discovery of heatpumps is based on
 * <a href="https://github.com/Bouni/python-luxtronik/blob/main/luxtronik/discover.py">the python implementation</a>
 */
public class Discovery {

	private static final Logger LOGGER = LoggerFactory.getLogger(Discovery.class.getSimpleName());
	private static InetSocketAddress remembered;

	private static final Discovery INSTANCE = new Discovery();

	public static InetSocketAddress getHeatpump(JFrame parent) {

		if (remembered != null) {
			return remembered;
		}

		return INSTANCE.showAddressDialog(parent);
	}

	@SuppressWarnings("BusyWait")
	private InetSocketAddress showAddressDialog(JFrame parent) {
		Map<String, InetSocketAddress> entryMap = new HashMap<>();

		LOGGER.debug("Showing Address Dialog..");

		JDialog dialog = new JDialog(parent, tr("dialog.title"));
		JComboBox<String> input = new JComboBox<>();
		JCheckBox remember = new JCheckBox(tr("action.remember"));

		dialog.setSize(400, 150);

		JTextPane instruction = new JTextPane();
		TextPaneUtil.hideCaret(instruction);
		instruction.setContentType("text/html");
		instruction.setText(tr("dialog.message"));
		instruction.setEditable(false);
		dialog.add(instruction, BorderLayout.NORTH);


		input.setEditable(false);
		input.setEnabled(false);

		JPanel inputPanel = new JPanel(new FlowLayout());
		dialog.add(inputPanel);
		JLabel loading = new JLabel(tr("dialog.loading"));
		loading.setFont(loading.getFont().deriveFont(Font.ITALIC));
		inputPanel.add(loading);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton cancel = new JButton(new AbstractAction(tr("action.cancel")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				input.setSelectedItem(null);
				dialog.setVisible(false);
			}
		});

		JButton done = new JButton(tr("action.select"));
		done.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		footer.add(done);
		footer.add(cancel);
		dialog.add(footer, BorderLayout.SOUTH);

		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		dialog.setVisible(true);

		CompletableFuture<Void> c = CompletableFuture.runAsync(() -> {
			List<InetSocketAddress> addresses = discover();
			if (!addresses.isEmpty()) {
				addresses.forEach(a -> {
					String repr = a.getHostString();
					entryMap.put(repr, a);
					input.addItem(repr);
				});
			}
			inputPanel.remove(loading);
			inputPanel.add(input);
			dialog.repaint();
			inputPanel.add(remember);
			input.setEditable(true);
			input.setEnabled(true);
		});

		while (!c.isDone()) {
			loading.setText(loading.getText() + tr("dialog.loading.indicator"));
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {
			}
		}

		LOGGER.debug("Waiting for user interaction...");
		while (dialog.isVisible()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
		}

		InetSocketAddress selected = entryMap.get((String) input.getSelectedItem());

		if (remember.isSelected()) {
			remembered = selected;
		} else {
			remembered = null;
		}

		dialog.dispose();

		return selected;
	}

	private static String tr(String key, Object... args) {
		return Translations.translate(key, args);
	}

	public List<InetSocketAddress> discover() {

		List<InetSocketAddress> results = new ArrayList<>();

		// Broadcast discovery for Luxtronik heat pumps.

		for (int port : Constants.DISCOVERY_PORTS) {

			try (DatagramSocket socket = new DatagramSocket(port)) {
				socket.setBroadcast(true);
				sendBroadcast(socket, port);
				socket.setSoTimeout(Constants.DISCOVERY_TIMEOUT);

				while (true) {
					try {
						byte[] data = new byte[1024];
						DatagramPacket rec = new DatagramPacket(data, data.length);
						socket.receive(rec);
						String res = new String(data, StandardCharsets.US_ASCII);

						// if we receive what we just sent, continue
						if (res.startsWith(Constants.DISCOVERY_MAGIC_PACKET)) {
							continue;
						}
						res = res.trim();
						String ip_address = rec.getAddress().getHostAddress();//con[0];
						// if the response starts with the magic nonsense
						Integer res_port;
						if (res.startsWith(Constants.DISCOVERY_RESPONSE_PREFIX)) {
							String[] res_list = res.split(";");
							LOGGER.debug(
									"Received response from {} {}", ip_address, Arrays.toString(res_list)
							);
							try {
								res_port = Integer.parseInt(res_list[2]);
								if (res_port < 1 || res_port > 65535) {
									LOGGER.debug("Response contained an invalid port, ignoring");
									res_port = null;
								}
							} catch (Exception e) {
								res_port = null;
							}
							if (res_port == null) {
								LOGGER.debug("Response did not contain a valid port number, \n" +
										"an old Luxtronic software version might be the reason");
							} else {
								results.add(new InetSocketAddress(InetAddress.getByName(ip_address), res_port));
							}
							continue;
						}
						LOGGER.debug(
								"Received response from {}, but with wrong content, skipping ", ip_address);
						//if the timeout triggers, go on and use the other broadcast port
					} catch (SocketTimeoutException e) {
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error while searching: ", e);
			}
		}

		return results;
	}

	private static void sendBroadcast(DatagramSocket socket, int port) throws IOException {
		sendBroadcastMessage(socket, InetAddress.getByName("255.255.255.255"), port);
	}

	private static void sendBroadcastMessage(DatagramSocket socket, InetAddress address, int port) throws IOException {
		LOGGER.debug("Sending broadcast: " + address + " " + port);
		byte[] message = Constants.DISCOVERY_MAGIC_PACKET.getBytes(StandardCharsets.UTF_8);
		DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
		socket.send(packet);
	}
}
