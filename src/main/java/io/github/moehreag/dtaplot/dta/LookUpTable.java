package io.github.moehreag.dtaplot.dta;

import lombok.Data;

@Data
public class LookUpTable {
	private final int[] data;  // y-data points
	private final int offset;     // offset of data points
	private final int delta;      // x-distance of datapoints
	private final int precision;
}
