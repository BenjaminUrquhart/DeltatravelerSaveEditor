package net.benjaminurquhart.dtsaveeditor.file;

public class ArraySinglePrimitive extends ArraySingle {
	
	public final PrimitiveType primitiveType;
	public final Object[] values;

	public ArraySinglePrimitive(Reader reader) {
		super(reader);
		this.values = new Object[length];
		this.primitiveType = PrimitiveType.values()[reader.buff.get()];
		
		for(int i = 0; i < length; i++) {
			offsets[i] = reader.buff.position();
			values[i] = reader.readPrimitive(primitiveType);
		}
	}

	@Override
	public Object[] getValues() {
		return values;
	}
	
	@Override
	protected void serializeInternal(Writer writer) {
		super.serializeInternal(writer);
		writer.write(primitiveType);
		for(Object obj : values) {
			writer.writePrimitive(primitiveType, obj, false);
		}
	}
}
