package net.benjaminurquhart.dtsaveeditor.file;

import java.util.Map;

public class BinaryLibrary extends Record {

	public final int libraryId;
	public final String libraryName;
	
	public BinaryLibrary(int libraryId, String libraryName) {
		this.libraryName = libraryName;
		this.libraryId = libraryId;
	}
	
	@Override
	protected void preProcess(Map<Integer, Record> objects) {
		objects.put(libraryId, this);
	}
}
