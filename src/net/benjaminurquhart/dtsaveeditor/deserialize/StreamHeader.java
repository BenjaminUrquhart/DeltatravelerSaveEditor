package net.benjaminurquhart.dtsaveeditor.deserialize;

import java.util.Map;

@RecordType(Deserializer.RecordType.SerializedStreamHeader)
public class StreamHeader extends Record {

	public final int rootId, headerId, majorVersion, minorVersion;
	
	private Record rootObject;
	
	public StreamHeader(int rootId, int headerId, int majorVersion, int minorVersion) {
		this.minorVersion = minorVersion;
		this.majorVersion = majorVersion;
		this.headerId = headerId;
		this.rootId = rootId;
	}
	
	@Override
	protected void postProcess(Map<Integer, Record> objects) {
		rootObject = objects.get(rootId);
	}
	
	public Record getRootObject() {
		return rootObject;
	}
	
	public <T> T getRootObject(Class<T> clazz) throws InstantiationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return ((ClassWithMembersAndTypes)rootObject).getAs(clazz);
	}
}
