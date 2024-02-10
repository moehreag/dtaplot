package io.github.moehreag.dtaplot.socket.tcp;

import java.util.function.Function;

import io.github.moehreag.dtaplot.ArrayUtil;
import io.github.moehreag.dtaplot.Value;
import lombok.Getter;

@Getter
public abstract class Datatype {

	private final String name;
	private final boolean writeable;
	private String unit = "";

	public Datatype(String name, boolean writeable) {
		this.name = name;
		this.writeable = writeable;
	}

	public Datatype unit(String unit){
		this.unit = unit;
		return this;
	}

	public abstract Value<?> read(int value);

	public abstract int write(Value<?> val);

	public static Datatype selection(String name, boolean writeable, String... codes){
		return new Datatype(name, writeable) {
			@Override
			public Value<?> read(int value) {
				return Value.of(codes[value]);
			}

			@Override
			public int write(Value<?> val) {
				return ArrayUtil.indexOf(codes, val.get());
			}
		};
	}

	public static Datatype scaling(String name, boolean writeable, float scale){
		return new Datatype(name, writeable) {
			@Override
			public Value<?> read(int value) {
				return Value.of(value*scale);
			}

			@Override
			public int write(Value<?> val) {
				if (val.get() instanceof Number num) {
					return Math.round(num.floatValue() / scale);
				}
				return -1;
			}
		};
	}

	public static Datatype bool(String name, boolean writeable){
		return new Datatype(name, writeable) {
			@Override
			public Value<?> read(int value) {
				return Value.of(value != 0);
			}

			@Override
			public int write(Value<?> val) {
				return val.get().equals(Boolean.TRUE) ? 1 : 0;
			}
		};
	}

	public static Datatype base(String name, boolean writeable){
		return new Datatype(name, writeable) {
			@Override
			public Value<?> read(int value) {
				return Value.of(value);
			}

			@Override
			public int write(Value<?> val) {
				if (val.get() instanceof Number num){
					return num.intValue();
				}
				return -1;
			}
		};
	}

	public static Datatype custom(String name, boolean writeable, Function<Integer, Value<?>> readF, Function<Value<?>, Integer> writeF){
		return new Datatype(name, writeable) {
			@Override
			public Value<?> read(int value) {
				return readF.apply(value);
			}

			@Override
			public int write(Value<?> val) {
				return writeF.apply(val);
			}
		};
	}

	public static Datatype custom(String name, Function<Integer, Value<?>> readF){
		return new Datatype(name, false) {
			@Override
			public Value<?> read(int value) {
				return readF.apply(value);
			}

			@Override
			public int write(Value<?> val) {
				return -1;
			}
		};
	}
}
