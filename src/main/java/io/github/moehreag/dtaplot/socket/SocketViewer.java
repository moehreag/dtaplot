package io.github.moehreag.dtaplot.socket;

import javax.swing.*;

import io.github.moehreag.dtaplot.KeyValueTableModel;

public class SocketViewer {

	public static void displayTcp(JScrollPane pane) {
		JTable text = new JTable();
		KeyValueTableModel tableModel = new KeyValueTableModel();
		text.setModel(tableModel);
		pane.setViewportView(text);
		tableModel.insert(TcpSocket.readAll());
	}

	public static void displayWs(JScrollPane pane) {
		JTable text = new JTable();
		KeyValueTableModel tableModel = new KeyValueTableModel();
		text.setModel(tableModel);
		pane.setViewportView(text);
		WebSocket.read(tableModel::insert);
	}
}
