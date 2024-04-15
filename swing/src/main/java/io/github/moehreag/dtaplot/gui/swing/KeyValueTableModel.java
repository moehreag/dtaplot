package io.github.moehreag.dtaplot.gui.swing;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.Value;
import lombok.Getter;

public class KeyValueTableModel extends AbstractTableModel {
	private final List<List<Object>> data = new ArrayList<>();
	@Getter
	private Collection<Map<String, Value<?>>> original;

	private final Map<Integer, Value<?>> rowIndexes = new HashMap<>();

	public KeyValueTableModel() {
		for (int i = 0; i < getColumnCount(); i++) {
			data.add(new ArrayList<>());
		}
	}

	public void insert(Collection<Map<String, Value<?>>> data) {
		clear();
		original = data;
		int index = 0;
		for (Map<String, Value<?>> map : data) {
			for (Map.Entry<String, Value<?>> entry : map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
				this.data.getFirst().add(Translations.translate(entry.getKey()));
				Object val = entry.getValue().get();
				if (!entry.getValue().getUnit().isEmpty()) {
					val = val + " " + entry.getValue().getUnit();
				}
				rowIndexes.put(index, entry.getValue());
				this.data.get(1).add(val);
				index++;
			}
		}
		fireTableDataChanged();
	}

	public void insert(String key, String value) {
		this.data.get(0).add(key);
		this.data.get(1).add(value);
		fireTableDataChanged();
	}

	public void clear() {
		rowIndexes.clear();
		this.data.get(0).clear();
		this.data.get(1).clear();
	}

	@Override
	public int getRowCount() {
		return data.getFirst().size();
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
		return Translations.translate(column == 0 ? "column.name" : "column.value");
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0 || original == null) {
			return false;
		}

		return rowIndexes.get(rowIndex) instanceof Value.Mutable<?>;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			set(aValue, rowIndex, columnIndex);
			fireTableCellUpdated(rowIndex, columnIndex);
		} catch (Throwable t) {
			t.printStackTrace();

			throw t;
		}
	}

	@SuppressWarnings("unchecked")
	private <T, B> void set(T newVal, int row, int column) {


		if (newVal instanceof String s) {
			Value<B> val = (Value<B>) rowIndexes.get(row);
			if (val instanceof Value.Mutable<B> mut) {
				Class<?> cls = val.get().getClass();
				try {
					if (!(val.get() instanceof String)) {
						MethodHandle handle = MethodHandles.lookup().findStatic(cls, "valueOf", MethodType.methodType(cls, String.class));
						if (s.contains(" ")) {
							mut.set((B) handle.invoke(s.split(" ")[0]));
						} else {
							mut.set((B) handle.invoke(s));
						}
					} else {
						mut.set((B) s);
					}
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}

			}
		} else {

			Value<T> val = (Value<T>) rowIndexes.get(row);

			if (val instanceof Value.Mutable<T> mut) {
				mut.set(newVal);
			}
		}
		data.get(column).set(row, newVal);
	}

	public TableCellEditor getCellEditor(int row, int column, JTable table) {
		if (column == 0) {
			return null;
		}
		Value<?> val = rowIndexes.get(row);

		if (!val.getUnit().isEmpty()) {
			return null;
		}

		if (val.get() instanceof Boolean){
			return table.getDefaultEditor(Boolean.class);
		}

		return null;
	}

	public TableCellRenderer getCellRenderer(int row, int column, JTable table) {
		if (column == 0) {
			return null;
		}

		return new ValueCellRenderer();
	}

	private class ValueCellRenderer extends DefaultTableCellRenderer {

		NumberFormat formatter;

		public void setValue(Object value) {
			if (formatter == null) {
				formatter = NumberFormat.getInstance();
			}
			try {

				if (value instanceof String s && s.contains(" ") && !s.matches("([:()])")) {

					try {
						String[] vals = s.split(" ", 2);
						setText(formatter.format(Double.parseDouble(vals[0])) + " " + vals[1]);
						return;
					} catch (NumberFormatException ignored){}

				}
				if (value instanceof Value<?> val && !val.getUnit().isEmpty()) {
					setText(formatter.format(val.get()) + " " + val.getUnit());
				}

				if (value instanceof Number) {
					setText(formatter.format(value));
				}
				setText(String.valueOf(value));
			} catch (Exception e) {
				System.out.println("Failed to format: " + value + " " + value.getClass());
				e.printStackTrace();
			}
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!(rowIndexes.get(row) instanceof Value.Mutable<?>)) {

				setForeground(Color.LIGHT_GRAY);
			}
			return c;
		}
	}
}
