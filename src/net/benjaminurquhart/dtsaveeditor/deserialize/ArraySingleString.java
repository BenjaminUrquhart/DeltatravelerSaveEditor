package net.benjaminurquhart.dtsaveeditor.deserialize;

import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.Reader;

public class ArraySingleString extends ArraySingle {

	public final String[] values;
	public ArraySingleString(Reader reader) {
		super(reader);
		this.values = new String[length];
		
		for(int i = 0; i < length; i++) {
			values[i] = reader.readString();
		}
	}
	@Override
	public String[] getValues() {
		return values;
	}

}
