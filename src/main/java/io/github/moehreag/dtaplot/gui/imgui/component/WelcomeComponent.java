package io.github.moehreag.dtaplot.gui.imgui.component;

import java.io.InputStream;

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import io.github.moehreag.dtaplot.Constants;
import io.github.moehreag.dtaplot.gui.imgui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomeComponent extends ViewComponent {
	private static final Logger LOGGER = LoggerFactory.getLogger("WelcomeViewComponent");

	@Override
	public void draw(float width, float height) {

		float itemWidth;

		itemWidth = ImGui.calcTextSize(tr("text.welcome.title")).x;
		ImGui.pushFont(App.getInstance().titleFont);
		ImGui.setCursorPos(width / 2 - itemWidth / 2, height / 4);
		ImGui.text(tr("text.welcome.title"));
		ImGui.popFont();
		ImGui.setCursorPosY(ImGui.getCursorPosY() + 10);
		itemWidth = ImGui.calcTextSize(tr("text.welcome.text")).x;
		ImGui.setCursorPosX(width / 2 - itemWidth / 2);
		ImGui.text(tr("text.welcome.text"));


		itemWidth = ImGui.getStyle().getItemSpacingX() * 3;
		itemWidth += ImGui.calcTextSize(tr("action.open")).x;
		itemWidth += ImGui.calcTextSize(tr("action.load")).x;
		itemWidth += ImGui.calcTextSize(tr("action.connect.ws")).x;
		itemWidth += ImGui.calcTextSize(tr("action.connect.tcp")).x;

		ImGui.setCursorPos(width / 2 - (itemWidth / 2), height / 2 - 20);
		Dialogs.showOpenDialog("welcome.open", () -> ImGui.button(tr("action.open")), FileFilters.OPEN).ifPresent(FileHandler::open);
		ImGui.sameLine();
		Dialogs.showConnectDialog("w.load", () -> ImGui.button(tr("action.load"))).ifPresent(address -> {
			try (InputStream in = FileHandler.getNewProcUri(address).openStream()) {
				App.View.PLOT.getComponent().load(in.readAllBytes());
				App.getInstance().setView(App.View.PLOT);
			} catch (Exception ex) {
				LOGGER.error("Failed to load file: ", ex);
			}
		});
		ImGui.sameLine();
		Dialogs.showConnectDialog("w.connect.ws", () -> ImGui.button(tr("action.connect.ws"))).ifPresent(address -> {
			App.getInstance().setView(App.View.WS);
			SocketLoader.loadWS(address, App.View.WS.getComponent());
		});
		ImGui.sameLine();
		Dialogs.showConnectDialog("w.connect.tcp", () -> ImGui.button(tr("action.connect.tcp"))).ifPresent(address -> {
			App.getInstance().setView(App.View.TCP);
			SocketLoader.loadTCP(address, App.View.TCP.getComponent());
		});

		ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
		ImGui.setCursorPosY(height-ImGui.getTextLineHeight()-ImGui.getStyle().getItemInnerSpacingY()-4);
		String text = Constants.NAME+" "+Constants.VERSION;
		float textWidth = ImGui.calcTextSize(text).x;
		if (ImGui.invisibleButton(text+"##button", textWidth, ImGui.getTextLineHeight())){
			AboutDialog.open();
		}
		ImGui.popStyleVar();
		AboutDialog.show();
		ImGui.sameLine();
		ImGui.setCursorPos(ImGui.getCursorStartPosX(), height-ImGui.getTextLineHeight()-ImGui.getStyle().getItemInnerSpacingY()-4);
		ImGui.text(text);
		ImGui.getForegroundDrawList().addLine(ImGui.getCursorStartPosX(),
				height-ImGui.getStyle().getItemInnerSpacingY()-4,
				ImGui.getCursorStartPosX()+textWidth,
				height-ImGui.getStyle().getItemInnerSpacingY()-4,
				ImColor.rgba(ImGui.getStyle().getColor(ImGuiCol.ButtonHovered)));
	}
}
