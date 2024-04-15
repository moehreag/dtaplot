package io.github.moehreag.dtaplot.gui.imgui.component;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import io.github.moehreag.dtaplot.gui.imgui.Dialogs;
import io.github.moehreag.dtaplot.gui.imgui.DiscoveryDialog;
import io.github.moehreag.dtaplot.gui.imgui.MenuBar;
import io.github.moehreag.dtaplot.gui.imgui.SocketLoader;
import io.github.moehreag.dtaplot.socket.TcpSocket;

public class TcpComponent extends TableComponent {

	@Override
	public MenuBar.Menu getMenu() {
		return MenuBar.Menu.of(tr("menu.tcp"),
				MenuBar.MenuEntry.handler(tr("action.refresh"), open -> {
					Dialogs.showConnectDialog("tcp.refresh", () -> open)
							.ifPresent(a -> SocketLoader.loadTCP(a, this));
				}),
				MenuBar.MenuEntry.handler(tr("action.write"), open -> {
					if (open){
						ImGui.openPopup(tr("dialog.confirmwrite.title")+"##popup");
					}
					ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX()/2-150, ImGui.getIO().getDisplaySizeY()/2-75, ImGuiCond.Once);
					ImGui.setNextWindowSize(300, 150, ImGuiCond.Once);
					if (ImGui.beginPopupModal(tr("dialog.confirmwrite.title")+"##popup")){
						ImGui.textWrapped(tr("dialog.confirmwrite.text"));

						ImGui.setCursorPos(
								ImGui.getContentRegionAvailX() -
								ImGui.calcTextSize(tr("action.cancel") +
												   tr("action.ok")).x - ImGui.getStyle().getItemSpacingX()*2 - ImGui.getStyle().getCellPaddingX(),
								ImGui.getWindowHeight() -
								ImGui.calcTextSize(tr("action.cancel") +
												   tr("action.ok")).y - ImGui.getTextLineHeight() - 2
						);
						if (ImGui.button(tr("action.cancel"))){
							ImGui.closeCurrentPopup();
						}
						ImGui.sameLine();
						if (ImGui.button(tr("action.ok"))){
							ImGui.closeCurrentPopup();
							DiscoveryDialog.open("tcp.write");
						}
						ImGui.endPopup();
					}

					DiscoveryDialog.show("tcp.write").ifPresent(TcpSocket::write);
				})
		);
	}
}
