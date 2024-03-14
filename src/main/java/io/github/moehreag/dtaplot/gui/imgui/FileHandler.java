package io.github.moehreag.dtaplot.gui.imgui;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.moehreag.dtaplot.DataLoader;
import io.github.moehreag.dtaplot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger("FileHandler");

	public static URL getNewProcUri(InetSocketAddress address){
		try {
			return URI.create("http://"+address.getHostString()+"/NewProc").toURL();
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void open(Path file) {
		if (file.getFileName().toString().endsWith(".dta")) {
			try {
				byte[] bytes = Files.readAllBytes(file);
				App.View.PLOT.getComponent().load(bytes);
				App.getInstance().setView(App.View.PLOT);
			} catch (Exception ex) {
				LOGGER.error("Failed to load file: ", ex);
			}
		} else if (file.getFileName().toString().endsWith(".json")) {
			Collection<Map<String, Value<?>>> data = DataLoader.getInstance().load(file);
			Set<String> keys = data.stream().map(Map::keySet).reduce(new HashSet<>(), (strings, strings2) -> {
				strings.addAll(strings2);
				return strings;
			});
			if (keys.contains("time")) {
				LOGGER.info("Loading plot..");
				App.getInstance().setView(App.View.PLOT);
				App.View.PLOT.getComponent().load(data);
			} else if (keys.stream().anyMatch(s -> s.startsWith("ID"))) {
				LOGGER.info("Loading tcp table..");
				App.getInstance().setView(App.View.TCP);
				App.View.TCP.getComponent().load(data);
			} else {
				LOGGER.info("Loading ws table..");
				App.getInstance().setView(App.View.WS);
				App.View.WS.getComponent().load(data);
			}
		} else {
			LOGGER.info("Unsupported File: " + file + " (" + file.getFileName() + ")");
		}
	}
}
