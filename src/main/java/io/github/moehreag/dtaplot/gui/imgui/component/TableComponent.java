package io.github.moehreag.dtaplot.gui.imgui.component;

import java.util.*;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImString;
import io.github.moehreag.dtaplot.Pair;
import io.github.moehreag.dtaplot.Value;

public class TableComponent extends ViewComponent {
	protected final List<Pair<String, String>> content = new ArrayList<>();
	protected final ImString filter = new ImString();

	@Override
	public void draw(float width, float height) {
		ImGui.setNextItemWidth(width-ImGui.getCursorPosX()*2);
		ImGui.inputTextWithHint("##table.filter", tr("hint.filter"), filter);
		if (ImGui.beginChild("##tableChild", width-ImGui.getStyle().getScrollbarSize(),
				height-ImGui.getItemRectMaxY())) {
			if (ImGui.beginTable("##table", 2, ImGuiTableFlags.RowBg | ImGuiTableFlags.Borders)) {

				ImGui.tableSetupColumn(tr("column.name"));
				ImGui.tableSetupColumn(tr("column.value"));
				ImGui.tableHeadersRow();

				synchronized (content) {
					content.forEach(p -> {
						if (filter.isEmpty() || p.getLeft().toLowerCase(Locale.ROOT).contains(filter.get().toLowerCase(Locale.ROOT))) {
							ImGui.tableNextColumn();
							ImGui.text(p.getLeft());
							ImGui.tableNextColumn();
							ImGui.text(p.getRight());
						}
					});
				}

				ImGui.endTable();
			}
		}
		ImGui.endChild();
	}

	public void load(Collection<Map<String, Value<?>>> data) {
		synchronized (content) {
			content.clear();
			data.forEach(map -> {
				map.forEach((s, value) -> {
					String val = value.getUnit() != null ? String.valueOf(value.get()) : value.get() + value.getUnit();
					content.add(Pair.of(s, val));
				});
			});
			content.sort(Comparator.comparing(Pair::getLeft));
		}
	}
}
