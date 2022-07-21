package net.benjaminurquhart.dtsaveeditor.file;

import net.benjaminurquhart.dtsaveeditor.file.Deserializer.Reader;

public class ArraySingleObject extends ArraySingle {

	public final Object[] values;
	public ArraySingleObject(Reader reader) {
		super(reader);
		this.values = new Object[length];
		
		Record record;
		for(int i = 0; i < length; i++) {
			record = reader.readRecord();
			offsets[i] = record.offset;
			if(record.type == RecordType.ObjectNull) {
				for(int j = 0, count = ((ObjectNull)record).repeats; j < count; j++, i++) {
					offsets[i] = record.offset;
					values[i] = null;
				}
				i--;
			}
			else {
				values[i] = record;
			}
		}
	}
	@Override
	public Object[] getValues() {
		return values;
	}

}
