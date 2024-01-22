package org.example;

import java.nio.ByteBuffer;

import lombok.Data;

@Data
public abstract class DataField<T> {

	private final String category, name;

	public abstract DtaFile.Value<T> getValue();

	public static DataField<Number> lut(LookUpTable table, String category, String name, ByteBuffer buffer){
		short value = buffer.getShort();

		// Position in Tabelle
		int idx = (value - table.getOffset()) / table.getDelta();
		int size = (table.getData().length*4) / 2;
		if (idx > (size - 2)) idx = size - 2;

		// linear approximation
		int x1 = idx * table.getDelta() + table.getOffset();
		int x2 = (idx + 1) * table.getDelta() + table.getOffset();
		int y1 = table.getData()[idx];
		int y2 = table.getData()[idx + 1];

		double m = (float)(y2 - y1) / (x2 - x1);
		double n = y1 - m * x1;

		// calc value
		double res = m * value + n;
		DtaFile.Value<Number> val = DtaFile.Value.of(Math.round(res) / (table.getPrecision()));
		return new DataField<>(category, name) {

			@Override
			public DtaFile.Value<Number> getValue() {
				return val;
			}
		};
	}

	public static DataField<Number> analogue(String category, String name, ByteBuffer buffer){
		return analogue(category, name, 10, 10, buffer);
	}

	public static DataField<Number> analogue(String category, String name, double factor, int precision, ByteBuffer buffer){

		int val = buffer.getInt();
		double res = (double) Math.round(val / factor * precision) / precision;

		DtaFile.Value<Number> value = DtaFile.Value.of(res);
		return new DataField<>(category, name) {
			@Override
			public DtaFile.Value<Number> getValue() {
				return value;
			}
		};
	}

}
