package io.github.moehreag.dtaplot.dta;

import java.util.Collection;
import java.util.List;

import io.github.moehreag.dtaplot.Value;

public interface DataFieldContainer {

	Collection<DataField<?>> get();

	default boolean isVoid(){
		return false;
	}

	static <T> DataFieldContainer single(String category, String name, T value){
		return single(category, name, Value.of(value));
	}

	static <T> DataFieldContainer single(String category, String name, Value<T> value){
		return single(new DataField<T>(category, name) {
			@Override
			public Value<T> getValue() {
				return value;
			}
		});
	}

	static DataFieldContainer single(DataField<?> field){
		return () -> List.of(field);
	}

	static DataFieldContainer multi(Collection<DataField<?>> fields){
		return () -> fields;
	}

	static DataFieldContainer empty(){
		return new DataFieldContainer() {

			@Override
			public Collection<DataField<?>> get() {
				return null;
			}

			@Override
			public boolean isVoid() {
				return true;
			}
		};
	}
}
