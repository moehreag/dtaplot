package io.github.moehreag.dtaplot.gui.imgui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import lombok.Data;

import static io.github.moehreag.dtaplot.gui.imgui.component.ViewComponent.tr;

public class MenuBar {

	private static final List<Menu> menu = new ArrayList<>(List.of(
			Menu.of(tr("menu.file"), MenuEntry.handler(tr("action.open"), onOpen -> {
				if (onOpen) {
					Dialogs.openOpenDialog("menu_open", FileFilters.OPEN);
				}
				Dialogs.drawOpenDialog("menu_open").ifPresent(file -> {
					App.View.PLOT.getComponent().clear();
					FileHandler.open(file);
				});
			}), MenuEntry.simple(tr("action.quit"), () -> {
				App.getInstance().quit();
			})),
			Menu.of(tr("menu.view"),
					MenuEntry.simple(tr("view.plot"), () -> {
						App.getInstance().setView(App.View.PLOT);
					}),
					MenuEntry.simple(tr("view.tcp"), () -> {
						App.getInstance().setView(App.View.TCP);
					}),
					MenuEntry.simple(tr("view.ws"), () -> {
						App.getInstance().setView(App.View.WS);
					})
			)
	));

	private static Menu currentViewMenu;

	public static void open(App.View<?> current) {
		menu.forEach(m -> {
			if (ImGui.beginMenu(m.name)) {
				m.entries.forEach(e -> {
					if (ImGui.menuItem(e.name)) {
						e.action.run();
					}
				});
				ImGui.endMenu();
			}
		});
		currentViewMenu = current.getMenu();
		if (currentViewMenu != null) {
			if (ImGui.beginMenu(currentViewMenu.name)) {
				currentViewMenu.entries.forEach(e -> {
					if (ImGui.menuItem(e.name)) {
						e.action.run();
					}
				});
				ImGui.endMenu();
			}
		}
		if (ImGui.beginMenu(tr("menu.help"))) {
			if (ImGui.menuItem(tr("action.about"))) {
				AboutDialog.open();
			}
			ImGui.endMenu();
		}
	}

	public static void handle() {
		AboutDialog.show();
		menu.forEach(m -> m.entries.forEach(e -> e.render.run()));
		if (currentViewMenu != null) {
			currentViewMenu.entries.forEach(e -> e.render.run());
		}
	}

	@Data
	public static class Menu {
		private final String name;
		private final List<MenuEntry> entries;

		public static Menu of(String name, MenuEntry... entries) {
			return new Menu(name, Arrays.asList(entries));
		}
	}

	@Data
	public static class MenuEntry {
		private final String name;

		private final Runnable action;
		private final Runnable render;

		/*public static MenuEntry popup(String name, String popupName, int width, int height, Runnable popupGui){
			AtomicBoolean shouldShow = new AtomicBoolean();
			AtomicBoolean showing = new AtomicBoolean();
			return of(name, () -> {
				showing.set(false);
				shouldShow.set(true);
			}, () -> {
				if (shouldShow.get() && !showing.get()){
					shouldShow.set(false);
					showing.set(true);
					ImGui.openPopup(popupName);
				}
				if (showing.get()){
					ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() / 2 - width/2f,
							ImGui.getIO().getDisplaySizeY() / 2 - height/2f, ImGuiCond.Once);
					ImGui.setNextWindowSize(width, height, ImGuiCond.Once);

					if (ImGui.beginPopupModal(popupName)){
						popupGui.run();
					}
				}
			});
		}*/
		public static MenuEntry popup(String name, String popupName, int width, int height, Runnable popupGui) {
			AtomicBoolean shouldShow = new AtomicBoolean();
			return of(name, () -> {
				shouldShow.set(true);
			}, () -> {
				if (shouldShow.get()) {
					shouldShow.set(false);
					ImGui.openPopup(popupName + "##key");
				}

				ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() / 2 - width / 2f,
						ImGui.getIO().getDisplaySizeY() / 2 - height / 2f, ImGuiCond.Once);
				ImGui.setNextWindowSize(width, height, ImGuiCond.Once);

				if (ImGui.beginPopupModal(popupName + "##key")) {
					popupGui.run();
					ImGui.endPopup();
				}

			});
		}

		public static MenuEntry handler(String entryName, Consumer<Boolean> handler) {
			AtomicBoolean shouldShow = new AtomicBoolean();
			return of(entryName, () -> {
				shouldShow.set(true);
			}, () -> {
				if (shouldShow.get()) {
					shouldShow.set(false);
					handler.accept(true);
				} else {
					handler.accept(false);
				}
			});
		}

		public static MenuEntry of(String name, Runnable action, Runnable handler) {
			return new MenuEntry(name, action, handler);
		}

		public static MenuEntry simple(String name, Runnable onClick) {
			return of(name, onClick, () -> {
			});
		}
	}
}
