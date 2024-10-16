package io.github.moehreag.dtaplot.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import io.github.moehreag.dtaplot.Constants;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.socket.tcp.Calculations;
import io.github.moehreag.dtaplot.socket.tcp.Datatype;
import io.github.moehreag.dtaplot.socket.tcp.Parameters;
import io.github.moehreag.dtaplot.socket.tcp.Visibilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpSocket implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger("TcpSocket");

	private final ByteBuffer READ_BUFFER = ByteBuffer.allocateDirect(4);
	private final ByteBuffer WRITE_BUFFER = ByteBuffer.allocateDirect(8);

	private SocketChannel socket;

	private static final TcpSocket INSTANCE = new TcpSocket();

	private Parameters parameters;

	private TcpSocket() {
	}

	public void connect(InetSocketAddress address){
		LOGGER.info("Connecting to: {}:{}", address.getHostString(), address.getPort());
		try {
			socket = SocketChannel.open(address);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Collection<Map<String, Value<?>>> readAll(InetSocketAddress address) {
		try (TcpSocket socket = INSTANCE) {
			socket.connect(address);
			Parameters p = socket.readParameters();
			Collection<Map<String, Value<?>>> data = new ArrayList<>(p.getValues());
			Calculations c = socket.readCalculations();
			data.addAll(c.getValues());
			Visibilities v = socket.readVisibilities();
			data.addAll(v.getValues());
			return data;
		}
	}

	public static Parameters write(InetSocketAddress address){
		try (TcpSocket socket = INSTANCE) {
			socket.connect(address);
			LOGGER.info("Writing all values to the heatpump!");
			return socket.writeParameters(socket.parameters);
		}
	}

	public Parameters readParameters() {
		Parameters parameters = new Parameters();
		try {
			writeInts(socket, Constants.PARAMETERS_READ, 0);
			int cmd = readInt(socket);
			int length = readInt(socket);
			LOGGER.info("CMD: " + cmd + " Length: " + length);
			int[] data = read(socket, length);
			parameters.read(data);
		} catch (IOException e) {
			LOGGER.error("Failed to read: ", e);
		}

		return this.parameters = parameters;
	}

	public Visibilities readVisibilities() {
		Visibilities visibility = new Visibilities();
		try {
			writeInts(socket, Constants.VISIBILITIES_READ, 0);
			int cmd = readInt(socket);
			int length = readInt(socket);
			LOGGER.info("CMD: " + cmd + " Length: " + length);
			int[] data = readChars(socket, length);
			visibility.read(data);
		} catch (IOException e) {
			LOGGER.error("Failed to read: ", e);
		}
		return visibility;
	}

	public Calculations readCalculations() {
		Calculations calculations = new Calculations();
		try {
			writeInts(socket, Constants.CALCULATIONS_READ, 0);
			int cmd = readInt(socket);
			int stat = readInt(socket);
			int length = readInt(socket);
			LOGGER.info("CMD: " + cmd + " Stat: " + stat + " Length: " + length);
			int[] data = read(socket, length);
			calculations.read(data);
		} catch (IOException e) {
			LOGGER.error("Failed to read: ", e);
		}
		return calculations;
	}

	public Parameters writeParameters(Parameters params) {
		for (Map<String, Value<?>> map : params.getValues()) {
			for (int i = 0; i < map.size(); i++) {
				Datatype type = params.get(i);
				Value<?> val = map.get(type.getName());

				try {
					writeInts(socket, Constants.PARAMETERS_WRITE, i, type.write(val));
				} catch (IOException e) {
					LOGGER.error("Failed to write parameter: " + type.getName() + " with value: " + val.get());
				}
			}
		}

		try {
			Thread.sleep(Constants.WRITE_TIMEOUT);
		} catch (InterruptedException ignored) {
		}

		return readParameters();
	}

	private int[] read(SocketChannel c, int length) throws IOException {
		int[] data = new int[length];

		for (int i = 0; i < length; i++) {
			data[i] = readInt(c);
		}

		return data;
	}

	private int[] readChars(SocketChannel c, int length) throws IOException {
		int[] data = new int[length];

		for (int i = 0; i < length; i++) {
			data[i] = readChar(c);
		}

		return data;
	}

	private void writeInts(SocketChannel out, int... values) throws IOException {
		ByteBuffer buf = WRITE_BUFFER.clear();
		if (buf.remaining() < values.length * 4) {
			throw new IllegalStateException("Write Buffer Overflow! " + buf.remaining() + " < " + (values.length * 4));
		}
		buf.order(ByteOrder.BIG_ENDIAN);
		for (int i : values) {
			buf.putInt(i);
		}
		buf.flip();
		out.write(buf);
	}

	private int readInt(SocketChannel in) throws IOException {
		ByteBuffer buf = READ_BUFFER.clear();
		buf.limit(4);
		buf.order(ByteOrder.BIG_ENDIAN);
		in.read(buf);
		buf.flip();
		return buf.getInt();
	}

	private char readChar(SocketChannel in) throws IOException {
		ByteBuffer buf = READ_BUFFER.clear();
		buf.limit(1);
		buf.order(ByteOrder.BIG_ENDIAN);
		in.read(buf);
		buf.flip();
		return (char) buf.get();
	}

	@Override
	public void close() {
		LOGGER.info("Closing socket..");
		try {
			socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
