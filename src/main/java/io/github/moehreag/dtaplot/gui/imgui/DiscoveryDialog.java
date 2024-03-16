package io.github.moehreag.dtaplot.gui.imgui;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.github.moehreag.dtaplot.Discovery;
import io.github.moehreag.dtaplot.Translations;

public class DiscoveryDialog {

	private static final DiscoveryDialog INSTANCE = new DiscoveryDialog();

	private static InetSocketAddress remembered;
	private boolean show = false;
	private int frameCount;
	private final Map<String, InetSocketAddress> entryMap = new HashMap<>();
	private final ImInt selected = new ImInt();

	private CompletableFuture<Void> discovery;
	private String loadingText = tr("dialog.loading");
	private long time;
	private ImBoolean rememberEnabled = new ImBoolean();
	private ImBoolean inputEnabled = new ImBoolean();
	private ImString inputText = new ImString();

	public static void open(String id) {

		if (remembered != null) {
			return;
		}
		INSTANCE.entryMap.clear();
		INSTANCE.selected.set(0);
		INSTANCE.time = System.currentTimeMillis();
		INSTANCE.loadingText = tr("dialog.loading");

		INSTANCE.show = true;
		ImGui.openPopup(tr("dialog.title") + "##" + id);
		INSTANCE.discovery = CompletableFuture
				.supplyAsync(() -> Discovery.getInstance().discover())
				.thenAccept(addresses -> {
					addresses.forEach(a -> {
						String repr = a.getHostString();
						INSTANCE.entryMap.put(repr, a);
					});
				});
	}

	public static Optional<InetSocketAddress> show(String id) {
		if (INSTANCE.show) {
			return Optional.ofNullable(remembered).or(() -> INSTANCE.display(id));
		}
		return Optional.empty();
	}

	private Optional<InetSocketAddress> display(String id) {
		ImGui.setNextWindowSize(400, 150, ImGuiCond.Once);
		if (ImGui.beginPopupModal(tr("dialog.title") + "##" + id)) {
			if (ImGui.getFrameCount() != frameCount) {
				frameCount = ImGui.getFrameCount();
			} else {
				ImGui.endPopup();
				return Optional.empty();
			}
			boolean remember;

			ImGui.textWrapped(tr("dialog.message"));

			String[] names = entryMap.keySet().toArray(String[]::new);
			if (discovery.isDone()) {
				if (inputEnabled.get()) {
					if (!names[selected.get()].isEmpty() && inputText.isEmpty()) {
						inputText.set(names[selected.get()]);
					}
					ImGui.inputText("##dialog.input", inputText);
				} else {
					ImGui.combo("##heatpumps_", selected, names);
				}
			} else {
				ImGui.textDisabled(loadingText);
			}

			ImGui.checkbox(tr("action.edit"), inputEnabled);
			remember = ImGui.checkbox(tr("action.remember"), rememberEnabled);

			ImGui.setCursorPos(
					ImGui.getContentRegionAvailX() -
					ImGui.calcTextSize(tr("action.cancel") +
									   tr("action.select")).x - ImGui.getStyle().getItemSpacingX() * 2 - ImGui.getStyle().getCellPaddingX(),
					ImGui.getWindowHeight() -
					ImGui.calcTextSize(tr("action.cancel") +
									   tr("action.select")).y - ImGui.getTextLineHeight() - 2
			);
			if (ImGui.button(tr("action.cancel"))) {
				show = false;
			}
			ImGui.sameLine();
			if (ImGui.button(tr("action.select")) && (names.length > 0 || inputEnabled.get())) {


				if (inputEnabled.get()) {
					try {
						InetSocketAddress selectedAddress = new InetSocketAddress(InetAddress.getByName(inputText.get()), 8889);
						if (remember) {
							remembered = selectedAddress;
						}
						show = false;
						ImGui.endPopup();
						return Optional.of(selectedAddress);
					} catch (UnknownHostException e) {
						throw new RuntimeException(e);
					}
				}

				InetSocketAddress selectedAddress = entryMap.get(names[selected.get()]);

				if (remember) {
					remembered = selectedAddress;
				}

				show = false;
				ImGui.endPopup();
				return Optional.of(selectedAddress);
			}

			if ((System.currentTimeMillis() - time) % 15 == 0) {
				time = System.currentTimeMillis();

				loadingText += tr("dialog.loading.indicator");
			}


			ImGui.endPopup();
		}
		return Optional.empty();
	}

	private static String tr(String key, Object... args) {
		return Translations.translate(key, args);
	}
}
