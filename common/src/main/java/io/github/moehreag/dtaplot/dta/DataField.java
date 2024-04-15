package io.github.moehreag.dtaplot.dta;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.moehreag.dtaplot.Value;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public abstract class DataField<T> {

	private final String category, name;

	public abstract Value<T> getValue();

	public boolean isNumeric(){
		return true;
	}

	public static DataFieldContainer time(ByteBuffer buffer){
		int time = buffer.getInt();
		return DataFieldContainer.single("", "time", time);
	}


	public static DataFieldContainer digital(String category, ByteBuffer buffer, DataFieldBit... bits){
		int val = buffer.getShort();

		List<DataField<Boolean>> fields = new ArrayList<>();
		for (DataFieldBit b : bits) {
			int value = (val >> b.bit) & 1;

			fields.add(new DataField<>(category, b.name) {
				@Override
				public Value<Boolean> getValue() {
					return Value.of(value == (b.inverted ? 0 : 1));
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
