package io.github.moehreag.dtaplot.gui.imgui.component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.gui.imgui.MenuBar;

public abstract class ViewComponent {

	public static String tr(String key, Object... args){
		return Translations.translate(key, args);
	}

	public abstract void draw(float width, float height);

	public void init(){}
	public void unload(){}

	public MenuBar.Menu getMenu() {
		return null;
	}

	public Collection<Map<String, Value<?>>> getData(){
		return List.of();
	}
}
