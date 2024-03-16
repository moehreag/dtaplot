package io.github.moehreag.dtaplot.gui.imgui;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Objects;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import lombok.Getter;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

public class Window {

	@Getter
	private final long handle;

	private final Runnable processTask;

	public Window(int width, int height, String title, Runnable processTask) {
		GLFWErrorCallback.createPrint(System.err).set();

		if (GLFW.glfwPlatformSupported(GLFW.GLFW_PLATFORM_WAYLAND)) {
			GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_WAYLAND);
		}
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, 1);

		//GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		handle = GLFW.glfwCreateWindow(width, height, title, 0, 0);

		if (handle == 0) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer pWidth = stack.mallocInt(1);
			final IntBuffer pHeight = stack.mallocInt(1);

			GLFW.glfwGetWindowSize(handle, pWidth, pHeight);
			final GLFWVidMode vidmode = Objects.requireNonNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()));
			//GLFW.glfwSetWindowPos(handle, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		}

		GLFW.glfwMakeContextCurrent(handle);

		GL.createCapabilities();

		GLFW.glfwSwapInterval(GLFW.GLFW_TRUE);


		//GLFW.glfwShowWindow(handle);


		//clearBuffer();
		//renderBuffer();

		/*GLFW.glfwSetWindowSizeCallback(handle, new GLFWWindowSizeCallback() {
			@Override
			public void invoke(final long window, final int width, final int height) {
				runFrame();
			}
		});*/
		initImGui();

		try {
			ImFontConfig fontConfig = new ImFontConfig();
			fontConfig.setFontDataOwnedByAtlas(false);
			byte[] bytes;
			try (InputStream input = this.getClass().getResourceAsStream("/Inter-Regular.ttf")) {
				assert input != null;
				bytes = input.readAllBytes();
			}
			App.getInstance().font = ImGui.getIO().getFonts().addFontFromMemoryTTF(bytes, 16, fontConfig);
			App.getInstance().titleFont = ImGui.getIO().getFonts().addFontFromMemoryTTF(bytes, 24, fontConfig);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		imGuiGlfw.init(handle, true);
		imGuiGl3.init("#version 150");

		this.processTask = processTask;
	}

	private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
	private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
	private ImPlotContext plotContext;

	/**
	 * Method to dispose all used application resources and destroy its window.
	 */
	protected void dispose() {
		imGuiGl3.dispose();
		imGuiGlfw.dispose();
		disposeImGui();
		disposeWindow();
	}

	protected void initImGui() {
		ImGui.createContext();
		ImGui.getIO().setIniFilename(null);
		plotContext = ImPlot.createContext();
	}

	/**
	 * Main application loop.
	 */
	protected void run() {
		while (!GLFW.glfwWindowShouldClose(handle)) {
			runFrame();
		}
		dispose();
	}

	/**
	 * Method used to run the next frame.
	 */
	protected void runFrame() {
		startFrame();
		processTask.run();
		endFrame();
	}

	/**
	 * Method used to clear the OpenGL buffer.
	 */
	private void clearBuffer() {
		/*if (ImGui.getCurrentContext().isValidPtr()) {
			int col = ImColor.rgba(ImGui.getStyle().getColor(ImGuiCol.WindowBg));
			GL32.glClearColor(col & 0xFF, col >> 8 & 0xFF, col >> 16 & 0xFF, col >> 24 & 0xFF);
		}*/
		GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * Method called at the beginning of the main cycle.
	 * It clears OpenGL buffer and starts an ImGui frame.
	 */
	protected void startFrame() {
		clearBuffer();
		imGuiGlfw.newFrame();
		ImGui.newFrame();
	}

	/**
	 * Method called in the end of the main cycle.
	 * It renders ImGui and swaps GLFW buffers to show an updated frame.
	 */
	protected void endFrame() {
		ImGui.render();
		imGuiGl3.renderDrawData(ImGui.getDrawData());

		if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
			final long backupWindowPtr = GLFW.glfwGetCurrentContext();
			ImGui.updatePlatformWindows();
			ImGui.renderPlatformWindowsDefault();
			GLFW.glfwMakeContextCurrent(backupWindowPtr);
		}

		renderBuffer();
	}

	/**
	 * Method to render the OpenGL buffer and poll window events.
	 */
	private void renderBuffer() {
		GLFW.glfwSwapBuffers(handle);
		ScreenshotUtil.run();
		GLFW.glfwPollEvents();
	}

	/**
	 * Method to destroy Dear ImGui context.
	 */
	protected void disposeImGui() {
		ImPlot.destroyContext(plotContext);
		ImGui.destroyContext();
	}

	/**
	 * Method to destroy GLFW window.
	 */
	protected void disposeWindow() {
		Callbacks.glfwFreeCallbacks(handle);
		GLFW.glfwDestroyWindow(handle);
		GLFW.glfwTerminate();
		Objects.requireNonNull(GLFW.glfwSetErrorCallback(null)).free();
	}
}
