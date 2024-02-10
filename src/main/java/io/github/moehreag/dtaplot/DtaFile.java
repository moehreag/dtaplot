package io.github.moehreag.dtaplot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import lombok.Getter;

public abstract class DtaFile {

	protected final ByteBuffer data;
	@Getter
	private final int version;
	@Getter
	private Collection<Map<String, Value<?>>> datapoints;

	public DtaFile(ByteBuffer data) {
		this.data = data.order(ByteOrder.LITTLE_ENDIAN);
		this.version = data.getInt(0);
	}

	protected void setDatapoints(Collection<Map<String, Value<?>>> entries){
		datapoints = Collections.unmodifiableCollection(entries);
	}

	protected boolean readBit(int i, int bit) {
		return readBit(i, bit, false);
	}

	protected boolean readBit(int i, int bit, boolean inverted) {
		if (Integer.bitCount(i) < bit) {
			throw new IllegalArgumentException("Integer does not contain bit at position " + bit);
		}
		int value = (i >> bit) & 1;
		return value == (inverted ? 0 : 1);
	}

	protected void skip(int i) {
		data.position(data.position() + i);
	}

	public static <A> Value<A> of(A val) {
		return Value.of(val);
	}

	public interface Value<T> {
		T get();

		static <A> Value<A> of(A val) {
			return new Value<>() {
				@Override
				public A get() {
					return val;
				}

				@Override
				public String toString() {
					return "Value(" + val.toString() + ")";
				}
			};
		}
	}
}
