package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import ptolemy.plot.Plot;

public class DtaPlot {

	private static final String HEATPUMP_LOCATION = "http://192.168.178.47/proclog";
	private static final NumberFormat timeFormat = new DecimalFormat("00");

	private final Collection<Map<String, DtaFile.Value<?>>> data = new ArrayList<>();
	private final Plot plot = new Plot();
	private final Vector<String> selectionItems = new Vector<>();
	private final JComboBox<String> selections = new JComboBox<>(selectionItems);

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
		fileMenu.add(new AbstractAction("Export") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File("."));
				List<String> supported = new ArrayList<>(Arrays.asList(ImageIO.getWriterFileSuffixes()));
				supported.add("eps");
				chooser.setFileFilter(new FileNameExtensionFilter("Supported Files", supported.toArray(String[]::new)));
				for (String s : supported){
					chooser.addChoosableFileFilter(new FileNameExtensionFilter(s.toUpperCase(Locale.ROOT)+" Image", s));
				}
				if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;
				String fileName = chooser.getSelectedFile().getName();
				if (supported.stream().noneMatch(fileName::endsWith)){
					return;
				}
				try (OutputStream out = Files.newOutputStream(chooser.getSelectedFile().toPath())) {

					if (fileName.endsWith(".eps")) {
						plot.export(out);
					} else {
						ImageIO.write(plot.exportImage(new Rectangle(frame.getSize())), fileName.substring(fileName.lastIndexOf(".")+1), out);
					}
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		fileMenu.add(new AbstractAction("Quit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		menuBar.add(fileMenu);

		/*selections.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED){
				addPoints();
			}
		});*/

		JButton set = new JButton("Display");
		JButton add = new JButton("Add to graph");
		set.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPoints();
			}
		});
		add.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addDataset((String) selections.getSelectedItem());
			}
		});

		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.SOUTH);
		panel.add(selections);
		panel.add(set);
		panel.add(add);
		frame.add(menuBar, BorderLayout.NORTH);
		frame.add(plot, BorderLayout.CENTER);

		frame.setSize(800, 580);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		plot.fillPlot();
		plot.repaint();
		plot.requestFocusInWindow();
		frame.validate();
		frame.setVisible(true);
	}

	public void addToGraph(byte[] bytes){
		DtaFile dta = DtaParser.get(bytes);
		addToGraph(dta.getDatapoints());
	}

	public void addToGraph(Path file){
		if (file.getFileName().toString().endsWith(".dta")) {
			try {
				byte[] bytes = Files.readAllBytes(file);
				addToGraph(bytes);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		} else if (file.getFileName().toString().endsWith(".json")) {
			addToGraph(DataLoader.getInstance().load(file));
		} else {
			System.out.println("Unsupported File: "+file+ " ("+file.getFileName()+")");
		}
	}

	public void addToGraph(Collection<Map<String, DtaFile.Value<?>>> data){
		System.out.println("Adding "+ data.size()+" points to the graph");
		Set<Integer> times = this.data.stream().map(map -> (int)map.get("time").get()).collect(Collectors.toSet());
		this.data.addAll(data.stream()
				.filter(stringValueMap -> !times.contains((Integer) stringValueMap.get("time").get())).toList());
		refreshSelection();
		addPoints();
	}

	private void refreshSelection(){
		ItemListener[] listeners = selections.getItemListeners();
		for (ItemListener i : listeners){
			selections.removeItemListener(i);
		}
		String prev = (String) selections.getSelectedItem();
		selectionItems.clear();
		data.stream().map(Map::keySet).forEach(strings -> strings.stream()
				.filter(s -> !"time".equals(s)).distinct()
				.filter(s -> !selectionItems.contains(s)).forEach(selections::addItem));
		if (prev != null){
			selections.setSelectedItem(prev);
		}
		for (ItemListener i : listeners){
			selections.addItemListener(i);
		}
	}

	public void addPoints(){
		plot.clear(true);
		plot.clearLegends();
		plot.setXLabel("time");
		plot.setYLabel("°C");
		System.out.println("Plotting "+data.size()+" data points!");
		String selected = (String) selections.getSelectedItem();
		addDataset(selected);
	}

	private void addDataset(String setName){
		int set = plot.getNumDataSets();
		plot.addLegend(set, setName);
		data.stream()
				.sorted(Comparator.comparingInt(map -> (Integer) map.get("time").get()))
				.forEachOrdered((stringValueMap) -> {
					int time = (int) stringValueMap.get("time").get();

					DtaFile.Value<?> value = stringValueMap.get(setName);
					if (value.get() instanceof Number) {
						ZonedDateTime zTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time),
								ZoneId.systemDefault());
						String label = String.format("%s:%s (%s.%s.%s)",
								timeFormat.format(zTime.getHour()),
								timeFormat.format(zTime.getMinute()),
								timeFormat.format(zTime.getDayOfMonth()),
								timeFormat.format(zTime.getMonthValue()),
								timeFormat.format(zTime.getYear())
						);
						plot.addXTick(label, time);
						plot.addPoint(set, time,
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
