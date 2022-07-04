package net.benjaminurquhart.dtsaveeditor.deserialize;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public abstract class Record {

	public final Deserializer.RecordType type;
	
	public Record() {
		if(this.getClass().isAnnotationPresent(RecordType.class)) {
			type = this.getClass().getAnnotation(RecordType.class).value();
			return;
		}
		else {
			for(Deserializer.RecordType type : Deserializer.RecordType.values()) {
				if(type.name().equalsIgnoreCase(this.getClass().getSimpleName())) {
					this.type = type;
					return;
				}
			}
		}
		throw new IllegalStateException("Could not automatically find type for " + this.getClass());
	}
	
	public Record(Deserializer.RecordType type) {
		this.type = type;
	}
	
	protected void preProcess(Map<Integer, Record> objects) {
		
	}
	protected void process(Map<Integer, Record> objects) {
		
	}
	protected void postProcess(Map<Integer, Record> objects) {
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(type.name());
		sb.append(" [");
		
		boolean space = false;
		for(Field field : this.getClass().getDeclaredFields()) {
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
