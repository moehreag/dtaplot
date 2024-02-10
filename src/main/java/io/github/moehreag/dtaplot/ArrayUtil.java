package io.github.moehreag.dtaplot;

public class ArrayUtil {
	public static <T> int indexOf(T[] array, Object element) {
		for (int i = 0; i < array.length; i++) {
			if (element.equals(array[i])) {
				return i;
			}
		}

		return -1;
	}
}
