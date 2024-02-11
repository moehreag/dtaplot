package io.github.moehreag.dtaplot.socket;

import javax.swing.*;
import java.util.*;
import java.util.function.Supplier;

import com.formdev.flatlaf.FlatLightLaf;
import io.github.moehreag.dtaplot.KeyValueTableModel;
import io.github.moehreag.dtaplot.Main;
import io.github.moehreag.dtaplot.Value;

public class SocketViewer {

	public static void main(String[] args){
		Main.init();
		FlatLightLaf.setup();
		JFrame frame = new JFrame();
		JScrollPane pane = new JScrollPane();
		frame.add(pane);
		displayWs(pane);
		frame.setSize(800, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		/*JFrame other = new JFrame();
		other.add(new JScrollPane(displayTcp()));
		other.setSize(800, 400);
		other.setLocationRelativeTo(null);
		other.setVisible(true);*/
		//new DtaPlot().display();
	}

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
