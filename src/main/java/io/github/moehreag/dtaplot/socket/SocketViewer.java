package io.github.moehreag.dtaplot.socket;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import io.github.moehreag.dtaplot.KeyValueTableModel;

public class SocketViewer {

	public static void displayTcp(JScrollPane pane) {
		KeyValueTableModel tableModel;
		if (pane.getViewport().getView() instanceof JTable table){
			tableModel = (KeyValueTableModel) table.getModel();
		} else {
			JTable text = getTable();
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
			JTable text = getTable();
			tableModel = new KeyValueTableModel();
			text.setModel(tableModel);
			pane.setViewportView(text);
		}
		WebSocket.read(tableModel::insert);
	}

	private static JTable getTable(){
		JTable table = new JTable(){

			@Override
			public TableCellEditor getCellEditor(int row, int column) {
				if (getModel() instanceof KeyValueTableModel kV){
					TableCellEditor ed = kV.getCellEditor(row, column, this);
					if (ed != null){
						return ed;
					}
				}
				return super.getCellEditor(row, column);
			}

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				if (getModel() instanceof KeyValueTableModel kV){
					TableCellRenderer re = kV.getCellRenderer(row, column, this);
					if (re != null){
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
