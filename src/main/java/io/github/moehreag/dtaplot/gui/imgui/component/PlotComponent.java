package io.github.moehreag.dtaplot.gui.imgui.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.flag.ImPlotAxisFlags;
import imgui.extension.implot.flag.ImPlotFlags;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiSliderFlags;
import imgui.type.ImInt;
import io.github.moehreag.dtaplot.DataLoader;
import io.github.moehreag.dtaplot.Pair;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.dta.DtaFile;
import io.github.moehreag.dtaplot.dta.DtaParser;
import io.github.moehreag.dtaplot.gui.imgui.Dialogs;
import io.github.moehreag.dtaplot.gui.imgui.FileFilters;
import io.github.moehreag.dtaplot.gui.imgui.FileHandler;
import io.github.moehreag.dtaplot.gui.imgui.MenuBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotComponent extends ViewComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger("PlotViewComponent");
	private static final NumberFormat timeFormat = new DecimalFormat("00");

	private final Map<String, Pair<Double[], Double[]>> displayedDatasets = new HashMap<>();
	private final Map<Integer, List<Pair<String, String>>> tableData = new HashMap<>();
	private final Collection<Map<String, Value<?>>> data = new ArrayList<>();
	private List<String> setNames = new ArrayList<>();
	private final ImInt currentTime = new ImInt();
	private int minTime, maxTime;
	private final ImInt currentSet = new ImInt();
	private boolean updated;

	public void draw(float width, float height) {
		ImPlot.getStyle().setUseLocalTime(true);
		ImPlot.getStyle().setUse24HourClock(true);
		int autofit = 0;
		if (updated) {
			autofit |= ImPlotAxisFlags.AutoFit;
			updated = false;
		}
		if (ImPlot.beginPlot("##PlotGraph", tr("label.time"), "°C", new ImVec2(width * 2 / 3, height - 70), ImPlotFlags.NoTitle, ImPlotAxisFlags.Time | autofit, autofit)) {
			displayedDatasets.forEach((s, data) -> {
				ImPlot.plotLine(tr(s), data.getLeft(), data.getRight());
			});
			ImPlot.endPlot();
		}

		ImGui.sameLine();
		ImGui.setCursorPosX(width * (2 / 3f) + ImGui.getStyle().getItemSpacingX() * 2);
		ImGui.setNextItemWidth(width - (width * (2 / 3f) + ImGui.getStyle().getItemSpacingX() * 3));
		ImGui.sliderScalar("##times", ImGuiDataType.S32, currentTime, 0, maxTime, timeFormat(currentTime.get() + minTime), ImGuiSliderFlags.NoInput);

		float cursorY = ImGui.getCursorPosY();
		ImGui.setCursorPosY(cursorY - height + 70 + 30);
		ImGui.setCursorPosX(width * (2 / 3f) + ImGui.getStyle().getItemSpacingX() * 3);
		if (ImGui.beginChild("##plotChild")) {
			if (ImGui.beginTable("##plotTable", 2)) {

				tableData.forEach((integer, pairs) -> {
					int diff = minTime + currentTime.get() - integer;
					if (diff >= 0 && diff < 60) {
						for (Pair<String, String> pair : pairs) {

							ImGui.tableNextColumn();
							ImGui.text(pair.getLeft());
							ImGui.tableNextColumn();
							ImGui.text(pair.getRight());

						}
					}
				});

				ImGui.endTable();
			}
		}
		ImGui.endChild();
		ImGui.setCursorPosY(cursorY);

		ImGui.setCursorPosX(width / 6);
		ImGui.setNextItemWidth((width * 2 / 3) / 6);
		ImGui.combo("##plotKeys", currentSet, setNames.toArray(String[]::new));
		ImGui.sameLine();
		if (ImGui.button(tr("button.display"))) {
			displayedDatasets.clear();
			displayDataset(setNames.get(currentSet.get()));
		}
		ImGui.sameLine();
		if (ImGui.button(tr("button.add"))) {
			displayDataset(setNames.get(currentSet.get()));
		}
		ImGui.sameLine();
		if (ImGui.button(tr("button.remove"))) {
			LOGGER.info("removing dataset: " + setNames.get(currentSet.get()));
			displayedDatasets.remove(setNames.get(currentSet.get()));
		}
	}

	private Collection<Map<String, Value<?>>> getValidData(Collection<Map<String, Value<?>>> data) {
		Collection<Map<String, Value<?>>> entries = new ArrayList<>();
		for (Map<String, Value<?>> map : data) {
			Map<String, Value<?>> clone = new HashMap<>(map);
			entries.add(clone);
		}
		List<String> keys = new ArrayList<>();
		entries.stream().map(Map::keySet).forEach(k -> k.stream().filter(s -> !keys.contains(s)).forEach(keys::add));
		for (String key : keys) {
			List<Double> values = new ArrayList<>();
			for (Map<String, Value<?>> map : entries) {
				if (!map.containsKey(key) || !(map.get(key).get() instanceof Number)) {
					continue;
				}
				double val = ((Number) map.get(key).get()).doubleValue();
				if (!values.contains(val)) {
					values.add(val);
				}
			}
			if (values.size() <= 1) {
				for (Map<String, Value<?>> map : entries) {
					map.remove(key);
				}
			}
		}
		return Collections.unmodifiableCollection(entries);
	}

	private void displayDataset(String name) {
		LOGGER.info("Adding set: " + name);

		List<Double> times = new ArrayList<>();
		List<Double> values = new ArrayList<>();

		getValidData(data).stream()
				.sorted(Comparator.comparingInt(map -> ((Number) map.get("time").get()).intValue()))
				.forEachOrdered((stringValueMap) -> {
					int time = ((Number) stringValueMap.get("time").get()).intValue();

					Value<?> value = stringValueMap.get(name);
					if (value == null) {
						return;
					}
					if (value.get() instanceof Number) {

						double val = ((Number) value.get()).doubleValue();
						times.add((double) time);
						values.add(val);
					}
				});

		displayedDatasets.put(name, Pair.of(times.toArray(Double[]::new), values.toArray(Double[]::new)));
		updated = true;
	}

	public void clear() {
		data.clear();
		displayedDatasets.clear();
		setNames.clear();
		currentSet.set(0);
		tableData.clear();
	}

	public void load(byte[] data) {
		DtaFile file = DtaParser.get(data);
		load(file.getDatapoints());
	}

	public void load(Collection<Map<String, Value<?>>> data) {

		String selected;
		if (!setNames.isEmpty()) {
			selected = setNames.get(currentSet.get());
		} else {
			selected = null;
		}
		this.data.addAll(data);
		setNames = getValidData(this.data).stream().map(Map::keySet).reduce(new HashSet<>(), (s1, s2) -> {
			s1.addAll(s2);
			return s1;
		}).stream().distinct().filter(s -> !"time".equals(s)).collect(Collectors.toCollection(ArrayList::new));
		currentSet.set(Math.max(setNames.indexOf(selected), 0));

		updateTableData(data);
		if (displayedDatasets.isEmpty()) {
			currentSet.set(0);
			displayDataset(setNames.get(0));
		}
	}

	private void updateTableData(Collection<Map<String, Value<?>>> data) {

		int minTime = -1;
		int maxTime = -1;
		for (Map<String, Value<?>> map : data) {

			int time = ((Number) map.get("time").get()).intValue();
			if (minTime == -1 || minTime > time) {
				minTime = time;
			}
			if (maxTime == -1 || maxTime < time) {
				maxTime = time;
			}

			map.forEach((s, value) -> {
				if ("time".equals(s)) {
					return;
				}
				String val = value.get() + (value.getUnit() == null ? "" : value.getUnit());
				if ("true".equals(val) || "false".equals(val)) {
					val = "active." + val;
				}
				tableData.computeIfAbsent(time, i -> new ArrayList<>()).add(Pair.of(s, tr(val)));
			});
		}

		tableData.values().forEach(l -> l.sort(Comparator.comparing(Pair::getLeft)));

		this.maxTime = maxTime - minTime;
		this.minTime = minTime;
		currentTime.set(this.maxTime);
	}

	private String timeFormat(int time) {
		ZonedDateTime zTime = ZonedDateTime.ofInstant(
				Instant.ofEpochSecond(time),
				ZoneId.systemDefault());
		return tr("date.format",
				timeFormat.format(zTime.getHour()),
				timeFormat.format(zTime.getMinute()),
				timeFormat.format(zTime.getDayOfMonth()),
				timeFormat.format(zTime.getMonthValue()),
				timeFormat.format(zTime.getYear())
		);
	}

	@Override
	public MenuBar.Menu getMenu() {
		return MenuBar.Menu.of(tr("menu.plot"),
				MenuBar.MenuEntry.handler(tr("action.load"), load -> {
					Dialogs.showConnectDialog("plot.load", () -> load)
							.ifPresent(address -> {
								clear();
								load(address);
							});
				}),
				MenuBar.MenuEntry.handler(tr("action.addHp"), load -> {
					Dialogs.showConnectDialog("plot.addHp", () -> load)
							.ifPresent(this::load);
				}),
				MenuBar.MenuEntry.handler(tr("action.addFile"), load -> {
					Dialogs.showOpenDialog("plot.addFile", () -> load, FileFilters.OPEN).ifPresent(FileHandler::open);
				}),
				MenuBar.MenuEntry.handler(tr("action.append"), load -> {
					Dialogs.showSaveDialog("plot.append", () -> load, FileFilters.OPEN)
							.ifPresent(p -> DataLoader.getInstance().append(data, p));
				})
		);
	}

	private void load(InetSocketAddress address){
		try (InputStream in = FileHandler.getNewProcUri(address).openStream()) {
			load(in.readAllBytes());
		} catch (IOException e) {
			LOGGER.error("Failed to load file: ", e);
		}
	}
}
