package io.github.moehreag.dtaplot.gui.imgui;

import java.net.URI;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import io.github.moehreag.dtaplot.Constants;
import io.github.moehreag.dtaplot.Translations;

public class AboutDialog {

	private static boolean show, shouldShow;

	public static void open(){
		shouldShow = true;
	}

	public static void show(){
		if (shouldShow && !show){
			show = true;
			ImGui.openPopup(tr("about.title"));
		}
		if (show) {
			ImGui.setNextWindowPos(ImGui.getWindowWidth() / 2 - 125, ImGui.getWindowHeight() / 2 - 100, ImGuiCond.Once);
			ImGui.setNextWindowSize(250, 200, ImGuiCond.Once);

			if (ImGui.beginPopupModal(tr("about.title"))) {

				ImGui.pushFont(App.getInstance().titleFont);
				ImGui.text(Constants.NAME+" "+Constants.VERSION);
				ImGui.popFont();

				ImGui.newLine();
				ImGui.textWrapped(tr("about.description"));

				ImGui.newLine();

				if (ImGui.button(tr("about.source"))){
					try {
						UrlHandler.open(URI.create(Constants.URL).toURL());
					} catch (Exception e) {
						ImGui.closeCurrentPopup();
						App.LOGGER.error("Failed to open url: ", e);
					}
				}

				ImGui.setCursorPos(
						ImGui.getContentRegionAvailX() -
						ImGui.calcTextSize(tr("action.close")).x - ImGui.getStyle().getItemSpacingX() - ImGui.getStyle().getCellPaddingX(),
						ImGui.getWindowHeight() -
						ImGui.calcTextSize(tr("action.close")).y - ImGui.getTextLineHeight() - 2
				);
				if (ImGui.button(tr("action.close"))) {
					show = shouldShow = false;
					ImGui.closeCurrentPopup();
				}
				ImGui.endPopup();
			}
		}
	}

	private static String tr(String key){
		return Translations.translate(key);
	}
}
