package net.benjaminurquhart.dtsaveeditor.deserialize;

import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.PrimitiveType;
import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.Reader;

public class MemberPrimitiveTyped extends Record {

	public final PrimitiveType primitiveType;
	public final Object value;
	
	public MemberPrimitiveTyped(Reader reader) {
		this.primitiveType = PrimitiveType.values()[reader.buff.get()];
		this.value = reader.readPrimitive(primitiveType);
	}
}
