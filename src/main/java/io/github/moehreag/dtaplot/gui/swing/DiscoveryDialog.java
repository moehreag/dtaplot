package io.github.moehreag.dtaplot.gui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.moehreag.dtaplot.Discovery;
import io.github.moehreag.dtaplot.Translations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryDialog {
	private static final DiscoveryDialog INSTANCE = new DiscoveryDialog();
	static InetSocketAddress remembered;

	public static InetSocketAddress getHeatpump(JFrame parent) {

		if (remembered != null) {
			return remembered;
		}

		return INSTANCE.showAddressDialog(parent);
	}

	@SuppressWarnings("BusyWait")
	private InetSocketAddress showAddressDialog(JFrame parent) {
		Map<String, InetSocketAddress> entryMap = new HashMap<>();

		LOGGER.debug("Showing Address Dialog..");

		JDialog dialog = new JDialog(parent, tr("dialog.title"));
		JComboBox<String> input = new JComboBox<>();
		JCheckBox remember = new JCheckBox(tr("action.remember"));

		dialog.setSize(400, 150);

		JTextPane instruction = new JTextPane();
		TextPaneUtil.hideCaret(instruction);
		instruction.setContentType("text/html");
		instruction.setText(tr("dialog.message"));
		instruction.setEditable(false);
		dialog.add(instruction, BorderLayout.NORTH);


		input.setEditable(false);
		input.setEnabled(false);

		JPanel inputPanel = new JPanel(new FlowLayout());
		dialog.add(inputPanel);
		JLabel loading = new JLabel(tr("dialog.loading"));
		loading.setFont(loading.getFont().deriveFont(Font.ITALIC));
		inputPanel.add(loading);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton cancel = new JButton(new AbstractAction(tr("action.cancel")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				input.setSelectedItem(null);
				dialog.setVisible(false);
			}
		});

		JButton done = new JButton(tr("action.select"));
		done.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		footer.add(done);
		footer.add(cancel);
		dialog.add(footer, BorderLayout.SOUTH);

		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		dialog.setVisible(true);

		CompletableFuture<Void> c = CompletableFuture.runAsync(() -> {
			List<InetSocketAddress> addresses = Discovery.getInstance().discover();
			if (!addresses.isEmpty()) {
				addresses.forEach(a -> {
					String repr = a.getHostString();
					entryMap.put(repr, a);
					input.addItem(repr);
				});
			}
			inputPanel.remove(loading);
			inputPanel.add(input);
			dialog.repaint();
			inputPanel.add(remember);
			input.setEditable(true);
			input.setEnabled(true);
		});

		while (!c.isDone()) {
			loading.setText(loading.getText() + tr("dialog.loading.indicator"));
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {
			}
		}

		LOGGER.debug("Waiting for user interaction...");
		while (dialog.isVisible()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
		}

		InetSocketAddress selected = entryMap.get((String) input.getSelectedItem());

		if (remember.isSelected()) {
			DiscoveryDialog.remembered = selected;
		} else {
			DiscoveryDialog.remembered = null;
		}

		dialog.dispose();

		return selected;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryDialog.class.getSimpleName());

	private static String tr(String key, Object... args) {
		return Translations.translate(key, args);
	}
}
