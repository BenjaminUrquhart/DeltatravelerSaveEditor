package net.benjaminurquhart.dtsaveeditor.file;

public class ArraySingleString extends ArraySingle {

	public final String[] values;
	public ArraySingleString(Reader reader) {
		super(reader);
		this.values = new String[length];
		
		for(int i = 0; i < length; i++) {
			offsets[i] = reader.buff.position();
			values[i] = reader.readString();
		}
	}
	@Override
	public String[] getValues() {
		return values;
	}
	@Override
	public void serializeInternal(Writer writer) {
		super.serializeInternal(writer);
		for(String s : values) {
			writer.writeString(s);
		}
	}

}
