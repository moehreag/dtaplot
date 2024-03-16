package io.github.moehreag.dtaplot.gui.imgui;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;

public class Dialogs {

	public static Optional<Collection<Path>> showOpenMultipleDialog(String dialogKey, BooleanSupplier action, String fileFilters) {
		if (action.getAsBoolean()) {
			ImGuiFileDialog.openModal(dialogKey, "Open Files", addAllFilter(fileFilters), "", 0, 0, ImGuiFileDialogFlags.HideColumnType);
		}
		if (ImGuiFileDialog.display(dialogKey, ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
			if (ImGuiFileDialog.isOk()) {
				ImGuiFileDialog.close();
				return Optional.of(ImGuiFileDialog.getSelection().values().stream().map(Path::of).toList());
			}
			ImGuiFileDialog.close();
		}
		return Optional.empty();
	}

	public static Optional<Path> showOpenDialog(String dialogKey, BooleanSupplier action, String fileFilters) {
		if (action.getAsBoolean()) {
			openOpenDialog(dialogKey, fileFilters);
		}
		return drawOpenDialog(dialogKey);
	}

	public static void openOpenDialog(String dialogKey, String fileFilters){
		ImGuiFileDialog.openModal(dialogKey, "Open File", addAllFilter(fileFilters), "", 1, 0, ImGuiFileDialogFlags.HideColumnType);
	}

	public static Optional<Path> drawOpenDialog(String dialogKey){
		if (ImGuiFileDialog.display(dialogKey, ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
			if (ImGuiFileDialog.isOk()) {
				ImGuiFileDialog.close();
				return ImGuiFileDialog.getSelection().values().stream().findFirst().map(Path::of);
			}
			ImGuiFileDialog.close();
		}
		return Optional.empty();
	}

	public static Optional<Path> showSaveDialog(String dialogKey, BooleanSupplier action, String fileFilters) {
		if (action.getAsBoolean()) {
			ImGuiFileDialog.openModal(dialogKey, "Save File", addAllFilter(fileFilters), "", 1, 0, ImGuiFileDialogFlags.HideColumnType | ImGuiFileDialogFlags.ConfirmOverwrite);
		}
		if (ImGuiFileDialog.display(dialogKey, ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
			if (ImGuiFileDialog.isOk()) {
				ImGuiFileDialog.close();
				return Optional.of(Path.of(ImGuiFileDialog.getCurrentPath() + "/" + ImGuiFileDialog.getCurrentFileName()));
			}
			ImGuiFileDialog.close();
		}
		return Optional.empty();
	}

	public static Optional<InetSocketAddress> showConnectDialog(String id, BooleanSupplier action){
		if (action.getAsBoolean()) {
			DiscoveryDialog.open(id);
		}

		return DiscoveryDialog.show(id);
	}

	public static String buildFileFilter(String description, String... extensions) {
		StringBuilder ex = new StringBuilder();
		for (String s : extensions) {
			if (!ex.isEmpty()) {
				ex.append(",");
			}
			ex.append(".").append(s);
		}
		StringBuilder exA = new StringBuilder();
		for (String s : extensions) {
			if (!exA.isEmpty()) {
				exA.append(" ");
			}
			exA.append(".").append(s);
		}
		return description + " (" + exA + "){" + ex + "}";
	}

	public static String concatFileFilters(String... filters) {
		return String.join(",", filters);
	}

	private static String addAllFilter(String filters){
		if (filters.contains(",")){
			String[] parts = filters.split("},", 2);
			return parts[0] + "}," + FileFilters.ALL + "," + parts[1];
		}
		return filters+","+FileFilters.ALL;
	}
}
