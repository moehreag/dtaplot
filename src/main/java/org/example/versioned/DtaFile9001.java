package org.example.versioned;

import java.nio.ByteBuffer;

import org.example.DtaFile;

public class DtaFile9001 extends DtaFile<DtaFile9001.Entry> {

	public DtaFile9001(ByteBuffer data) {
		super(data);
	}

	public static class Entry extends DtaFile.Entry {

	}
}
