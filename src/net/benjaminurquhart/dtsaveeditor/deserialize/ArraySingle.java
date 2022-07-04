package net.benjaminurquhart.dtsaveeditor.deserialize;

import java.util.Map;

import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.Reader;

public abstract class ArraySingle extends Record {

	public final int id, length;
	
	public ArraySingle(Reader reader) {
		this.id = reader.buff.getInt();
		this.length = reader.buff.getInt();
	}
	
	protected void preProcess(Map<Integer, Record> objects) {
		objects.put(id, this);
	}
	
	public abstract Object[] getValues();
}
