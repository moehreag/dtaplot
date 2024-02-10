package io.github.moehreag.dtaplot;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public static void main(String[] args){
		List<InetSocketAddress> addresses = discover();
		addresses.forEach(System.out::println);
	}

	public static List<InetSocketAddress> discover(){
		List<InetSocketAddress> results = new ArrayList<>();

		// Broadcast discovery for Luxtronik heat pumps.

		for (int port : Constants.LUXTRONIK_DISCOVERY_PORTS) {

			try (DatagramSocket socket = new DatagramSocket(port)) {
				socket.setBroadcast(true);
				sendBroadcast(socket, port);
				socket.setSoTimeout(Constants.LUXTRONIK_DISCOVERY_TIMEOUT);

				while (true) {
					try {
						byte[] data = new byte[1024];
						DatagramPacket rec = new DatagramPacket(data, data.length);
						socket.receive(rec);
						String res = new String(data, StandardCharsets.US_ASCII);

                		// if we receive what we just sent, continue
						if (res.startsWith(Constants.LUXTRONIK_DISCOVERY_MAGIC_PACKET)) {
							continue;
						}
						res = res.trim();
						String ip_address = rec.getAddress().getHostAddress();//con[0];
                		// if the response starts with the magic nonsense
						Integer res_port;
						if (res.startsWith(Constants.LUXTRONIK_DISCOVERY_RESPONSE_PREFIX)) {
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
								results.add(InetSocketAddress.createUnresolved(ip_address, res_port));
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

		return results;
	}

	private static void sendBroadcast(DatagramSocket socket, int port) throws IOException {
		sendBroadcastMessage(socket, InetAddress.getByName("255.255.255.255"), port);
	}

	private static void sendBroadcastMessage(DatagramSocket socket, InetAddress address, int port) throws IOException {
		LOGGER.debug("Sending broadcast: "+address+" "+port);
		byte[] message = Constants.LUXTRONIK_DISCOVERY_MAGIC_PACKET.getBytes(StandardCharsets.UTF_8);
		DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
		socket.send(packet);
	}
}
