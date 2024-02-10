package io.github.moehreag.dtaplot.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.github.moehreag.dtaplot.DataLoader;
import io.github.moehreag.dtaplot.Discovery;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.socket.tcp.Calculations;
import io.github.moehreag.dtaplot.socket.tcp.Datatype;
import io.github.moehreag.dtaplot.socket.tcp.Parameters;
import io.github.moehreag.dtaplot.socket.tcp.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpSocket implements AutoCloseable {

	private static final int WRITE_TIMEOUT = 2;

	private static final int PARAMETERS_WRITE = 3002;
	private static final int PARAMETERS_READ = 3003;
	private static final int CALCULATIONS_READ = 3004;
	private static final int VISIBILITIES_READ = 3005;

	private static final Logger LOGGER = LoggerFactory.getLogger("TcpSocket");

	private final ByteBuffer READ_BUFFER = ByteBuffer.allocateDirect(4);
	private final ByteBuffer WRITE_BUFFER = ByteBuffer.allocateDirect(8);

	private SocketChannel socket;

	public TcpSocket() {
		List<InetSocketAddress> addresses = Discovery.discover();

		for (InetSocketAddress a : addresses) {
			System.out.println("Connecting to: " + a.getHostString() + ":" + a.getPort());
			try {
				socket = SocketChannel.open(a);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void main(String[] args) {
		try (TcpSocket socket = new TcpSocket()) {
			Parameters p = socket.readParameters();
			DataLoader.getInstance().save(p.getValues(), Path.of("tcpparams.json"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Parameters readParameters() {
		Parameters parameters = new Parameters();
		parameters.read(read(PARAMETERS_READ));
		return parameters;
	}

	public Visibility readVisibility() {
		Visibility visibility = new Visibility();
		visibility.read(read(VISIBILITIES_READ));
		return visibility;
	}

	public Calculations readCalculations() {
		Calculations calculations = new Calculations();
		calculations.read(read(CALCULATIONS_READ));
		return calculations;
	}

	public Parameters writeParameters(Parameters params) {
		for (Map<String, Value<?>> map : params.getValues()) {
			for (int i = 0; i < map.size(); i++) {
				Datatype type = params.get(i);
				Value<?> val = map.get(type.getName());

				try {
					writeInts(socket, PARAMETERS_WRITE, i, type.write(val));
				} catch (IOException e) {
					LOGGER.error("Failed to write parameter: "+type.getName()+" with value: "+ val.get());
				}

			}
		}

		try {
			Thread.sleep(WRITE_TIMEOUT);
		} catch (InterruptedException ignored) {
		}

		return readParameters();
	}

	private int[] read(int command) {

		try {
			System.out.println("Connected!");
			writeInts(socket, command, 0);
			int cmd = readInt(socket);
			int length = readInt(socket);
			System.out.println("CMD: " + cmd + " Length: " + length);
			int[] data = read(socket, length);
			System.out.println(Arrays.toString(data));
			System.out.println();
			return data;

		} catch (IOException e) {
			LOGGER.error("Failed to read: ", e);
		}

		return new int[0];
	}

	private int[] read(SocketChannel c, int length) throws IOException {
		int[] data = new int[length];

		for (int i = 0; i < length; i++) {
			data[i] = readInt(c);
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
		buf.order(ByteOrder.BIG_ENDIAN);
		in.read(buf);
		buf.flip();
		return buf.getInt();
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}
}
