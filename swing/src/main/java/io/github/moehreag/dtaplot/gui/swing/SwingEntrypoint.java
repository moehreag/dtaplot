package io.github.moehreag.dtaplot.gui.swing;

import java.nio.file.Path;

import com.formdev.flatlaf.FlatLightLaf;
import io.github.moehreag.dtaplot.Main;

public class SwingEntrypoint {
	public static void main(String[] args){
		Main.init();
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
