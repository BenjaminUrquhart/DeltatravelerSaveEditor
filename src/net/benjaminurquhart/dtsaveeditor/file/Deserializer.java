package net.benjaminurquhart.dtsaveeditor.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deserializer {
	
	static class DeserializationException extends RuntimeException {
		
		private static final long serialVersionUID = -6206411312435090662L;
		
		protected DeserializationException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class Reader {
		
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
					int rootId = buff.getInt();
					int headerId = buff.getInt();
					int majorVersion = buff.getInt();
					int minorVersion = buff.getInt();
					return new StreamHeader(rootId, headerId, majorVersion, minorVersion);
				case BinaryLibrary:
					int libraryId = buff.getInt();
					String libraryName = this.readString();
					return new BinaryLibrary(libraryId, libraryName);
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
					return new ObjectNull(buff.get());
				case ObjectNullMultiple:
					return new ObjectNull(buff.getInt());
				case ObjectNull:
					return new ObjectNull();
				
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
			BinaryObject obj = this.readBinaryObjectInternal();
			records.add(obj);
			return obj;
		}
		
		private BinaryObject readBinaryObjectInternal() {
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
	}
	
	public static StreamHeader deserialize(File file) throws IOException {
		Reader reader = new Reader(Files.readAllBytes(file.toPath()));
		Map<Integer, Record> objects = reader.objects;
		List<Record> records = reader.records;
		
		RecordType type = RecordType.values()[reader.buff.get(0)];
		
		if(type != RecordType.SerializedStreamHeader) {
			throw new IllegalStateException("Expected SerializedStreamHeader, got " + type);
		}
		
		Record record = reader.readRecord();
		StreamHeader header = (StreamHeader)record;
		
		while(record != null) {
			//System.out.println(record);
			record = reader.readRecord();
		}
		
		records.forEach(r ->  r.preProcess(objects));
		records.forEach(r ->     r.process(objects));
		records.forEach(r -> r.postProcess(objects));
		
		return header;
	}
}
