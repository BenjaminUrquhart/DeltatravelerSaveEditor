package net.benjaminurquhart.dtsaveeditor.file;

public class ObjectNull extends Record {
	
	public static final ObjectNull NULL = new ObjectNull();

	public final int repeats;
	
	public ObjectNull() {
		this(1);
	}
	
	public ObjectNull(int repeats) {
		this.repeats = repeats;
	}
	
	@Override
	public String toString() {
		return (repeats > 1 ? "ObjectNull x " + repeats : "ObjectNull") + String.format(" @ 0x%08x", offset);
	}
}
