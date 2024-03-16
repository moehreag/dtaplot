package io.github.moehreag.dtaplot.gui.imgui;

import javax.imageio.ImageIO;
import java.util.Locale;

import io.github.moehreag.dtaplot.Translations;

public class FileFilters {

	public static final String ALL = ".*";
	public static final String OPEN = Dialogs.concatFileFilters(Dialogs.buildFileFilter(tr("filter.supported"), "json", "dta"),
			Dialogs.buildFileFilter(tr("filter.json"), "json"),
			Dialogs.buildFileFilter(tr("filter.dta"), "dta"));

	public static final String EXPORT = buildImageFileFilter();
	public static final String SAVE = Dialogs.buildFileFilter(tr("filter.json"), "json");

	private static String buildImageFileFilter(){
		String[] suffixes = ImageIO.getWriterFileSuffixes();
		String[] filters = new String[suffixes.length+1];
		for (int i=0;i<suffixes.length;i++){
			String s = suffixes[i];
			filters[i+1] = Dialogs.buildFileFilter(s.toUpperCase(Locale.ROOT)+
												 tr("filter.formatImage"), s);
		}
		filters[0] = Dialogs.buildFileFilter(tr("filter.supported"), suffixes);
		return Dialogs.concatFileFilters(filters);
	}

	private static String tr(String key, Object... args){
		return Translations.translate(key, args);
	}
}
