 package net.benjaminurquhart.dtsaveeditor.file;

import java.util.Map;

@BinaryRecordType(RecordType.SerializedStreamHeader)
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
		if(clazz.isInstance(rootObject)) {
			return clazz.cast(rootObject);
		}
		return ((ClassWithMembersAndTypes)rootObject).getAs(clazz);
	}

	@Override
	protected void serializeInternal(Writer writer) {
		writer.writeInt(rootId)
			  .writeInt(headerId)
			  .writeInt(majorVersion)
			  .writeInt(minorVersion);
	}
}
