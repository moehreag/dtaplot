package org.example.versioned;

import java.nio.ByteBuffer;

import org.example.DtaFile;

public class DtaFile9003 extends DtaFile<DtaFile9003.Entry> {

	public DtaFile9003(ByteBuffer data) {
		super(data);
	}

	public static class Entry extends DtaFile.Entry {

	}
}
