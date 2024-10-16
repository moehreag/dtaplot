package io.github.moehreag.dtaplot.dta;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.github.moehreag.dtaplot.dta.versioned.DtaFile8209;
import io.github.moehreag.dtaplot.dta.versioned.DtaFile9000;
import io.github.moehreag.dtaplot.dta.versioned.DtaFile9001;
import io.github.moehreag.dtaplot.dta.versioned.DtaFile9003;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DtaParser {

	public static DtaFile get(ByteBuffer data){
		int version = data.order(ByteOrder.LITTLE_ENDIAN).getInt(0);
		return switch (version){
			case 8208, 8209 -> new DtaFile8209(data);
			case 9000 -> new DtaFile9000(data);
			case 9001 -> new DtaFile9001(data);
			case 9003 -> new DtaFile9003(data);
			default -> throw new IllegalArgumentException("Unsupported DTA version: "+version);
		};
	}

	public static DtaFile get(byte[] data){
		return get(ByteBuffer.wrap(data));
	}
}
