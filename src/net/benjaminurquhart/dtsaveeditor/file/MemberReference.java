package net.benjaminurquhart.dtsaveeditor.file;

import java.util.Map;

public class MemberReference extends Record {

	public final int refId;
	private Record reference;
	
	public MemberReference(int refId) {
		this.refId = refId;
	}
	
	@Override
	public void postProcess(Map<Integer, Record> objects) {
		reference = objects.get(refId);
	}
	
	public Record getReference() {
		return reference;
	}
	
	public <T> T getReference(Class<T> clazz) {
		return clazz.cast(reference);
	}

	@Override
	protected void serializeInternal(Writer writer) {
		writer.writeInt(refId);
	}
}
