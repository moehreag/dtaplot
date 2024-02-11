package io.github.moehreag.dtaplot;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.moehreag.dtaplot.dta.DtaFile;
import io.github.moehreag.dtaplot.dta.DtaParser;
import io.github.moehreag.dtaplot.socket.SocketViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.plot.Plot;

public class DtaPlot {

	public static final Logger LOGGER = LoggerFactory.getLogger(DtaPlot.class.getSimpleName());

	private final Supplier<String> HEATPUMP_LOCATION = () -> DotEnv.getOrDefault("PROCLOG_FILE",
			() -> "http://" + Discovery.getHeatpump(this.frame).getHostString() + "/proclog");

	private static final NumberFormat timeFormat = new DecimalFormat("00");

	private final Collection<Map<String, Value<?>>> data = new ArrayList<>();
	private final Plot plot = new Plot();
	private final Map<String, Integer> datasets = new HashMap<>();
	private final List<String> displayedDatasets = new ArrayList<>();
	private final Vector<String> selectionItems = new Vector<>();
	private final JComboBox<String> selections = new JComboBox<>(selectionItems);
	private final JTable infos = new JTable(new KeyValueTableModel());
	private final JLabel timeLabel = new JLabel();
	private final JSlider timeSlider = new JSlider();
	private final JFrame frame = new JFrame();
	private final JPanel side = new JPanel();
	private View currentView = View.WELCOME;
	private final Map<View, AbstractAction> viewMenuActions = Map.of(
			View.PLOT, new AbstractAction(tr("view.plot")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (AbstractAction c : viewMenuActions.values()) {
						c.setEnabled(true);
					}
					setEnabled(false);
					currentView = View.PLOT;
					display();
				}
			},
			View.TCP, new AbstractAction(tr("view.tcp")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (AbstractAction c : viewMenuActions.values()) {
						c.setEnabled(true);
					}
					setEnabled(false);
					currentView = View.TCP;
					display();
				}
			}, View.WS, new AbstractAction(tr("view.ws")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (AbstractAction c : viewMenuActions.values()) {
						c.setEnabled(true);
					}
					setEnabled(false);
					currentView = View.WS;
					display();
				}
			});

	public void display() {
		display(false);
	}

	public void display(boolean loaded) {

		frame.setTitle("DtaPlot");
		frame.getContentPane().removeAll();
		side.removeAll();
		LOGGER.info("Loading view: " + currentView);
		if (currentView == View.WELCOME) {
			addPlaceholder();
		} else {
			JMenuBar menuBar = new JMenuBar();
			JMenu fileMenu = new JMenu(tr("menu.file"));

			fileMenu.add(new AbstractAction(tr("action.open")) {
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
			fileMenu.add(new AbstractAction(tr("action.addFile")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					CompletableFuture.runAsync(() -> {
						JFileChooser chooser = new JFileChooser(new File("."));
						chooser.setMultiSelectionEnabled(true);
						addFileFilters(chooser);
						if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						for (File f : chooser.getSelectedFiles()) {
							LOGGER.info("Adding file " + f + " to the graph!");
							addToGraph(f.toPath());
						}
					});
				}
			});
			fileMenu.add(new AbstractAction(tr("action.load")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					CompletableFuture.runAsync(() -> {
						String url = HEATPUMP_LOCATION.get();
						if (url.trim().isEmpty()) {
							return;
						}
						try (InputStream in = URI.create(url).toURL().openStream()) {
							byte[] bytes = in.readAllBytes();
							data.clear();
							addToGraph(bytes);
						} catch (IOException ex) {
							LOGGER.error("Failed to load file: ", ex);
						}
					});
				}
			});
			fileMenu.add(new AbstractAction(tr("action.addHp")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					CompletableFuture.runAsync(() -> {
						String url = HEATPUMP_LOCATION.get();
						if (url.trim().isEmpty()) {
							return;
						}
						try (InputStream in = URI.create(url).toURL().openStream()) {
							byte[] bytes = in.readAllBytes();
							addToGraph(bytes);
						} catch (IOException ex) {
							LOGGER.error("Failed to load file: ", ex);
						}
					});
				}
			});
			fileMenu.add(new AbstractAction(tr("action.save")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					CompletableFuture.runAsync(() -> {
						JFileChooser chooser = new JFileChooser(new File("."));
						FileFilter json = new FileNameExtensionFilter(tr("filter.json"), "json");
						chooser.setFileFilter(json);
						if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						DataLoader.getInstance().save(data, chooser.getSelectedFile().toPath());
					});
				}
			});
			fileMenu.add(new AbstractAction(tr("action.append")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(new File("."));
					FileFilter json = new FileNameExtensionFilter(tr("filter.json"), "json");
					chooser.setFileFilter(json);
					if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
						return;
					DataLoader.getInstance().append(data, chooser.getSelectedFile().toPath());
				}
			});
			fileMenu.add(new AbstractAction(tr("action.export")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(new File("."));
					List<String> supported = new ArrayList<>(Arrays.asList(ImageIO.getWriterFileSuffixes()));
					supported.add("eps");
					chooser.setFileFilter(new FileNameExtensionFilter(tr("filter.supported"), supported.toArray(String[]::new)));
					for (String s : supported) {
						chooser.addChoosableFileFilter(new FileNameExtensionFilter(s.toUpperCase(Locale.ROOT) + tr("filter.formatImage"), s));
					}
					if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
						return;
					String fileName = chooser.getSelectedFile().getName();
					if (supported.stream().noneMatch(fileName::endsWith)) {
						return;
					}
					try (OutputStream out = Files.newOutputStream(chooser.getSelectedFile().toPath())) {

						if (fileName.endsWith(".eps")) {
							plot.export(out);
						} else {
							ImageIO.write(plot.exportImage(new Rectangle(frame.getSize())), fileName.substring(fileName.lastIndexOf(".") + 1), out);
						}
					} catch (IOException ex) {
						LOGGER.error("Failed to save file: ", ex);
					}
				}
			});
			fileMenu.add(new AbstractAction(tr("action.quit")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
				}
			});
			menuBar.add(fileMenu);

			JMenu viewMenu = new JMenu(tr("menu.view"));

			viewMenuActions.values().stream().sorted(Comparator.comparing(a -> a.getValue(Action.NAME).toString())).forEach(viewMenu::add);
			menuBar.add(viewMenu);

			JMenu helpMenu = new JMenu(tr("menu.help"));
			helpMenu.add(new AbstractAction(tr("action.about")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AboutDialog(frame);
				}
			});
			menuBar.add(helpMenu);

			frame.add(menuBar, BorderLayout.NORTH);

			if (currentView == View.PLOT) {

				JButton set = new JButton(tr("button.display"));
				JButton add = new JButton(tr("button.add"));
				JButton remove = new JButton(tr("button.remove"));
				set.addActionListener(e -> {
							displayedDatasets.clear();
							displayedDatasets.add((String) selections.getSelectedItem());
							addPoints();
						}
				);
				add.addActionListener(e -> {
					if (!displayedDatasets.contains((String) selections.getSelectedItem())) {
						displayedDatasets.add((String) selections.getSelectedItem());
						addDataset((String) selections.getSelectedItem());
					}
				});
				remove.addActionListener(e -> {
					int dataset = datasets.get((String) selections.getSelectedItem());
					plot.clear(dataset);
					plot.removeLegend(dataset);
					displayedDatasets.remove((String) selections.getSelectedItem());

				});

				JPanel panel = new JPanel();
				frame.add(panel, BorderLayout.SOUTH);
				panel.add(selections);
				panel.add(set);
				panel.add(add);
				panel.add(remove);

				side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
				side.add(timeSlider);
				side.add(timeLabel);
				timeSlider.addChangeListener(e -> addInfos());


				side.add(new JScrollPane(infos));
				frame.add(side, BorderLayout.EAST);
				frame.add(plot);

				plot.fillPlot();
				plot.repaint();
				plot.requestFocusInWindow();
				frame.revalidate();
			} else if (currentView == View.WS && !loaded) {
				JScrollPane pane = new JScrollPane();
				CompletableFuture.runAsync(() -> SocketViewer.displayWs(pane));
				frame.add(pane, BorderLayout.CENTER);
			} else if (currentView == View.TCP && !loaded) {
				JScrollPane pane = new JScrollPane();
				CompletableFuture.runAsync(() -> SocketViewer.displayTcp(pane));
				frame.add(pane, BorderLayout.CENTER);
			}
		}

		if (!frame.isVisible()) {
			LOGGER.info("Opening view");
			frame.setSize(960, 580);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
		}

		frame.repaint();
		frame.setVisible(true);
	}

	private void addPlaceholder() {
		setView(View.PLOT);
		LOGGER.info("Adding placeholder..");
		JPanel view = new JPanel();
		view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
		JPanel buttons = new JPanel();
		JPanel text = new JPanel();
		JTextPane in = new JTextPane();
		in.setContentType("text/html");
		in.setEditable(false);
		in.setFont(new Font(in.getFont().getName(), in.getFont().getStyle(), 18));
		in.setText("<h1><center>" + tr("text.welcome.title") + "</center></h1><br><p>" + tr("text.welcome.text") + "</p>");

		view.add(Box.createVerticalGlue());
		text.add(in);
		in.setAlignmentX(Component.CENTER_ALIGNMENT);
		view.add(text);

		JButton open = new JButton(new AbstractAction(tr("action.open")) {
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
		buttons.add(open);

		JButton load = new JButton(new AbstractAction(tr("action.load")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				CompletableFuture.runAsync(() -> {
					String url = HEATPUMP_LOCATION.get();
					if (url.trim().isEmpty()) {
						return;
					}
					try (InputStream in = URI.create(url).toURL().openStream()) {
						byte[] bytes = in.readAllBytes();
						addToGraph(bytes);
					} catch (IOException ex) {
						LOGGER.error("Failed to load file: ", ex);
					}
				});
			}
		});
		buttons.add(load);

		view.add(buttons);
		view.add(Box.createVerticalGlue());
		frame.add(getBottomTextPane(), BorderLayout.SOUTH);

		frame.add(view, BorderLayout.CENTER);
	}

	private void setView(View view){
		currentView = view;
		viewMenuActions.values().forEach(a -> a.setEnabled(true));
		viewMenuActions.get(view).setEnabled(false);
	}

	private JTextPane getBottomTextPane() {
		JTextPane bottomText = new JTextPane();
		bottomText.setContentType("text/html");
		bottomText.setEditable(false);
		bottomText.setFont(new Font(bottomText.getFont().getName(), bottomText.getFont().getStyle(), 10));

		bottomText.setText("<a href=\"open-about\">" + Constants.NAME + " " + Constants.VERSION + "</a>");
		bottomText.addHyperlinkListener(e -> {
			if (e.getDescription().equals("open-about") && e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				new AboutDialog(frame);
			}
		});
		return bottomText;
	}

	public void addToGraph(byte[] bytes) {
		DtaFile dta = DtaParser.get(bytes);
		addToGraph(dta.getDatapoints());
	}

	public void addToGraph(Path file) {
		if (file.getFileName().toString().endsWith(".dta")) {
			try {
				byte[] bytes = Files.readAllBytes(file);
				addToGraph(bytes);
			} catch (IOException ex) {
				LOGGER.error("Failed to load file: ", ex);
			}
		} else if (file.getFileName().toString().endsWith(".json")) {
			Collection<Map<String, Value<?>>> data = DataLoader.getInstance().load(file);
			Set<String> keys = data.stream().map(Map::keySet).reduce(new HashSet<>(), (strings, strings2) -> {
				strings.addAll(strings2);
				return strings;
			});
			if (keys.contains("time")) {
				LOGGER.info("Loading plot..");
				setView(View.PLOT);
				addToGraph(data);
			} else if (keys.stream().anyMatch(s -> s.startsWith("ID"))) {
				LOGGER.info("Loading tcp table..");
				setView(View.TCP);
				addTable(data);
			} else {
				LOGGER.info("Loading ws table..");
				setView(View.WS);
				addTable(data);
			}
		} else {
			LOGGER.info("Unsupported File: " + file + " (" + file.getFileName() + ")");
		}
	}

	private void addTable(Collection<Map<String, Value<?>>> data) {
		JTable text = new JTable();
		KeyValueTableModel tableModel = new KeyValueTableModel();
		text.setModel(tableModel);
		tableModel.insert(data);
		display(true);
		frame.add(new JScrollPane(text), BorderLayout.CENTER);
		frame.revalidate();
	}

	public void addToGraph(Collection<Map<String, Value<?>>> data) {
		LOGGER.info("Adding " + data.size() + " points to the graph");
		Set<Integer> times = this.data.stream().map(map -> ((Number) map.get("time").get()).intValue()).collect(Collectors.toSet());
		this.data.addAll(data.stream()
				.filter(stringValueMap -> !times.contains(((Number) stringValueMap.get("time").get()).intValue())).toList());
		LOGGER.info("Available Datasets: " + data.stream().map(Map::keySet).reduce(new HashSet<>(), (strings, strings2) -> {
			strings.addAll(strings2);
			return strings;
		}).stream().reduce((s, s2) -> String.join(", ", s, s2)).orElse(""));

		display();


		List<Integer> t = this.data.stream().map(map -> ((Number) map.get("time").get()).intValue())
				.distinct().sorted(Integer::compare).toList();
		timeSlider.setMinimum(t.get(0));
		timeSlider.setMaximum(t.get(t.size() - 1));
		timeSlider.setMinorTickSpacing(60 * 60);
		timeSlider.setMajorTickSpacing(60 * 60 * 24);
		timeSlider.setEnabled(true);
		timeSlider.setPaintTicks(true);
		timeSlider.setValue(timeSlider.getMaximum());
		timeSlider.validate();
		timeLabel.setFont(new Font(timeLabel.getFont().getName(), timeLabel.getFont().getStyle(), 14));

		refreshSelection();
		addPoints();
		addInfos();

		side.setMaximumSize(new Dimension(frame.getWidth() / 4, frame.getHeight()));
		side.setPreferredSize(new Dimension(frame.getWidth() / 4, frame.getHeight()));
		frame.revalidate();
		frame.repaint();
	}

	private void refreshSelection() {
		ItemListener[] listeners = selections.getItemListeners();
		for (ItemListener i : listeners) {
			selections.removeItemListener(i);
		}
		String prev = (String) selections.getSelectedItem();
		selectionItems.clear();
		getValidData().stream().map(Map::keySet).forEach(strings -> strings.stream()
				.filter(s -> !"time".equals(s)).distinct()
				.filter(s -> !selectionItems.contains(s)).sorted().forEachOrdered(selections::addItem));
		if (prev != null) {
			selections.setSelectedItem(prev);
		}
		if (displayedDatasets.isEmpty()) {
			displayedDatasets.add((String) selections.getSelectedItem());
		}
		for (ItemListener i : listeners) {
			selections.addItemListener(i);
		}
	}

	public void addPoints() {
		datasets.clear();
		plot.clear(true);
		plot.clearLegends();
		plot.setXLabel(tr("label.time"));
		plot.setYLabel("°C");
		LOGGER.info("Plotting " + data.size() + " data points!");
		if (displayedDatasets.isEmpty()) {
			displayedDatasets.add((String) selections.getSelectedItem());
		}
		for (String s : displayedDatasets) {
			addDataset(s);
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

	private void addDataset(String setName) {
		LOGGER.info("Adding dataset: " + setName);
		int set = plot.getNumDataSets();
		datasets.put(setName, set);
		plot.addLegend(set, setName);

		getValidData().stream()
				.sorted(Comparator.comparingInt(map -> ((Number) map.get("time").get()).intValue()))
				.forEachOrdered((stringValueMap) -> {
					int time = ((Number) stringValueMap.get("time").get()).intValue();

					Value<?> value = stringValueMap.get(setName);
					if (value == null) {
						return;
					}
					if (value.get() instanceof Number) {
						ZonedDateTime zTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time),
								ZoneId.systemDefault());
						String label = tr("date.format",
								timeFormat.format(zTime.getHour()),
								timeFormat.format(zTime.getMinute()),
								timeFormat.format(zTime.getDayOfMonth()),
								timeFormat.format(zTime.getMonthValue()),
								timeFormat.format(zTime.getYear())
						);
						double val = ((Number) value.get()).doubleValue();
						plot.addXTick(label, time);
						plot.addPoint(set, time, val, true);
					}
				});

		plot.fillPlot();
		plot.repaint();
	}

	private synchronized void addInfos() {
		side.setVisible(false);
		data.stream()
				.filter(map -> {
					int diff = timeSlider.getValue() - ((Number) map.get("time").get()).intValue();
					return diff >= 0 && diff < 60;
				}).findAny()
				.ifPresent(m -> {
					KeyValueTableModel model = ((KeyValueTableModel) infos.getModel());
					model.clear();
					int time = ((Number) m.get("time").get()).intValue();
					ZonedDateTime zTime = ZonedDateTime.ofInstant(
							Instant.ofEpochSecond(time),
							ZoneId.systemDefault());
					String label = tr("date.format",
							timeFormat.format(zTime.getHour()),
							timeFormat.format(zTime.getMinute()),
							timeFormat.format(zTime.getDayOfMonth()),
							timeFormat.format(zTime.getMonthValue()),
							timeFormat.format(zTime.getYear())
					);
					timeLabel.setText(getFriendlyString("time")+": "+label);
					//builder.append("<h3>").append(getFriendlyString("time")).append(": ").append(label).append("</h3>");

					m.forEach((s, value) -> {
						if (value.get() instanceof Boolean) {
							model.insert(getFriendlyString(s), getFriendlyString(value.get()));
							/*builder.append("<p>")
									.append(getFriendlyString(s))
									.append(": ")
									.append(getFriendlyString(value.get()))
									.append("</p>");*/
						}
					});
				});
		//infos.setText("<html><body>" + builder + "</body></html>");
		//infos.setCaretPosition(0);
		side.setVisible(true);
	}

	private String getFriendlyString(Object val) {
		String s = String.valueOf(val);
		return tr(switch (s) {
			case "true" -> "active.true";
			case "false" -> "active.false";
			default -> s;
		});
	}

	private static void addFileFilters(JFileChooser chooser) {
		FileFilter supported = new FileNameExtensionFilter(tr("filter.supported"), "dta", "json");
		FileFilter dta = new FileNameExtensionFilter(tr("filter.dta"), "dta");
		FileFilter json = new FileNameExtensionFilter(tr("filter.json"), "json");
		chooser.setFileFilter(supported);
		chooser.addChoosableFileFilter(dta);
		chooser.addChoosableFileFilter(json);
	}


	private static String tr(String key, Object... args) {
		return Translations.translate(key, args);
	}

	private enum View {
		WELCOME,
		PLOT,
		TCP,
		WS
	}

}
