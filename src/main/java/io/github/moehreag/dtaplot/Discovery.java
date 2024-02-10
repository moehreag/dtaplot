package io.github.moehreag.dtaplot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The discovery of heatpumps is based on
 * <a href="https://github.com/Bouni/python-luxtronik/blob/main/luxtronik/discover.py">the python implementation</a>
 *
 */
public class Discovery {

	private static final Logger LOGGER = LoggerFactory.getLogger(Discovery.class.getSimpleName());

	private static List<InetSocketAddress> adresses;
	private static InetSocketAddress remembered;

	public static InetSocketAddress getHeatpump(){

		if (remembered != null){
			return remembered;
		}

		return showAddressDialog();
	}

	@SuppressWarnings("BusyWait")
	private static InetSocketAddress showAddressDialog() {
		Map<String, InetSocketAddress> entryMap = new HashMap<>();

		JFrame dialog = new JFrame(tr("dialog.title"));
		dialog.setSize(400, 150);

		JTextPane instruction = new JTextPane();
		instruction.setContentType("text/html");
		instruction.setText(tr("dialog.message"));
		instruction.setEditable(false);
		dialog.add(instruction, BorderLayout.NORTH);

		JComboBox<String> input = new JComboBox<>();

		input.setEditable(false);
		input.setEnabled(false);

		JCheckBox remember = new JCheckBox("action.remember");

		JPanel inputPanel = new JPanel(new FlowLayout());
		dialog.add(inputPanel);
		JLabel loading = new JLabel(tr("dialog.loading"));
		loading.setFont(new Font(loading.getFont().getName(), Font.ITALIC, loading.getFont().getSize()));
		inputPanel.add(loading);

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
				dialog.setVisible(false);
				dialog.dispose();
			}
		});

		footer.add(done);
		footer.add(cancel);
		dialog.add(footer, BorderLayout.SOUTH);

		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		dialog.setVisible(true);

		CompletableFuture<Void> c = CompletableFuture.runAsync(() -> {
			List<InetSocketAddress> addresses = Discovery.discover();
			if (!addresses.isEmpty()) {
				addresses.forEach(a -> {
					String repr = a.getHostString();
					entryMap.put(repr, a);
					input.addItem(repr);
				});
			}
			inputPanel.remove(loading);
			inputPanel.add(input);
			inputPanel.add(remember);
			input.setEditable(true);
			input.setEnabled(true);
		});

		while (!c.isDone()){
			loading.setText(loading.getText()+tr("dialog.loading.indicator"));
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {
			}
		}

		while (dialog.isShowing()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
		}

		InetSocketAddress selected = entryMap.get((String) input.getSelectedItem());

		if (remember.isSelected()){
			remembered  = selected;
		} else {
			remembered = null;
		}

		return selected;
	}

	private static String tr(String key, Object... args){
		return Translations.translate(key, args);
	}

	public static List<InetSocketAddress> discover(){

		if (adresses != null){
			return adresses;
		}

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
					} catch (SocketTimeoutException e){
						break;
					}
				}
			} catch (Exception e){
				LOGGER.error("AA", e);
			}
		}

		return adresses = results;
	}

	private static void sendBroadcast(DatagramSocket socket, int port) throws IOException {
		sendBroadcastMessage(socket, InetAddress.getByName("255.255.255.255"), port);
	}

	private static void sendBroadcastMessage(DatagramSocket socket, InetAddress address, int port) throws IOException {
		LOGGER.debug("Sending broadcast: "+address+" "+port);
		byte[] message = Constants.DISCOVERY_MAGIC_PACKET.getBytes(StandardCharsets.UTF_8);
		DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
		socket.send(packet);
	}
}
