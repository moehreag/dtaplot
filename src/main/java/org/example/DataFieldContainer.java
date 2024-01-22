package org.example;

import java.util.Collection;
import java.util.List;

public interface DataFieldContainer {

	boolean isMulti();
	Collection<DataField<?>> get();

	default boolean isVoid(){
		return false;
	}

	static <T> DataFieldContainer single(String category, String name, T value){
		return single(category, name, DtaFile.Value.of(value));
	}

	static <T> DataFieldContainer single(String category, String name, DtaFile.Value<T> value){
		return single(new DataField<T>(category, name) {
			@Override
			public DtaFile.Value<T> getValue() {
				return value;
			}
		});
	}

	static DataFieldContainer single(DataField<?> field){
		return new DataFieldContainer() {
			@Override
			public boolean isMulti() {
				return false;
			}

			@Override
			public Collection<DataField<?>> get() {
				return List.of(field);
			}
		};
	}

	static DataFieldContainer multi(Collection<DataField<?>> fields){
		return new DataFieldContainer() {
			@Override
			public boolean isMulti() {
				return true;
			}

			@Override
			public Collection<DataField<?>> get() {
				return fields;
			}
		};
	}

	static DataFieldContainer empty(){
		return new DataFieldContainer() {
			@Override
			public boolean isMulti() {
				return false;
			}

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
