package org.example;

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

			//Path p = Path.of("240121_2122_proclog.dta");
			//open(p);
	}

	/*private static Path download() {
		String name = formatter.format(new Date());
		Path target = Path.of(name + "_" + FILE_LOCATION.substring(FILE_LOCATION.lastIndexOf("/") + 1) + ".dta");
		download(URI.create(FILE_LOCATION), target);
		return target;
	}

	private static void download(URI source, Path target) {
		try {
			Files.copy(source.toURL().openStream(), target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}*/

}