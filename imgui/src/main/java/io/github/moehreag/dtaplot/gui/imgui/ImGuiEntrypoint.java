package io.github.moehreag.dtaplot.gui.imgui;

import java.nio.file.Path;

import io.github.moehreag.dtaplot.Main;

public class ImGuiEntrypoint {
	public static void main(String[] args){
		Main.init();
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
}
