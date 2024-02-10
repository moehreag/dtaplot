package io.github.moehreag.dtaplot;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.JsonSerializer;
import io.toadlabs.jfgjds.data.JsonArray;
import io.toadlabs.jfgjds.data.JsonObject;
import io.toadlabs.jfgjds.data.JsonValue;
import lombok.Getter;

public class DataLoader {

	@Getter
	private static final DataLoader instance = new DataLoader();

	public Collection<Map<String, DtaFile.Value<?>>> load(Path file){
		try {
			List<Map<String, DtaFile.Value<?>>> list = new ArrayList<>();
			JsonArray object = JsonDeserializer.read(Files.newBufferedReader(file)).asArrayOrNull();
			if (object != null){
				object.forEach(jsonValue -> {
					Map<String, DtaFile.Value<?>> map = new HashMap<>();
					JsonObject o = jsonValue.asObject();
					o.forEach((s, val) -> {

						if (val.isNull()){
							return;
						}

						Object value;
						if (val.isNumber()) {
							value = val.getDoubleNumberValue();
						} else if (val.isArray()) {
							value = val.asArray().stream().map(JsonValue::getBooleanValue).toArray(Boolean[]::new);
						} else if (val.isBoolean()) {
							value = val.getBooleanValue();
						} else {
							value = val.getStringValue();
						}
						map.put(s, DtaFile.Value.of(value));
					});
					list.add(map);
				});
			}
			return list;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(Collection<Map<String, DtaFile.Value<?>>> data, Path file){
		JsonArray object = new JsonArray();
		data.forEach((map) -> {
			JsonObject val = new JsonObject();

			map.forEach((s, value) -> {
				Object valueO;
				if (value.get().getClass().isArray()){
					JsonArray a = new JsonArray();
					for (int i = 0; i< Array.getLength(value.get()); i++){
						a.add(JsonValue.coerce(Array.get(value.get(), i)));

					}
					valueO = a;
				} else {
					valueO = value.get();
				}
				val.put(s, JsonValue.coerce(valueO));
			});
			object.add(val);
		});

		try {
			Files.createDirectories(file.getParent());
			BufferedWriter writer = Files.newBufferedWriter(file);
			JsonSerializer.write(object, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void append(Collection<Map<String, DtaFile.Value<?>>> data, Path file){
		Collection<Map<String, DtaFile.Value<?>>> existing = load(file);

		for (Map<String, DtaFile.Value<?>> map : data){
			int time = ((Number)map.get("time").get()).intValue();

			boolean in = false;
			for (Map<String, DtaFile.Value<?>> e : existing){
				if (((Number)e.get("time").get()).intValue() == time){
					in = true;
				}
			}

			if (!in){
				existing.add(map);
			}
		}

		//existing.addAll(data);
		save(existing, file);
	}

}
