package net.benjaminurquhart.dtsaveeditor.file;

import java.util.Arrays;

public class BinaryMember {
	
	public BinaryType type;
	public Object value;
	
	public BinaryMember(BinaryType type, Object value) {
		this.value = value;
		this.type = type;
	}
	
	private String asPrimitiveArrayString() {
		try {
			return (String)Arrays.class.getMethod("toString", value.getClass()).invoke(null, value);
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		if(value != null) {
			if(type == BinaryType.PrimitiveArray) {
				return String.format("%s %s", type, this.asPrimitiveArrayString());
			}
			else if(type.name().contains("Array") && value.getClass().isArray()) {
				return String.format("%s %s", type, Arrays.deepToString((Object[])value));
			}
		}
		return String.format("%s [%s]", type, value);
	}
}
