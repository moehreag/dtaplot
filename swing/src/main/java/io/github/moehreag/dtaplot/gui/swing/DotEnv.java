package io.github.moehreag.dtaplot.gui.swing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DotEnv {

	private static final Map<String, String> entries = new HashMap<>();

	static {
		try (BufferedReader reader = Files.newBufferedReader(Path.of(".env"))){
			reader.lines().filter(l -> !l.trim().isEmpty())
					.filter(l -> !l.startsWith("#"))
					.map(l -> l.split("=", 2))
					.filter(l -> l.length == 2)
					.forEach(l -> entries.put(l[0].trim(), l[1].trim()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String get(String key){
		if (!entries.containsKey(key)){
			throw new IllegalStateException("Missing .env value: "+key);
		}
		return entries.get(key);
	}

	public static String getOrDefault(String key, String fallback){
		return entries.getOrDefault(key, fallback);
	}

	public static String getOrDefault(String key, Supplier<String> fallback){
		if (!entries.containsKey(key)){
			return fallback.get();
		}
		return entries.get(key);
	}
}
