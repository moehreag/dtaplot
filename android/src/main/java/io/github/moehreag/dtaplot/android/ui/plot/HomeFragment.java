package io.github.moehreag.dtaplot.android.ui.plot;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import io.github.moehreag.dtaplot.DataLoader;
import io.github.moehreag.dtaplot.Value;
import io.github.moehreag.dtaplot.android.databinding.FragmentHomeBinding;
import io.github.moehreag.dtaplot.dta.DtaParser;

public class HomeFragment extends Fragment {

	private FragmentHomeBinding binding;
	private GraphView graph;
	//private ExpandableListView list;
	private final Collection<Map<String, Value<?>>> data = new ArrayList<>();
	private final AtomicReference<String> selection = new AtomicReference<>();

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		HomeViewModel homeViewModel =
				new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(HomeViewModel.class);

		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		//list = binding.listItem;
		graph = binding.graph;

		/*list.setOnItemClickListener((parent, view, position, id) -> {
			selection.set((String) parent.getItemAtPosition(position));
			populateGraph();
		});*/

		graph.getViewport().setScalable(true);
		graph.getViewport().setScrollable(true);
		graph.getLegendRenderer().setVisible(true);
		graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

		load();

		FloatingActionButton fab = binding.fab;
		fab.setOnClickListener(view -> Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show());


		return root;
	}

	/**
	 * @noinspection unchecked
	 */
	private void updateList() {
		/*ArrayAdapter<String> adapter;
		if (!(list.getAdapter() instanceof ArrayAdapter)) {
			adapter = new ArrayAdapter<>(list.getContext(), android.R.layout.simple_expandable_list_item_1);
			list.setAdapter(adapter);
		} else {
			adapter = (ArrayAdapter<String>) list.getAdapter();
		}
		Set<String> entries = new HashSet<>();
		for (int i = 0; i < adapter.getCount(); i++) {
			entries.add(adapter.getItem(i));
		}
		getValidData().stream().map(Map::keySet).forEach(strings -> strings.stream()
				.filter(s -> !"time".equals(s)).distinct()
				.filter(s -> !entries.contains(s))
				.forEach(adapter::add));
		if (selection.get() == null){
			selection.set(adapter.getItem(0));
		}*/
	}

	public void open(byte[] bytes) {
		data.addAll(DtaParser.get(bytes).getDatapoints());
		updateList();
		populateGraph();
	}

	private void load() {
		try (InputStream in = URI.create("http://192.168.178.47/NewProc").toURL().openStream()) {
			byte[] bytes = new byte[in.available()];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) in.read();
			}
			data.clear();
			selection.set("TVL");
			open(bytes);
		} catch (Exception ignored) {
		}
	}

	public void open(Path file) {
		if (file.getFileName().toString().endsWith(".dta")) {
			try {
				byte[] bytes = Files.readAllBytes(file);
				open(bytes);
			} catch (Exception ex) {
				/*Error: failed to load file*/
			}
		} else if (file.getFileName().toString().endsWith(".json")) {
			Collection<Map<String, Value<?>>> data = DataLoader.getInstance().load(file);
			Set<String> keys = data.stream().map(Map::keySet).reduce(new HashSet<>(), (strings, strings2) -> {
				strings.addAll(strings2);
				return strings;
			});
			if (keys.contains("time")) {
				/*Plot*/
			} else if (keys.stream().anyMatch(s -> s.startsWith("ID"))) {
				/*TCP*/
			} else {
				/*WS*/
			}
		} else {
			/*Error: unsupported file*/
		}
	}

	private Collection<Map<String, Value<?>>> getValidData() {
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

	private void populateGraph() {
		LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
		getValidData().stream()
				.sorted(Comparator.comparingInt(map -> ((Number) map.get("time").get()).intValue()))
				.forEachOrdered((stringValueMap) -> {
					int time = ((Number) stringValueMap.get("time").get()).intValue();

					Value<?> value = stringValueMap.get(selection.get());
					if (value == null) {
						return;
					}
					if (value.get() instanceof Number) {
						double val = ((Number) value.get()).doubleValue();
						//plot.addXTick(label, time);
						series.appendData(new DataPoint(time * 1000, val), true, Integer.MAX_VALUE);
						//plot.addPoint(set, time, val, true);
					}
				});
		graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graph.getContext()));
		graph.addSeries(series);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}