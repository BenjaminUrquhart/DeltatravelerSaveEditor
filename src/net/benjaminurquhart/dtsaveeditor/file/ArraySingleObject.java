package net.benjaminurquhart.dtsaveeditor.file;

public class ArraySingleObject extends ArraySingle {

	public final Object[] values;
	public ArraySingleObject(Reader reader) {
		super(reader);
		this.values = new Object[length];
		
		Record record;
		for(int i = 0; i < length; i++) {
			record = reader.readRecord();
			offsets[i] = record.offset;
			if(record instanceof ObjectNull nullObj) {
				for(int j = 0; j < nullObj.repeats; j++, i++) {
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
	
	@Override
	protected void serializeInternal(Writer writer) {
		super.serializeInternal(writer);
		
		int nullCount = 0;
		for(Object obj : values) {
			if(obj == null) {
				nullCount++;
				continue;
			}
			else if(nullCount > 0) {
				writer.writeNull(nullCount);
				nullCount = 0;
			}
			writer.writeRecord((Record)obj);
		}
	}
}
