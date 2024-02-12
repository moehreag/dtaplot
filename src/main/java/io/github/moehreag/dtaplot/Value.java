package io.github.moehreag.dtaplot;

public interface Value<T> {
	static <A> Value<A> of(A val) {
		return new Value<>() {
			@Override
			public A get() {
				return val;
			}

			@Override
			public String toString() {
				return "Value(" + get() + ")";
			}
		};
	}

	static <A> Value<A> of (A val, String unit){
		return new Value<>() {
			@Override
			public A get() {
				return val;
			}

			@Override
			public String getUnit() {
				return unit;
			}

			@Override
			public String toString() {
				return "Value(" + get() + (!getUnit().isEmpty() ? " " + getUnit() : "") + ")";
			}
		};
	}

	T get();

	default String getUnit() {
		return "";
	}

	interface Mutable<T> extends Value<T> {
		void set(T value);
	}
}
