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
				return "Value(" + val.toString() + ")";
			}
		};
	}

	T get();
}
