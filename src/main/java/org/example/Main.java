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
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.versioned.DtaFile8209;
import ptolemy.plot.Plot;

public class Main {

	private static final String FILE_LOCATION = "http://192.168.178.47/proclog";
	private static final DateFormat formatter = new SimpleDateFormat("yyMMdd_HHmm");
	private static final NumberFormat timeFormat = new DecimalFormat("00");

	public static void main(String[] args) {

			FlatLightLaf.setup();


			Path p = Path.of("240121_2122_proclog.dta");
			open(p);
	}

	private static Path download() {
		String name = formatter.format(new Date());
		Path target = Path.of(name + "_" + FILE_LOCATION.substring(FILE_LOCATION.lastIndexOf("/") + 1) + ".dta");
		download(URI.create(FILE_LOCATION), target);
		return target;
	}

	private static void download(URI source, Path target) {
		try {
			Files.copy(source.toURL().openStream(), target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void open(Path file) {
		try {
			byte[] data = Files.readAllBytes(file);
			open(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void open(byte[] data) {
		DtaFile<?> dta = new DtaFile8209(ByteBuffer.wrap(data));

		plot(dta);
	}

	private static void plot(DtaFile<?> file) {
		final Collection<Map<String, DtaFile.Value<?>>> data = new ArrayList<>();
		if (file != null){
			data.addAll(file.datapoints.values());
		}
		JFrame frame = new JFrame();
		frame.setTitle("DtaPlot");

		Plot plot = new Plot();
		JComboBox<String> graphs = new JComboBox<>();
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
				if (chooser.getSelectedFile().getName().endsWith(".dta")) {
					try {
						byte[] bytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
						addToGraph(bytes, data, plot, (String) graphs.getSelectedItem());
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				} else if (chooser.getSelectedFile().getName().endsWith(".json")) {
					addToGraph(DataLoader.getInstance().load(chooser.getSelectedFile().toPath()), data, plot, (String) graphs.getSelectedItem());
				}
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
				if (chooser.getSelectedFile().getName().endsWith(".dta")) {
					try {
						byte[] bytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
						addToGraph(bytes, data, plot, (String) graphs.getSelectedItem());
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				} else if (chooser.getSelectedFile().getName().endsWith(".json")) {
					addToGraph(DataLoader.getInstance().load(chooser.getSelectedFile().toPath()), data, plot, (String) graphs.getSelectedItem());
				}
			}
		});
		fileMenu.add(new AbstractAction("Load from Heatpump..") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try (InputStream in = URI.create(FILE_LOCATION).toURL().openStream()) {
					byte[] bytes = in.readAllBytes();
					data.clear();
					addToGraph(bytes, data, plot, (String) graphs.getSelectedItem());
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		fileMenu.add(new AbstractAction("Add from Heatpump..") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try (InputStream in = URI.create(FILE_LOCATION).toURL().openStream()) {
					byte[] bytes = in.readAllBytes();
					addToGraph(bytes, data, plot, (String) graphs.getSelectedItem());
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


		file.datapoints.values().stream().map(Map::keySet).distinct()
				.forEach(strings -> strings.forEach(graphs::addItem));
		graphs.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPoints(plot, data, (String) graphs.getSelectedItem());

			}
		});

		loadFileIntoGraph(plot, file, (String) graphs.getSelectedItem());


		frame.setLayout(new BorderLayout());

		frame.add(plot, BorderLayout.CENTER);
		frame.add(graphs, BorderLayout.SOUTH);
		frame.add(menuBar, BorderLayout.NORTH);


		frame.setSize(800, 580);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		plot.fillPlot();
		plot.repaint();
		frame.validate();
		frame.setVisible(true);
	}

	private static void addToGraph(byte[] bytes, Collection<Map<String, DtaFile.Value<?>>> data, Plot plot, String selected){
		DtaFile<?> dta = new DtaFile8209(ByteBuffer.wrap(bytes));
		addToGraph(dta.datapoints.values(), data, plot, selected);
	}

	private static void addToGraph(Collection<Map<String, DtaFile.Value<?>>> data, Collection<Map<String, DtaFile.Value<?>>> existing, Plot plot, String selected){
		Set<Integer> times = existing.stream().map(map -> (int)map.get("time").get()).collect(Collectors.toSet());
		existing.addAll(data.stream()
				.filter(stringValueMap -> !times.contains((Integer) stringValueMap.get("time").get())).toList());
		addPoints(plot, existing, selected);
	}

	private static void addPoints(Plot plot, Collection<Map<String, DtaFile.Value<?>>> data, String selected){
		plot.clear(true);
		plot.setXLabel("time");
		plot.setYLabel("°C");
		AtomicInteger points = new AtomicInteger();
		System.out.println("Plotting "+data.size()+" data points!");
		data.stream()
				.sorted(Comparator.comparingInt(map -> (Integer) map.get("time").get()))
				.forEachOrdered((stringValueMap) -> {
					AtomicInteger dataset = new AtomicInteger(0);
					int time = (int) stringValueMap.get("time").get();

					DtaFile.Value<?> value = stringValueMap.get(selected);
					if (value.get() instanceof Number) {
						if (points.get() == 0) {
							plot.addLegend(dataset.get(), selected);
						}
						ZonedDateTime zTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time),
								ZoneId.systemDefault());
						plot.addXTick(timeFormat.format(zTime.getHour())+":"+ timeFormat.format(zTime.getMinute()), time);
						plot.addPoint(dataset.get(), time,
								((Number) value.get()).doubleValue(), true);
						dataset.getAndIncrement();
					}
					points.getAndIncrement();

				});
		plot.fillPlot();
		plot.repaint();
	}

	private static void loadFileIntoGraph(Plot plot, DtaFile<?> file, String selected) {
		plot.setXLabel("time");
		plot.setYLabel("°C");
		addPoints(plot, file.datapoints.values(), selected);
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