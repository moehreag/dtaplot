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
