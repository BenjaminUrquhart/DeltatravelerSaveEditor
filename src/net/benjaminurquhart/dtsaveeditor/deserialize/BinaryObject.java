package net.benjaminurquhart.dtsaveeditor.deserialize;

import java.util.Map;

import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.RecordType;

public class BinaryObject extends Record {

	public final int id;
	public final Object value;
	
	public BinaryObject(RecordType type, int id, Object value) {
		super(type);
		this.value = value;
		this.id = id;
	}
	
	@Override
	protected void preProcess(Map<Integer, Record> objects) {
		if(id > -1) {
			objects.put(id, this);
		}
	}
}
