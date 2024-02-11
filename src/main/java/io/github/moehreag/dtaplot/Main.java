package io.github.moehreag.dtaplot;

import java.nio.file.Path;

import com.formdev.flatlaf.FlatLightLaf;

public class Main {

	static {
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		if (Constants.NAME.contains("Dev")){
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
		}
	}

	public static void main(String[] args) {

		FlatLightLaf.setup();

		DtaPlot plot = new DtaPlot();

		if (args.length>0){
			for (String s : args){
				Path path = Path.of(s);
				plot.open(path);
			}
		} else {
			System.out.println("No file arguments found, opening empty view!");
		}

		plot.display();
	}

}