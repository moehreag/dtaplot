package io.github.moehreag.dtaplot.gui.util;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlHandler {
	public static void open(URL url) throws URISyntaxException, IOException {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop().browse(url.toURI());
		} else {
			try {
				ProcessBuilder builder = new ProcessBuilder("xdg-open", url.toString());
				builder.start();
			} catch (Exception ex) {
				throw new UnsupportedOperationException("Failed to open " + url.toString());
			}
		}
	}
}
