package net.benjaminurquhart.dtsaveeditor.file;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Writer {
	
	private ByteBuffer buff;
	private List<Record> records;
	private List<String> strings;
	
	public Writer() {
		this(1024);
	}
	
	public Writer(int initialSize) {
		strings = new ArrayList<>();
		records = new ArrayList<>();
		buff = ByteBuffer.allocate(initialSize);
		buff.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	private void expandBuffer(int requested) {
		if(buff.capacity() == Integer.MAX_VALUE) {
			throw new BufferOverflowException();
		}
		
		int newSize = Math.max(requested, (int)(buff.capacity() * 1.5));
		if(newSize < 0) {
			newSize = Integer.MAX_VALUE;
		}
		
		ByteBuffer newBuff = ByteBuffer.allocate(newSize);
		newBuff.order(ByteOrder.LITTLE_ENDIAN);
		
		buff.flip();
		newBuff.put(buff);
		
		buff = newBuff;
	}
	
	private void ensureSpaceFor(int size) {
		int requested = buff.position() + size;
		
		if(size < 0) {
			throw new IllegalArgumentException(size + " < 0");
		}
		if(requested < 0) {
			throw new BufferOverflowException();
		}
		
		if(requested > buff.capacity()) {
			expandBuffer(requested);
		}
	}
	
	// This wasn't as bad as I thought it would be
	public static byte[] compressStringLength(int length) {
		if(length < 0) {
			throw new IllegalArgumentException(length + " < 0");
		}
		if(length > 0xfffffff) {
			// Length must fit into 28 bits (4 groups of 7)
			throw new IllegalArgumentException("String too long: " + length + " > " + 0xfffffff);
		}
		if(length < 0x80) {
			// Shortcut, length fits into 7 bits
			return new byte[] {(byte)length};
		}
		
		byte[] bytes = new byte[4];
		int pos = 0;
		
		// Split length into 7-bit chunks
		while(length > 0 && pos < 4) {
			bytes[pos] = (byte)(length & 0x7f);
			length >>= 7;
			pos++;
		}
		
		if(length > 0) {
			// Sanity
			throw new IllegalArgumentException("String too long");
		}
		
		// Eighth bit is used to signify
		// that the next byte should be read
		// to continue parsing the length.
		for(int i = 0; i < pos - 1; i++) {
			bytes[i] |= 0x80;
		}
		
		// Trim array
		return Arrays.copyOfRange(bytes, 0, pos);
	}
	
	public Writer write(byte b) {
		ensureSpaceFor(1);
		buff.put(b);
		
		return this;
	}
	
	public Writer write(byte[] bytes) {
		ensureSpaceFor(bytes.length);
		buff.put(bytes);
		
		return this;
	}
	
	public Writer write(Enum<?> e) {
		return write((byte)e.ordinal());
	}
	
	public Writer writePrimitive(PrimitiveType type, Object obj) {
		return writePrimitive(type, obj, true);
	}
	
	public Writer writePrimitive(PrimitiveType type, Object obj, boolean header) {
		if(header) {
			write(type);
		}
		
		switch(type) {
		case Boolean: writeBool((boolean)obj); break;
		case Byte:    write((byte)obj); break;
		case Char:	  writeChar((char)obj); break;
		case Double:  writeDouble((double)obj); break;
		
		case UInt16:
		case Int16:   writeShort((short)obj); break;
		
		case UInt32:
		case Int32:   writeInt((int)obj); break;
		
		case UInt64:
		case Int64:   writeLong((long)obj); break;
		
		case Single:  writeFloat((float)obj); break;
		case String: {
			String s = (String)obj;
			int index = strings.indexOf(s);
			if(index >= 0) {
				write(RecordType.MemberReference);
				writeInt(index);
			}
			else {
				write(RecordType.BinaryObjectString);
				writeInt(strings.size());
				writeString(s);
				strings.add(s);
			}
		} break;
		default: throw new UnsupportedOperationException("Cannot write type " + type);
		}
		
		return this;
	}
	
	public Writer writeRecord(Record record) {
		// Don't reference other refs or null
		if((record instanceof MemberReference) || (record instanceof ObjectNull)) {
			record.serialize(this);
			return this;
		}
		int index = records.indexOf(record);
		if(index >= 0) {
			write(RecordType.MemberReference);
			writeInt(index);
		}
		else {
			record.serialize(this);
		}
		return this;
	}
	
	public Writer writeString(String s) {
		// Number of bytes can differ from
		// number of characters due to
		// encoding.
		byte[] bytes = s.getBytes();
		byte[] compressedLen = compressStringLength(bytes.length);
		
		ensureSpaceFor(s.length() + compressedLen.length);
		write(compressedLen);
		write(bytes);
		
		return this;
	}
	
	public Writer writeBool(boolean b) {
		return write((byte)(b ? 1 : 0));
	}
	
	public Writer writeShort(short s) {
		ensureSpaceFor(2);
		buff.putShort(s);
		
		return this;
	}
	
	public Writer writeChar(char c) {
		ensureSpaceFor(2);
		buff.putChar(c);
		
		return this;
	}
	
	public Writer writeInt(int i) {
		ensureSpaceFor(4);
		buff.putInt(i);
		
		return this;
	}
	
	public Writer writeLong(long l) {
		ensureSpaceFor(8);
		buff.putLong(l);
		
		return this;
	}
	
	public Writer writeFloat(float f) {
		ensureSpaceFor(4);
		buff.putFloat(f);
		
		return this;
	}
	
	public Writer writeDouble(double d) {
		ensureSpaceFor(8);
		buff.putDouble(d);
		
		return this;
	}
	
	public Writer writeNull() {
		return writeNull(1);
	}
	
	public Writer writeNull(int repeats) {
		ObjectNull.get(repeats).serialize(this);
		return this;
	}
	
	public byte[] bytes() {
		return Arrays.copyOfRange(buff.array(), 0, buff.position());
	}
}
