package io.github.moehreag.dtaplot;

import java.nio.file.Path;

import io.github.moehreag.dtaplot.gui.imgui.App;
import io.github.moehreag.dtaplot.gui.imgui.FileHandler;
import io.github.moehreag.dtaplot.gui.swing.DtaPlot;

public class Main {

	static {
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		if (Constants.NAME.contains("Dev")){
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
		}
	}

	public static void main(String[] args) {
		openImGui(args);
	}

	public static void openImGui(String[] args){
		new App();
		if (args.length>0){
			for (String s : args){
				Path path = Path.of(s);
				FileHandler.open(path);
			}
		} else {
			System.out.println("No file arguments found, opening empty view!");
		}
	}

	public static void openSwing(String[] args){
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