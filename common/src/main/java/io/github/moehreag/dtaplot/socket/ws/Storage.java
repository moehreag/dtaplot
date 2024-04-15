package io.github.moehreag.dtaplot.socket.ws;

import java.util.HashMap;
import java.util.Map;

import io.github.moehreag.dtaplot.Value;

public class Storage {

	public static final Map<String, String> idValueMap = new HashMap<>();
	public static final Map<String, String> idNameMap = new HashMap<>();

	public static Map<String, Value<?>> getMerged(){
		Map<String, Value<?>> merged = new HashMap<>();

		for (String s : idNameMap.keySet()){
			if (idValueMap.containsKey(s)) {
				merged.put(idNameMap.get(s), Value.of(idValueMap.get(s)));
			}
		}

		return merged;
	}

}
