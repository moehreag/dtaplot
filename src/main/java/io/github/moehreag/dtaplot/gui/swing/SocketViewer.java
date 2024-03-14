package io.github.moehreag.dtaplot.gui.swing;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

import io.github.moehreag.dtaplot.gui.swing.DiscoveryDialog;
import io.github.moehreag.dtaplot.gui.swing.KeyValueTableModel;
import io.github.moehreag.dtaplot.gui.swing.TextPaneUtil;
import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.socket.TcpSocket;
import io.github.moehreag.dtaplot.socket.WebSocket;

public class SocketViewer {

	public static void displayTcp(JPanel panel) {
		JScrollPane pane = new JScrollPane();
		panel.add(pane);

		JTable text = getTable();
		KeyValueTableModel tableModel = new KeyValueTableModel();
		text.setModel(tableModel);
		pane.setViewportView(text);
		panel.revalidate();

		tableModel.insert(TcpSocket.readAll(DiscoveryDialog.getHeatpump(null)));
	}

	@SuppressWarnings("BusyWait")
	public static void displayWs(JPanel panel) {
		JScrollPane pane = new JScrollPane();
		panel.add(pane);

		JTable text = getTable();
		KeyValueTableModel tableModel = new KeyValueTableModel();
		text.setModel(tableModel);
		pane.setViewportView(text);
		panel.revalidate();

		WebSocket.read(DiscoveryDialog.getHeatpump(null), () -> {
			final String[] result = {null};
			JDialog dialog = new JDialog((Window) null, Translations.translate("dialog.password"));

			JTextPane instructions = new JTextPane();
			TextPaneUtil.hideCaret(instructions);
			instructions.setEditable(false);
			instructions.setText(Translations.translate("dialog.password.instruction"));
			instructions.setFont(new JLabel().getFont());
			dialog.add(instructions, BorderLayout.NORTH);
			JPanel center = new JPanel();
			JTextField input = new JTextField();

			center.setLayout(new BorderLayout());
			center.add(Box.createRigidArea(new Dimension(20, 35)), BorderLayout.SOUTH);
			dialog.add(Box.createRigidArea(new Dimension(20, 10)), BorderLayout.WEST);
			dialog.add(Box.createRigidArea(new Dimension(20, 10)), BorderLayout.EAST);
			center.add(input);
			center.add(Box.createRigidArea(new Dimension(20, 35)), BorderLayout.NORTH);
			dialog.add(center);

			input.setMaximumSize(new Dimension(input.getWidth(), input.getFont().getSize() + 3));

			JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			JButton cancel = new JButton(new AbstractAction(Translations.translate("action.cancel")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
					dialog.dispose();
				}
			});

			JButton done = new JButton(Translations.translate("action.select"));
			done.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {

					dialog.setVisible(false);
					dialog.dispose();
					result[0] = input.getText();
				}
			});

			footer.add(done);
			footer.add(cancel);
			dialog.add(footer, BorderLayout.SOUTH);

			dialog.setSize(400, 200);
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
			input.requestFocus();

			while (dialog.isVisible()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignored) {
				}
			}
			return Optional.ofNullable(result[0]);
		}, tableModel::insert);
	}

	private static JTable getTable() {
		JTable table = new JTable() {

			@Override
			public TableCellEditor getCellEditor(int row, int column) {
				if (getModel() instanceof KeyValueTableModel kV) {
					TableCellEditor ed = kV.getCellEditor(row, column, this);
					if (ed != null) {
						return ed;
					}
				}
				return super.getCellEditor(row, column);
			}

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				if (getModel() instanceof KeyValueTableModel kV) {
					TableCellRenderer re = kV.getCellRenderer(row, column, this);
					if (re != null) {
						return re;
					}
				}
				return super.getCellRenderer(row, column);
			}
		};
		table.setShowGrid(true);
		return table;
	}
}
