package org.example;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public abstract class DataField<T> {

	private final String category, name;

	public abstract DtaFile.Value<T> getValue();

	public boolean isNumeric(){
		return true;
	}

	public static DataFieldContainer time(ByteBuffer buffer){
		int time = buffer.getInt();
		return DataFieldContainer.single("", "time", time);
	}


	public static DataFieldContainer lut(LookUpTable table, String category, String name, ByteBuffer buffer){
		int value = buffer.getShort();

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
		return DataFieldContainer.single(category, name, val);
	}

	public static DataFieldContainer analogue(String category, String name, ByteBuffer buffer){
		return analogue(category, name, 10, 10, buffer);
	}

	public static DataFieldContainer analogue(String category, String name, double factor, int precision, ByteBuffer buffer){
		return analogue(category, name, factor, precision, buffer, false);
	}

	public static DataFieldContainer analogue(String category, String name, double factor, int precision, ByteBuffer buffer, boolean highbytes){

		int val = highbytes ? buffer.getInt() : buffer.getShort();
		double res = (double) Math.round(val / factor * precision) / precision;

		DtaFile.Value<Number> value = DtaFile.Value.of(res);
		return DataFieldContainer.single(category, name, value);
	}

	public static DataFieldContainer digital(String category, ByteBuffer buffer, DataFieldBit... bits){
		int val = buffer.getShort();

		List<DataField<Boolean>> fields = new ArrayList<>();
		for (DataFieldBit b : bits) {
			/*if (Integer.bitCount(val) < b.bit) {
				throw new IllegalArgumentException("Integer "+val+" does not contain bit at position " + b.bit+" (Only "+Integer.bitCount(val)+" bits)");
			}*/
			int value = (val >> b.bit) & 1;

			//System.out.printf("Bit %s of %s: %s%n", bit, i, value);
			fields.add(new DataField<>(category, b.name) {
				@Override
				public DtaFile.Value<Boolean> getValue() {
					return DtaFile.Value.of(value == (b.inverted ? 0 : 1));
				}

				@Override
				public boolean isNumeric() {
					return false;
				}
			});
		}

		return DataFieldContainer.multi(Collections.unmodifiableCollection(fields));
	}

	public static DataFieldBit bit(String name, int bit){
		return new DataFieldBit(name, bit, false);
	}

	public static DataFieldBit bit(String name, int bit, boolean inverted){
		return new DataFieldBit(name, bit, inverted);
	}

	public static DataFieldContainer unknown(int count, ByteBuffer buf){
		buf.position(buf.position()+count);
		return DataFieldContainer.empty();
	}

	@Data
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class DataFieldBit {
		private final String name;
		private final int bit;
		private final boolean inverted;

	}
}
