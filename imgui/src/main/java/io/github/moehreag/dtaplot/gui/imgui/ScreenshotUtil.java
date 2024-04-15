package io.github.moehreag.dtaplot.gui.imgui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;

import imgui.ImGui;
import io.github.moehreag.dtaplot.Pair;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ScreenshotUtil {

	private static final ArrayDeque<Pair<Integer, Runnable>> tasks = new ArrayDeque<>();

	public static void screenshot(Path out){
		push(() -> takeScreenshot(out));
	}

	private static void takeScreenshot(Path output){
		int[] width = new int[1], height = new int[1];
		GLFW.glfwGetWindowSize(App.getInstance().getWindow().getHandle(), width, height);
		takeScreenshotRegion(0, 0, width[0], height[0], output);
	}

	public static void screenshotRegion(int x, int y, int width, int height, Path out){
		push(() -> takeScreenshotRegion(x, y, width, height, out));
	}

	private static void takeScreenshotRegion(int x, int y, int width, int height, Path output){
		int[] windowWidth = new int[1], windowHeight = new int[1];
		GLFW.glfwGetWindowSize(App.getInstance().getWindow().getHandle(), windowWidth, windowHeight);
		ByteBuffer buf = ByteBuffer.allocateDirect(width*height*4);
		buf.order(ByteOrder.nativeOrder());

		GL11.glReadPixels(x, windowHeight[0] - y - height, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int pY=height-1;pY>=0;pY--){
			for (int pX = 0;pX<width;pX++){
				int r = buf.get() & 0xff, g = buf.get() & 0xff, b = buf.get() & 0xff, a = buf.get() & 0xff;
				image.setRGB(pX, pY, a << 24 | r << 16 | g << 8 | b);
			}
		}
		String fileName = output.getFileName().toString();
		try (OutputStream out = Files.newOutputStream(output)) {
			ImageIO.write(image, fileName.substring(fileName.lastIndexOf(".") + 1), out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void push(Runnable task){
		tasks.push(Pair.of(ImGui.getFrameCount(), task));
	}

	public static void run(){
		try {
			tasks.removeIf(pair -> {
				if (ImGui.getFrameCount() - pair.getLeft() > 3){
					pair.getRight().run();
					return true;
				}
				return false;
			});
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
