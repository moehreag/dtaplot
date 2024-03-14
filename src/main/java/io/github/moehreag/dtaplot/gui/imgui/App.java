package io.github.moehreag.dtaplot.gui.imgui;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import io.github.moehreag.dtaplot.Constants;
import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.gui.imgui.component.*;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	public static final Logger LOGGER = LoggerFactory.getLogger(App.class.getSimpleName());

	private View<?> current = View.WELCOME;

	public ImFont font;
	public ImFont titleFont;

	private static App INSTANCE;
	private final Window window;

	public App() {
		INSTANCE = this;
		window = new Window(960, 580, Constants.NAME + " " + Constants.VERSION, this::process);
		window.run();
	}

	public static App getInstance() {
		return INSTANCE;
	}

	public void setView(View<?> next) {
		current.getComponent().unload();
		current = next;
		current.getComponent().init();
	}

	private void process() {
		ImGui.pushFont(font);
		ImGui.getStyle().setTabRounding(5);
		ImGui.getStyle().setChildRounding(5);
		ImGui.getStyle().setGrabRounding(5);
		ImGui.getStyle().setFrameRounding(5);
		ImGui.getStyle().setPopupRounding(5);
		float width = ImGui.getIO().getDisplaySizeX();
		float height = ImGui.getIO().getDisplaySizeY();
		ImGui.setNextWindowSize(width, height, ImGuiCond.Always);

		ImGui.setNextWindowPos(0, 0, ImGuiCond.Once);
		int flags = ImGuiWindowFlags.NoTitleBar |
					ImGuiWindowFlags.NoResize |
					ImGuiWindowFlags.NoMove;
		if (current != View.WELCOME) {
			flags |= ImGuiWindowFlags.MenuBar;
		}
		if (ImGui.begin(Constants.NAME, flags)) {
			ImGui.getStyle().setWindowRounding(5);
			ImGui.alignTextToFramePadding();

			if (current != View.WELCOME) {
				if (ImGui.beginMenuBar()) {
					MenuBar.open(current);
					ImGui.endMenuBar();
				}
				MenuBar.handle();
				height -= 20;
			}

			current.draw(width, height);
			ImGui.getStyle().setWindowRounding(0);
		}
		ImGui.end();
		ImGui.popFont();
	}

	public void quit() {
		GLFW.glfwSetWindowShouldClose(window.getHandle(), true);
	}

	public static void main(String[] args) {
		new App();
	}

	private static String tr(String key, Object... args) {
		return Translations.translate(key, args);
	}

	@Getter
	public static class View<T extends ViewComponent> {
		public static final View<PlotComponent> PLOT = new View<>(new PlotComponent());
		public static final View<WsComponent> WS = new View<>(new WsComponent());
		public static final View<TcpComponent> TCP = new View<>(new TcpComponent());
		public static final View<WelcomeComponent> WELCOME = new View<>(new WelcomeComponent());

		private final T component;

		private View(T component) {
			this.component = component;
		}

		public void draw(float width, float height) {
			component.draw(width, height);
		}

		public MenuBar.Menu getMenu() {
			return component.getMenu();
		}
	}

}
