package io.github.moehreag.dtaplot.socket.tcp;

import java.util.function.Function;

import io.github.moehreag.dtaplot.ArrayUtil;
import io.github.moehreag.dtaplot.Value;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public abstract class Datatype {

	private final String name;
	private String unit = "";

	public Datatype(String name) {
		this.name = name;
	}

	public Datatype unit(String unit){
		this.unit = unit;
		return this;
	}

	public abstract Value<?> read(int value);

	public abstract int write(Value<?> val);

	private static <T> Value<T> of(T val, boolean writeable, String unit){

		if (writeable) {
			return new Value.Mutable<>() {

				private T value = val;

				@Override
				public void set(T value) {
					this.value = value;
				}

				@Override
				public T get() {
					return value;
				}

				@Override
				public String getUnit() {
					return unit;
				}

				@Override
				public String toString() {
					return "Datatype:Value.Mutable(" + get() + (!getUnit().isEmpty() ? " " + getUnit() : "") + ")";
				}
			};
		}
		return Value.of(val, unit);
	}

	public static Datatype selection(String name, boolean writeable, String... codes){
		return new Datatype(name) {
			@Override
			public Value<?> read(int value) {
				return of(codes[value], writeable, getUnit());
			}

			@Override
			public int write(Value<?> val) {
				return ArrayUtil.indexOf(codes, val.get());
			}
		};
	}

	public static Datatype scaling(String name, boolean writeable, float scale){
		return new Datatype(name) {
			@Override
			public Value<?> read(int value) {
				return of(value*scale, writeable, getUnit());
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
		return new Datatype(name) {
			@Override
			public Value<?> read(int value) {
				return of(value != 0, writeable, getUnit());
			}

			@Override
			public int write(Value<?> val) {
				return val.get().equals(Boolean.TRUE) ? 1 : 0;
			}
		};
	}

	public static Datatype base(String name, boolean writeable){
		return new Datatype(name) {
			@Override
			public Value<?> read(int value) {
				return of(value, writeable, getUnit());
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

	public static Datatype custom(String name, boolean writeable, Function<Integer, ?> readF, Function<Value<?>, Integer> writeF){
		return new Datatype(name) {
			@Override
			public Value<?> read(int value) {
				return of(readF.apply(value), writeable, getUnit());
			}

			@Override
			public int write(Value<?> val) {
				return writeF.apply(val);
			}
		};
	}

	public static Datatype custom(String name, Function<Integer, ?> readF){
		return new Datatype(name) {
			@Override
			public Value<?> read(int value) {
				return of(readF.apply(value), false, getUnit());
			}

			@Override
			public int write(Value<?> val) {
				return -1;
			}
		};
	}
}
