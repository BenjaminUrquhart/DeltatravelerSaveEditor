package net.benjaminurquhart.dtsaveeditor.file;

public class MemberPrimitiveTyped extends Record {

	public final PrimitiveType primitiveType;
	public final Object value;
	
	public MemberPrimitiveTyped(Reader reader) {
		this.primitiveType = PrimitiveType.values()[reader.buff.get()];
		this.value = reader.readPrimitive(primitiveType);
	}

	@Override
	protected void serializeInternal(Writer writer) {
		writer.writePrimitive(primitiveType, value);
	}
}
