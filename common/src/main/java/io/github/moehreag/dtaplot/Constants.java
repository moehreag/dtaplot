package io.github.moehreag.dtaplot;

public class Constants {
	public static final String NAME;
	public static final String VERSION;
	public static final String URL = "https://github.com/moehreag/dtaplot";
	// List of ports that are known to respond to discovery packets
	static final int[] DISCOVERY_PORTS = new int[]{4444, 47808};
	// Time (in milliseconds) to wait for response after sending discovery broadcast
	static final int DISCOVERY_TIMEOUT = 1000;
	// Content of packet that will be sent for discovering heat pumps
	static final String DISCOVERY_MAGIC_PACKET = "2000;111;1;\u0000";
	// Content of response that is contained in responses to discovery broadcast
	static final String DISCOVERY_RESPONSE_PREFIX = "2500;111;";
	public static final int WRITE_TIMEOUT = 2;
	public static final int PARAMETERS_WRITE = 3002;
	public static final int PARAMETERS_READ = 3003;
	public static final int CALCULATIONS_READ = 3004;
	public static final int VISIBILITIES_READ = 3005;

	static {
		Package pkg = Main.class.getPackage();
		NAME = pkg.getImplementationTitle() == null ? "DtaPlot-Dev" : pkg.getImplementationTitle();
		VERSION = pkg.getImplementationVersion() == null ? "(unknown)" : pkg.getImplementationVersion();
	}
}
