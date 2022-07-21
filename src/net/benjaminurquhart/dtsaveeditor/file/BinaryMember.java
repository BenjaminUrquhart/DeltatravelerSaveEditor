package net.benjaminurquhart.dtsaveeditor.file;

import java.util.Arrays;

public class BinaryMember {
	
	public BinaryType type;
	public Object value;
	
	public BinaryMember(BinaryType type, Object value) {
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String toString() {
		if(value != null && type.name().contains("Array") && value.getClass().isArray()) {
			return String.format("%s %s", type, Arrays.deepToString((Object[])value));
		}
		return String.format("%s [%s]", type, value);
	}
}
