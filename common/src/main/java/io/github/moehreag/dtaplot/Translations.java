package io.github.moehreag.dtaplot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.data.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Translations {
	private static final Logger LOGGER = LoggerFactory.getLogger(Translations.class.getSimpleName());
	private static final Map<String, Map<String, String>> translations = new HashMap<>();
	private static final String defaultLang = "de_de";
	private static String lang;

	static {
		try {
			load();
		} catch (Exception e) {
			LOGGER.error("Failed to load translations!", e);
		}
	}

	public static void load() throws IOException {
		loadLanguage(defaultLang, true);

		String language = System.getProperty("user.language");
		String country = System.getProperty("user.country");


		loadLanguage(lang = language.toLowerCase(Locale.ROOT) + "_" + country.toLowerCase(Locale.ROOT), false);
	}

	private static void loadLanguage(String name, boolean required) throws IOException {
		try (InputStream in = Translations.class.getResourceAsStream("/lang/" + name + ".json")) {
			if (in != null) {
				JsonValue value = JsonDeserializer.read(in, StandardCharsets.UTF_8);
				value.asObject().forEach((s, jsonValue) -> {
					if (jsonValue.isString() && !jsonValue.getStringValue().isBlank()) {
						translations.computeIfAbsent(name, a -> new HashMap<>())
								.put(s, jsonValue.getStringValue());
					}
				});
			} else if (required) {
				throw new IllegalStateException("Could not find file for language: "+name);
			}
		}
	}

	public static String translate(String key, Object... args) {
		return String.format(get(lang, key), args);
	}

	private static String get(String lang, String key) {
		if (defaultLang.equals(lang)){
			return translations.get(defaultLang).getOrDefault(key, key);
		}
		return translations.getOrDefault(lang, translations.get(defaultLang)).getOrDefault(key, get(defaultLang, key));
	}
}
