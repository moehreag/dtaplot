package org.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.RequiredArgsConstructor;
import org.example.versioned.DtaFile8209;
import org.example.versioned.DtaFile9000;
import org.example.versioned.DtaFile9001;
import org.example.versioned.DtaFile9003;

@RequiredArgsConstructor
public class DtaParser {

	private final ByteBuffer data;

	public static DtaParser of(ByteBuffer buf){
		return new DtaParser(buf.order(ByteOrder.LITTLE_ENDIAN));
	}

	public DtaFile<?> parse(){
		int version = data.getInt(0);
		return switch (version){
			case 8208, 8209 -> new DtaFile8209(data);
			case 9000 -> new DtaFile9000(data);
			case 9001 -> new DtaFile9001(data);
			case 9003 -> new DtaFile9003(data);
			default -> throw new IllegalArgumentException("Unsupported DTA version: "+version);
		};
	}
}
