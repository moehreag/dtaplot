package io.github.moehreag.dtaplot;

import java.nio.file.Path;

import com.formdev.flatlaf.FlatLightLaf;

public class Main {

	public static void main(String[] args) {

		FlatLightLaf.setup();

		DtaPlot plot = new DtaPlot();

		plot.display();

		if (args.length>0){
			for (String s : args){
				Path path = Path.of(s);
				plot.addToGraph(path);
			}
		} else {
			System.out.println("No file arguments found, opening empty view!");
		}
	}

}