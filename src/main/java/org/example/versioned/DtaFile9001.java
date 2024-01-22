package org.example.versioned;

import java.nio.ByteBuffer;

import org.example.DtaFile;

public class DtaFile9001 extends DtaFile {

	public DtaFile9001(ByteBuffer data) {
		super(data);
		throw new UnsupportedOperationException("File format not implemented yet");
	}

	public static class Entry extends DtaFile.Entry {

	}
}
