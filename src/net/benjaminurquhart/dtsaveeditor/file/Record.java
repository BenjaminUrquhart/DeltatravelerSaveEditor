package net.benjaminurquhart.dtsaveeditor.file;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public abstract class Record {

	private final RecordType type;
	public int offset = -1;
	
	public Record() {
		if(this.getClass().isAnnotationPresent(BinaryRecordType.class)) {
			type = this.getClass().getAnnotation(BinaryRecordType.class).value();
			return;
		}
		else {
			for(RecordType type : RecordType.values()) {
				if(type.name().equalsIgnoreCase(this.getClass().getSimpleName())) {
					this.type = type;
					return;
				}
			}
		}
		throw new IllegalStateException("Could not automatically find type for " + this.getClass());
	}
	
	public Record(RecordType type) {
		this.type = type;
	}
	
	public RecordType getType() {
		return type;
	}
	
	protected void preProcess(Map<Integer, Record> objects) {
		
	}
	protected void process(Map<Integer, Record> objects) {
		
	}
	protected void postProcess(Map<Integer, Record> objects) {
		
	}
	
	public final void serialize(Writer writer) {
		writer.write(this.getType());
		this.serializeInternal(writer);
	}
	protected abstract void serializeInternal(Writer writer);
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(type.name());
		sb.append(String.format(" @ 0x%08x [", offset));
		
		boolean space = false;
		for(Field field : this.getClass().getDeclaredFields()) {
			if(field.getName().startsWith("$SWITCH_TABLE$")) { // idk what this is
				continue;
			}
			if(space) {
				sb.append(", ");
			}
			sb.append(field.getName());
			sb.append('=');
			
			try {
				field.setAccessible(true);
				if(field.getType().isArray()) {
					sb.append(Arrays.deepToString((Object[])field.get(this)));
				}
				else {
					sb.append(String.valueOf(field.get(this)));
				}
			}
			catch(Exception e) {
				sb.append("ERR");
			}
			space = true;
		}
		
		sb.append(']');
		return sb.toString();
	}
}
