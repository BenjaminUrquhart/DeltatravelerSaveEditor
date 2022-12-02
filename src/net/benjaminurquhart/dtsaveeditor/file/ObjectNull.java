package net.benjaminurquhart.dtsaveeditor.file;

import java.util.HashMap;
import java.util.Map;

public class ObjectNull extends Record {
	
	public static boolean DO_POOLING = false;
	
	private static final ObjectNull NULL = new ObjectNull();
	private static final Map<Integer, ObjectNull> nulls = new HashMap<>();
	
	public static ObjectNull get() {
		return DO_POOLING ? NULL : new ObjectNull();
	}
	
	public static ObjectNull get(int repeats) {
		if(repeats < 2) {
			return get();
		}
		return DO_POOLING ? nulls.computeIfAbsent(repeats, ObjectNull::new) : new ObjectNull(repeats);
	}
	
	public final int repeats;
	
	private ObjectNull() {
		this(1);
	}
	
	private ObjectNull(int repeats) {
		this.repeats = repeats;
	}
	
	@Override
	public int hashCode() {
		return repeats;
	}
	
	@Override
	public String toString() {
		return (repeats > 1 ? "ObjectNull x " + repeats : "ObjectNull") + String.format(" @ 0x%08x", offset);
	}
	
	@Override
	public RecordType getType() {
		if(repeats > 1) {
			return repeats < 256 ? RecordType.ObjectNullMultiple256 : RecordType.ObjectNullMultiple;
		}
		return RecordType.ObjectNull;
	}

	@Override
	protected void serializeInternal(Writer writer) {
		if(repeats > 1) {
			if(repeats < 256) {
				writer.write((byte)repeats);
			}
			else {
				writer.writeInt(repeats);
			}
		}
	}
}
