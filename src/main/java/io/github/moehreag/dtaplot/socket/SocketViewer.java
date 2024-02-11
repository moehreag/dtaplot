package io.github.moehreag.dtaplot.socket;

import javax.swing.*;

import io.github.moehreag.dtaplot.KeyValueTableModel;

public class SocketViewer {

	public static void displayTcp(JScrollPane pane) {
		KeyValueTableModel tableModel;
		if (pane.getViewport().getView() instanceof JTable table){
			tableModel = (KeyValueTableModel) table.getModel();
		} else {
			JTable text = new JTable();
			tableModel = new KeyValueTableModel();
			text.setModel(tableModel);
			pane.setViewportView(text);
		}
		tableModel.insert(TcpSocket.readAll());
	}

	public static void displayWs(JScrollPane pane) {
		KeyValueTableModel tableModel;
		if (pane.getViewport().getView() instanceof JTable table){
			tableModel = (KeyValueTableModel) table.getModel();
		} else {
			JTable text = new JTable();
			tableModel = new KeyValueTableModel();
			text.setModel(tableModel);
			pane.setViewportView(text);
		}
		WebSocket.read(tableModel::insert);
	}
}
