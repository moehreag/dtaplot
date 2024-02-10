package io.github.moehreag.dtaplot;

import java.nio.file.Path;

import com.formdev.flatlaf.FlatLightLaf;

public class Main {

	public static final String NAME;
	public static final String VERSION;
	public static final String URL;

	static {
		Package pkg = Main.class.getPackage();
		NAME = pkg.getImplementationTitle() == null ? "DtaPlot-Dev" : pkg.getImplementationTitle();
		VERSION = pkg.getImplementationVersion() == null ? "(unknown)" : pkg.getImplementationVersion();
		URL = "https://github.com/moehreag/dtaplot";

		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
	}

	public static void main(String[] args) {

		FlatLightLaf.setup();

		DtaPlot plot = new DtaPlot();

		if (args.length>0){
			for (String s : args){
				Path path = Path.of(s);
				plot.addToGraph(path);
			}
		} else {
			System.out.println("No file arguments found, opening empty view!");
		}

		plot.display();
	}

}