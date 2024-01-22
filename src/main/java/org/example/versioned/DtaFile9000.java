package org.example.versioned;

import java.nio.ByteBuffer;

import org.example.DtaFile;

public class DtaFile9000 extends DtaFile<DtaFile9000.Entry> {

	public DtaFile9000(ByteBuffer data) {
		super(data);
	}

	public static class Entry extends DtaFile.Entry {

	}
}
