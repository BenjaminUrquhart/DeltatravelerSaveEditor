package net.benjaminurquhart.dtsaveeditor.file;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.benjaminurquhart.dtsaveeditor.file.Deserializer.DeserializationException;

public class Reader {
	
	public final ByteBuffer buff;
	
	protected Map<Integer, Record> objects;
	protected List<Record> records;
	
	public Reader(byte[] bytes) {
		buff = ByteBuffer.wrap(bytes);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		this.records = new ArrayList<>();
		this.objects = new HashMap<>();
	}
	
	public Record readRecord() {
		int offset = buff.position();
		Record record = this.readRecordInternal();
		if(record != null) {
			records.add(record);
			record.offset = offset;
			//System.out.println(record);
		}
		return record;
	}
	
	private Record readRecordInternal() {
		if(!buff.hasRemaining()) {
			return null;
		}
		
		int pos = buff.position();
		RecordType type = null;
	
		try {
			type = RecordType.values()[buff.get()];
			switch(type) {
			
			case SerializedStreamHeader:
				return new StreamHeader(buff.getInt(), buff.getInt(), buff.getInt(), buff.getInt());
			case BinaryLibrary:
				return new BinaryLibrary(buff.getInt(), this.readString());
			case SystemClassWithMembersAndTypes:
				return new SystemClassWithMembersAndTypes(this);
			case ClassWithMembersAndTypes:
				return new ClassWithMembersAndTypes(this);
			case MemberReference:
				return new MemberReference(buff.getInt());
			
			case BinaryObjectString:
				return new BinaryObject(type, buff.getInt(), this.readString());
			
			case ArraySinglePrimitive:
				return new ArraySinglePrimitive(this);
			case ArraySingleObject:
				return new ArraySingleObject(this);
			case ArraySingleString:
				return new ArraySingleString(this);
			
			case MemberPrimitiveTyped:
				return new MemberPrimitiveTyped(this);
			
			case ObjectNullMultiple256:
				return ObjectNull.get(buff.get());
			case ObjectNullMultiple:
				return ObjectNull.get(buff.getInt());
			case ObjectNull:
				return ObjectNull.get();
			
			case MessageEnd:
				break;
				
			default:
				throw new UnsupportedOperationException("Cannot deserialize record of type " + type);
			}
		}
		catch(Throwable e) {
			int currPos = buff.position();
			
			throw new DeserializationException(String.format(
					"\nException while processing %s at position 0x%08x (error occured around 0x%08x)",
					type == null ? "<unknown type>" : type,
					pos,
					currPos
			), e);
		}
		
		return null;
	}
	
	public BinaryObject readBinaryObject() {
		return (BinaryObject) this.readRecord();
	}
	
	public Object readPrimitive(PrimitiveType type) {
		Object obj;
		switch(type) {
		case Boolean: obj = buff.get() == 1; break;
		case Byte:    obj = buff.get(); break;
		case Char:    obj = buff.getChar(); break;
		case Int16:   obj = buff.getShort(); break;
		case Int32:   obj = buff.getInt(); break;
		case Int64:   obj = buff.getLong(); break;
		case UInt16:  obj = buff.getShort() & 0xffff; break;
		case UInt32:  obj = buff.getInt() & 0xffffffffL; break;
		
		case Single:  obj = buff.getFloat(); break;
		case Double:  obj = buff.getDouble(); break;
		default:      throw new UnsupportedOperationException("Unsupported PrimitiveType " + type);
		}
		return obj;
	}
	
	public String readString() {
		int len = readStringLength();
		byte[] bytes = new byte[len];
		
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = buff.get();
		}
		
		return new String(bytes);
	}
	
	// I get it saves space but it's also painful
	private int readStringLength() {
		int out = 0;
		int shift = 0;
		
		boolean highBit = false;
		byte b;
		do {
			b = buff.get();
			highBit = (b & 0x80) > 0;
			out |= (b & 0x7f) << shift;
			shift += 7;
		} while(highBit && shift < 32);
		
		if((highBit && shift >= 32) || out < 0) {
			throw new IllegalStateException("Invalid string length field");
		}
		
		return out;
	}

	public void finish() {
		records.forEach(r ->  r.preProcess(objects));
		records.forEach(r ->     r.process(objects));
		records.forEach(r -> r.postProcess(objects));
	}
}
