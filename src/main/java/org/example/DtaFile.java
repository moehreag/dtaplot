package org.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import lombok.Getter;
import org.example.unused.DtaFile8209Old;

public abstract class DtaFile {

	protected final ByteBuffer data;
	@Getter
	private final int version;
	@Getter
	protected Collection<Map<String, Value<?>>> datapoints = new ArrayList<>();

	public DtaFile(ByteBuffer data) {
		this.data = data.order(ByteOrder.LITTLE_ENDIAN);
		this.version = data.getInt(0);
	}

	protected boolean readBit(int i, int bit) {
		return readBit(i, bit, false);
	}

	protected boolean readBit(int i, int bit, boolean inverted) {
		if (Integer.bitCount(i) < bit) {
			throw new IllegalArgumentException("Integer does not contain bit at position " + bit);
		}
		int value = (i >> bit) & 1;;
		//System.out.printf("Bit %s of %s: %s%n", bit, i, value);
		return value == (inverted ? 0 : 1);
	}

	protected boolean[] readBits(int bits) {
		boolean[] b = new boolean[Integer.bitCount(bits)];
		for (int i = 0; i < b.length; i++) {
			b[i] = readBit(bits, i);
		}
		return b;
	}

	protected boolean[] readBits(int bits, int... invertedBits) {
		boolean[] b = new boolean[Integer.bitCount(bits)];
		Integer[] in = new Integer[invertedBits.length];
		for (int i = 0; i < invertedBits.length; i++) {
			in[i] = invertedBits[i];
		}
		Collection<Integer> inverted = Arrays.asList(in);
		for (int i = 0; i < b.length; i++) {
			b[i] = readBit(bits, i, inverted.contains(i));
		}
		return b;
	}

	protected int combine(short low, short high) {
		int i = high << 4;
		i += low;
		return i;
	}

	protected void skip(int i) {
		data.position(data.position() + i);
	}

	public static class Entry {

	}

	@Getter
	public enum Entries {
		STATUS_OUT("StatusA"),
		STATUS_IN("StatusE"),
		TEMP_FLOOR_1("TFB1"),
		TEMP_USE_WARM("TBW"),
		TEMP_OUTSIDE("TA"),
		TEMP_BACK_EXT("TRLext"),
		TEMP_BACK("TRL"),
		TEMP_FRONT("TVL"),
		TEMP_GAS("THG"),
		TEMP_SOURCE_OUT("TWQaus"),
		TEMP_SOURCE_IN("TWQein"),
		TEMP_BACK_TARGET("TRLsoll"),
		TEMP_MIX_C1_TARGET("TMK1soll"),
		COMFORT_PLATINE("ComfortPlatine"),
		CP_STATUS_OUT("StatusA_CP"),
		CP_ANALOG_OUT_1("AO1"),
		AP_ANALOG_OUT_2("AO2"),
		CP_STATUS_IN("StatusE_CP"),
		TEMP_SOLAR_STORAGE("TSS"),
		TEMP_SOLAR_COLLECTOR("TSK"),
		TEMP_FLOOR_2("TFB2"),
		TEMP_FLOOR_3("TFB3"),
		TEMP_EXTERN("TEE"),
		CP_ANALOG_IN_1("AI1"),
		TEMP_MIX_C2_TARGET("TMK2soll"),
		TEMP_MIX_C3_TARGEt("TMK3soll")
		;
		final String shortName;

		Entries(String shortName) {
			this.shortName = shortName;
		}
	}

	public static <A> DtaFile8209Old.Value<A> of(A val) {
		return Value.of(val);
	}

	public interface Value<T> {
		T get();

		static <A> DtaFile8209Old.Value<A> of(A val) {
			return new DtaFile8209Old.Value<>() {
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
