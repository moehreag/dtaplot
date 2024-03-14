package io.github.moehreag.dtaplot.gui.imgui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import io.github.moehreag.dtaplot.Pair;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.gui.imgui.Dialogs;
import io.github.moehreag.dtaplot.gui.imgui.DiscoveryDialog;
import io.github.moehreag.dtaplot.gui.imgui.MenuBar;
import io.github.moehreag.dtaplot.gui.imgui.SocketLoader;
import io.github.moehreag.dtaplot.socket.TcpSocket;

public class TcpComponent extends ViewComponent {

	private final List<Pair<String, String>> content = new ArrayList<>();
	@Override
	public void draw(float width, float height) {
		if (ImGui.beginTable("##tcpTable", 2)){

			ImGui.tableSetupColumn("column.name");
			ImGui.tableSetupColumn("column.value");

			synchronized (content) {
				content.forEach(p -> {
					ImGui.tableNextColumn();
					ImGui.text(p.getLeft());
					ImGui.tableNextColumn();
					ImGui.text(p.getRight());
				});
			}

			ImGui.endTable();
		}
	}

	public void load(Collection<Map<String, Value<?>>> data){
		synchronized (content) {
			content.clear();
			data.forEach(map -> {
				map.forEach((s, value) -> {
					String val = value.getUnit() != null ? String.valueOf(value.get()) : value.get() + value.getUnit();
					content.add(Pair.of(s, val));
				});
			});
		}
	}

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
