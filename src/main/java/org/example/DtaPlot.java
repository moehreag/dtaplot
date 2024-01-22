package org.example;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.example.versioned.DtaFile8209;
import ptolemy.plot.Plot;

public class DtaPlot {

	private static final String HEATPUMP_LOCATION = "http://192.168.178.47/proclog";
	private static final NumberFormat timeFormat = new DecimalFormat("00");

	private final Collection<Map<String, DtaFile.Value<?>>> data = new ArrayList<>();
	private final Plot plot = new Plot();
	private final JComboBox<String> selections = new JComboBox<>();

	public void display() {
		JFrame frame = new JFrame();
		frame.setTitle("DtaPlot");



		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");

		fileMenu.add(new AbstractAction("Open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File("."));
				addFileFilters(chooser);
				if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;
				data.clear();
				addToGraph(chooser.getSelectedFile().toPath());
			}
		});
		fileMenu.add(new AbstractAction("Add file to graph") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File("."));
				addFileFilters(chooser);
				if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;
				System.out.println("Adding file "+chooser.getSelectedFile()+" to the graph!");
				addToGraph(chooser.getSelectedFile().toPath());
			}
		});
		fileMenu.add(new AbstractAction("Load from Heatpump..") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try (InputStream in = URI.create(HEATPUMP_LOCATION).toURL().openStream()) {
					byte[] bytes = in.readAllBytes();
					data.clear();
					addToGraph(bytes);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		fileMenu.add(new AbstractAction("Add from Heatpump..") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try (InputStream in = URI.create(HEATPUMP_LOCATION).toURL().openStream()) {
					byte[] bytes = in.readAllBytes();
					addToGraph(bytes);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		fileMenu.add(new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File("."));
				FileFilter json = new FileNameExtensionFilter("JSON File", "json");
				chooser.setFileFilter(json);
				if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;
				DataLoader.getInstance().save(data, chooser.getSelectedFile().toPath());
			}
		});
		fileMenu.add(new AbstractAction("Append Data to File") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File("."));
				FileFilter json = new FileNameExtensionFilter("JSON File", "json");
				chooser.setFileFilter(json);
				if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;
				DataLoader.getInstance().append(data, chooser.getSelectedFile().toPath());
			}
		});
		fileMenu.add(new AbstractAction("Quit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		menuBar.add(fileMenu);

		selections.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPoints(data);

			}
		});

		frame.setLayout(new BorderLayout());

		frame.add(plot, BorderLayout.CENTER);
		frame.add(selections, BorderLayout.SOUTH);
		frame.add(menuBar, BorderLayout.NORTH);


		frame.setSize(800, 580);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		plot.fillPlot();
		plot.repaint();
		frame.validate();
		frame.setVisible(true);
	}

	public void addToGraph(byte[] bytes){
		DtaFile<?> dta = new DtaFile8209(ByteBuffer.wrap(bytes));
		addToGraph(dta.datapoints.values());
	}

	public void addToGraph(Path file){
		if (file.getFileName().endsWith(".dta")) {
			try {
				byte[] bytes = Files.readAllBytes(file);
				addToGraph(bytes);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		} else if (file.getFileName().endsWith(".json")) {
			addToGraph(DataLoader.getInstance().load(file));
		} else {
			System.out.println("Unsupported File: "+file);
		}
	}

	public void addToGraph(Collection<Map<String, DtaFile.Value<?>>> data){
		Set<Integer> times = this.data.stream().map(map -> (int)map.get("time").get()).collect(Collectors.toSet());
		this.data.addAll(data.stream()
				.filter(stringValueMap -> !times.contains((Integer) stringValueMap.get("time").get())).toList());
		addPoints(this.data);
	}

	public void addPoints(Collection<Map<String, DtaFile.Value<?>>> data){
		plot.clear(true);
		plot.clearLegends();
		plot.setXLabel("time");
		plot.setYLabel("°C");
		System.out.println("Plotting "+data.size()+" data points!");
		String prev = (String) selections.getSelectedItem();
		selections.removeAllItems();
		data.stream().map(Map::keySet).forEach(strings -> strings.stream()
				.filter(s -> !"time".equals(s)).distinct().forEach(selections::addItem));
		if (prev != null){
			selections.setSelectedItem(prev);
		}
		String selected = (String) selections.getSelectedItem();
		plot.addLegend(0, selected);
		data.stream()
				.sorted(Comparator.comparingInt(map -> (Integer) map.get("time").get()))
				.forEachOrdered((stringValueMap) -> {
					int time = (int) stringValueMap.get("time").get();

					DtaFile.Value<?> value = stringValueMap.get(selected);
					if (value.get() instanceof Number) {
						ZonedDateTime zTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time),
								ZoneId.systemDefault());
						plot.addXTick(timeFormat.format(zTime.getHour())+":"+ timeFormat.format(zTime.getMinute()), time);
						plot.addPoint(0, time,
								((Number) value.get()).doubleValue(), true);
					}

				});
		plot.fillPlot();
		plot.repaint();
	}

	private static void addFileFilters(JFileChooser chooser){
		FileFilter supported = new FileNameExtensionFilter("Supported Files", "dta", "json");
		FileFilter dta = new FileNameExtensionFilter("DTA File", "dta");
		FileFilter json = new FileNameExtensionFilter("JSON File", "json");
		chooser.setFileFilter(supported);
		chooser.addChoosableFileFilter(dta);
		chooser.addChoosableFileFilter(json);
	}

}
