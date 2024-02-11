package io.github.moehreag.dtaplot;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.moehreag.dtaplot.Value;
import lombok.Getter;

public class KeyValueTableModel extends AbstractTableModel {
	private final List<List<Object>> data = new ArrayList<>();
	@Getter
	private Collection<Map<String, Value<?>>> original;

	public KeyValueTableModel() {
		for (int i = 0; i < getColumnCount(); i++) {
			data.add(new ArrayList<>());
		}
	}

	public void insert(Collection<Map<String, Value<?>>> data) {
		clear();
		original = data;
		for (Map<String, Value<?>> map : data) {
			for (Map.Entry<String, Value<?>> entry : map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
				this.data.get(0).add(Translations.translate(entry.getKey()));
				this.data.get(1).add(entry.getValue().get());
			}
		}
		fireTableDataChanged();
	}

	public void insert(String key, String value){
		this.data.get(0).add(key);
		this.data.get(1).add(value);
		fireTableDataChanged();
	}

	public void clear(){
		this.data.get(0).clear();
		this.data.get(1).clear();
	}

	@Override
	public int getRowCount() {
		return data.get(0).size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(columnIndex).get(rowIndex);
	}

	@Override
	public String getColumnName(int column) {
		return column == 0 ? "Name" : "Value";
	}

}
