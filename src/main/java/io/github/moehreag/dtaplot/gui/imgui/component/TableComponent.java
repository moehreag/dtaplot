package io.github.moehreag.dtaplot.gui.imgui.component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImString;
import io.github.moehreag.dtaplot.Pair;
import io.github.moehreag.dtaplot.Value;

public class TableComponent extends ViewComponent {
	protected final List<Pair<String, ?>> content = new ArrayList<>();
	protected final ImString filter = new ImString();
	private final String id = this.getClass().getSimpleName();
	private final Collection<Map<String, Value<?>>> data = new ArrayList<>();

	@Override
	public void draw(float width, float height) {
		ImGui.setNextItemWidth(width - ImGui.getCursorPosX() * 2);
		ImGui.inputTextWithHint("##table.filter." + id, tr("hint.filter"), filter);
		if (ImGui.beginChild("##tableChild." + id, width - ImGui.getStyle().getScrollbarSize(),
				height - ImGui.getItemRectMaxY())) {
			if (ImGui.beginTable("##table." + id, 2, ImGuiTableFlags.RowBg | ImGuiTableFlags.Borders)) {

				ImGui.tableSetupColumn(tr("column.name"));
				ImGui.tableSetupColumn(tr("column.value"));
				ImGui.tableHeadersRow();

				synchronized (content) {
					content.forEach(p -> {
						if (filter.isEmpty() || p.getLeft().toLowerCase(Locale.ROOT).contains(filter.get().toLowerCase(Locale.ROOT))) {
							ImGui.tableNextColumn();
							ImGui.text(p.getLeft());
							ImGui.tableNextColumn();
							if (p.getRight() instanceof String s) {
								ImGui.text(s);
							} else if (p.getRight() instanceof Pair<?, ?> pair) {
								if (ImGui.inputText("##tablefield." + p.getLeft(), (ImString) pair.getLeft())) {
									((Runnable) pair.getRight()).run();
								}
							}
						}
					});
				}

				ImGui.endTable();
			}
		}
		ImGui.endChild();
	}

	public void load(Collection<Map<String, Value<?>>> data) {
		this.data.clear();
		this.data.addAll(data);
		synchronized (content) {
			content.clear();
			data.forEach(map -> {
				map.forEach((s, value) -> {
					String val = value.getUnit() != null ? String.valueOf(value.get()) : value.get() + value.getUnit();
					if (value instanceof Value.Mutable<?> mut) {
						ImString string = new ImString(val);
						content.add(Pair.of(s, Pair.of(string, (Runnable) () -> {
							set(mut, string.get());
						})));
					} else {
						content.add(Pair.of(s, val));
					}
				});
			});
			content.sort(Comparator.comparing(Pair::getLeft));
		}
	}

	@SuppressWarnings("unchecked")
	private <T, B> void set(Value.Mutable<B> mut, T newVal) {
		if (newVal instanceof String s) {
			Class<?> cls = mut.get().getClass();
			try {
				if (!(mut.get() instanceof String)) {
					MethodHandle handle = MethodHandles.lookup().findStatic(cls, "valueOf", MethodType.methodType(cls, String.class));
					if (s.contains(" ")) {
						mut.set((B) handle.invoke(s.split(" ")[0]));
					} else {
						mut.set((B) handle.invoke(s));
					}
				} else {
					mut.set((B) s);
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}
}
