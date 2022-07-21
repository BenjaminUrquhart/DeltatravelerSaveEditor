package net.benjaminurquhart.dtsaveeditor.file;

import java.util.Map;

import net.benjaminurquhart.dtsaveeditor.file.Deserializer.Reader;

public abstract class ArraySingle extends Record {

	public final int id, length;
	public final int[] offsets;
	
	public ArraySingle(Reader reader) {
		this.id = reader.buff.getInt();
		this.length = reader.buff.getInt();
		this.offsets = new int[this.length];
	}
	
	protected void preProcess(Map<Integer, Record> objects) {
		objects.put(id, this);
	}
	
	public abstract Object[] getValues();
}
