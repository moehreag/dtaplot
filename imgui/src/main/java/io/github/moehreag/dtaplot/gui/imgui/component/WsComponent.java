package io.github.moehreag.dtaplot.gui.imgui.component;

import java.util.Optional;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImString;
import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.gui.imgui.Dialogs;
import io.github.moehreag.dtaplot.gui.imgui.MenuBar;
import io.github.moehreag.dtaplot.gui.imgui.SocketLoader;
import io.github.moehreag.dtaplot.socket.WebSocket;

public class WsComponent extends TableComponent {
	private PwResult pwres = null;
	private final ImString pwInput = new ImString();
	private boolean doPwQuery;


	@Override
	public void draw(float width, float height) {
		super.draw(width, height);
		if (doPwQuery){
			doPwQuery = false;
			ImGui.openPopup(Translations.translate("dialog.password") + "##ws.pw");
		}
		ImGui.setNextWindowPos(width/2-150, height/2-75, ImGuiCond.Once);
		ImGui.setNextWindowSize(300, 150, ImGuiCond.Once);
		if (ImGui.beginPopupModal(Translations.translate("dialog.password") + "##ws.pw")){

			ImGui.textWrapped(Translations.translate("dialog.password.instruction"));
			ImGui.inputText("##pwbox", pwInput);

			ImGui.setCursorPos(
					ImGui.getContentRegionAvailX() -
					ImGui.calcTextSize(ViewComponent.tr("action.cancel") +
									   ViewComponent.tr("action.ok")).x - ImGui.getStyle().getItemSpacingX() * 2 - ImGui.getStyle().getCellPaddingX(),
					ImGui.getWindowHeight() -
					ImGui.calcTextSize(ViewComponent.tr("action.cancel") +
									   ViewComponent.tr("action.ok")).y - ImGui.getTextLineHeight() - 2
			);

			if (ImGui.button(Translations.translate("action.cancel"))){
				ImGui.closeCurrentPopup();
				pwres = new PwResult(true, null);
			}
			ImGui.sameLine();
			if (ImGui.button(Translations.translate("action.ok"))){
				ImGui.closeCurrentPopup();
				pwres = new PwResult(false, pwInput.get());
			}

			ImGui.endPopup();
		}
	}

	@Override
	public MenuBar.Menu getMenu() {
		return MenuBar.Menu.of(ViewComponent.tr("menu.ws"),
				MenuBar.MenuEntry.handler(ViewComponent.tr("action.connect"), open -> {
					Dialogs.showConnectDialog("ws.connect", () -> open)
							.ifPresent(a -> SocketLoader.loadWS(a, this));
				}),
				MenuBar.MenuEntry.simple(ViewComponent.tr("action.disconnect"), () -> {
					if (WebSocket.isConnected()) {
						WebSocket.disconnect();
					}
				})
		);
	}

	@SuppressWarnings("BusyWait")
	public Optional<String> queryPassword(){
		doPwQuery = true;
		while (pwres == null){
			try {
				Thread.sleep(200);
			} catch (InterruptedException ignored) {
			}
		}
		if (pwres.isCanceled){
			return Optional.empty();
		}
		return Optional.of(pwres.result);
	}


	private record PwResult(boolean isCanceled, String result) {

	}
}
