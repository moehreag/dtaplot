package io.github.moehreag.dtaplot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Pair<A, B> {

	private final A left;
	private final B right;

	public static <A, B> Pair<A, B> of(A left, B right){
		return new Pair<>(left, right);
	}
}
