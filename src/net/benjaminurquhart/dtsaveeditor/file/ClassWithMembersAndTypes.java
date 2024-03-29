package net.benjaminurquhart.dtsaveeditor.file;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;	
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import sun.misc.Unsafe;

public class ClassWithMembersAndTypes extends Record {
	
	private static final Unsafe unsafe;

	static {
		unsafe = getUnsafe();
	}
	
	private static Unsafe getUnsafe() {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe)field.get(null);
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class ClassInfo {
		public final int objectId;
		public final String name;
		
		public final List<String> memberNames;
		
		public ClassInfo(Reader reader) {
			this.objectId = reader.buff.getInt();
			this.name = reader.readString();
			
			int num = reader.buff.getInt();
			List<String> names = new ArrayList<>();
			for(int i = 0; i < num; i++) {
				names.add(reader.readString());
			}
			
			this.memberNames = Collections.unmodifiableList(names);
		}
	}
	
	public static class MemberTypeInfo {
		
		public class TypeInfoPair {
			public final BinaryType type;
			public final Object additionalInfo;
			
			protected TypeInfoPair(BinaryType type, Object additionalInfo) {
				this.additionalInfo = additionalInfo;
				this.type = type;
			}
		}
		
		public final List<TypeInfoPair> typeInfo;
		
		public MemberTypeInfo(Reader reader, int numRecords) {
			List<TypeInfoPair> info = new ArrayList<>();
			List<BinaryType> types = new ArrayList<>();
			
			
			for(int i = 0; i < numRecords; i++) {
				types.add(BinaryType.values()[reader.buff.get()]);
			}
			
			Object additionalInfo;
			for(BinaryType type : types) {
				//System.out.print(type);
				additionalInfo = null;
				
				switch(type) {
				case Primitive:
				case PrimitiveArray:
					additionalInfo = PrimitiveType.values()[reader.buff.get()];
					break;
				
				case SystemClass:
					additionalInfo = reader.readString();
					break;
				
				case ObjectArray:
				case StringArray:
				case Object:
				case String:
					break;
				
				default:
					//System.out.println();
					throw new UnsupportedOperationException("Unsupported BinaryType " + type);
				}
				/*
				if(additionalInfo == null) {
					System.out.println();
				}
				else {
					System.out.println(" -> " + additionalInfo);
				}*/
				info.add(new TypeInfoPair(type, additionalInfo));
			}
			
			this.typeInfo = Collections.unmodifiableList(info);
		}
	}
	
	public final ClassInfo classInfo;
	public final MemberTypeInfo memberTypeInfo;
	
	public final int libraryId;
	
	public Map<String, Object> values;
	public Map<String, Integer> offsets;
	
	private BinaryLibrary library;
	
	public ClassWithMembersAndTypes(Reader reader) {
		this(reader, true);
	}

	public ClassWithMembersAndTypes(Reader reader, boolean hasLibrary) {
		this.classInfo = new ClassInfo(reader);
		this.memberTypeInfo = new MemberTypeInfo(reader, classInfo.memberNames.size());
		this.offsets = new HashMap<>();
		this.values = new HashMap<>();
		
		if(hasLibrary) {
			this.libraryId = reader.buff.getInt();
		}
		else {
			this.libraryId = -1;
		}
		
		Object obj;
		String name;
		MemberTypeInfo.TypeInfoPair typeInfo;
		for(int i = 0; i < classInfo.memberNames.size(); i++) {
			name = classInfo.memberNames.get(i);
			typeInfo = memberTypeInfo.typeInfo.get(i);
			//System.out.print(name + " -> ");
			
			offsets.put(name, reader.buff.position());
			if(typeInfo.additionalInfo == null) {
				obj = reader.readRecord();
			}
			else {
				switch(typeInfo.type) {
				case Primitive:
					PrimitiveType primitiveType = (PrimitiveType)typeInfo.additionalInfo;
					obj = reader.readPrimitive(primitiveType);
					break;
				
				case PrimitiveArray:
				case SystemClass:
					obj = reader.readRecord();
					break;
				default:
					throw new UnsupportedOperationException("Unsupported BinaryType " + typeInfo.type);
				}
			}
			
			//System.out.println(obj);
			values.put(name, obj);
		}
	}
	
	public String getClassName() {
		return classInfo.name;
	}
	
	public BinaryLibrary getLibrary() {
		return library;
	}
	
	public <T> T getAs(Class<T> clazz) throws InstantiationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return getAs(clazz, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAs(Class<T> clazz, Function<ClassWithMembersAndTypes, T> converter) throws InstantiationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(converter != null) {
			return converter.apply(this);
		}
		String systemClassName;
		T inst = (T)unsafe.allocateInstance(clazz);
		
		Field field;
		Object value;
		for(String fieldName : values.keySet()) {
			try {
				field = clazz.getDeclaredField(fieldName);
			}
			catch(NoSuchFieldException e) {
				field = clazz.getField(fieldName);
			}
			if(Modifier.isStatic(field.getModifiers())) {
				throw new IllegalStateException("Attempted to set static field " + fieldName);
			}
			field.setAccessible(true);
			
			value = values.get(fieldName);
			
			while(value instanceof MemberReference) {
				value = ((MemberReference)value).getReference();
			}
			if(value instanceof BinaryObject) {
				value = ((BinaryObject)value).value;
			}
			if(value instanceof ArraySingle) {
				value = ((Record)value).getType() == RecordType.ArraySinglePrimitive ? 
						this.convertToPrimitiveArray((ArraySinglePrimitive)value) : 
						((ArraySingle)value).getValues();
			}
			if(value instanceof SystemClassWithMembersAndTypes) {
				SystemClassWithMembersAndTypes cls = (SystemClassWithMembersAndTypes)value;
				systemClassName = cls.getClassName();
				value = ObjectMapper.mapToJavaObject(systemClassName, cls.values);
			}
			field.set(inst, value);
		}
		
		return (T)inst;
	}
	
	private Object convertToPrimitiveArray(ArraySinglePrimitive arr) {
		
		// I found out how to dynamically create arrays of any type.
		// I must now apologize for what shall follow.
		
		Object out = null;
		
		Object[] values = arr.getValues();
		Class<?> clazz = null;

		switch(arr.primitiveType) {
		case Boolean: clazz = boolean.class; break;
		case Byte:    clazz = byte.class;    break;
		case Char:    clazz = char.class;    break;
		case Int16:   clazz = short.class;   break;
		case Int32:   clazz = int.class;     break;
		case Int64:   clazz = long.class;    break;
		case UInt16:  clazz = int.class;     break;
		case UInt32:  clazz = long.class;    break;
		
		case Single:  clazz = float.class;   break;
		case Double:  clazz = double.class;  break;
		
		default: throw new UnsupportedOperationException("no ArraySinglePrimitive type mapping exists for PrimitiveType " + arr.primitiveType);
		}
		
		out = Array.newInstance(clazz, values.length);
		
		for(int i = 0; i < values.length; i++) {
			Array.set(out, i, values[i]);
		}
		
		return out;
	}
	
	@Override
	public void preProcess(Map<Integer, Record> objects) {
		objects.put(classInfo.objectId, this);
	}
	
	@Override
	public void postProcess(Map<Integer, Record> objects) {
		if(libraryId > -1) {
			library = (BinaryLibrary)objects.get(libraryId);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("class ");
		sb.append(classInfo.name);
		sb.append(" (id ");
		sb.append(classInfo.objectId);
		sb.append(String.format(") @ 0x%08x {\n", offset));
		
		String tmp;
		String name;
		Object value;
		MemberTypeInfo.TypeInfoPair typeInfo;
		for(int i = 0; i < classInfo.memberNames.size(); i++) {
			name = classInfo.memberNames.get(i);
			typeInfo = memberTypeInfo.typeInfo.get(i);
			
			value = values.get(name);
			
			sb.append('\t');
			sb.append(typeInfo.type);
			if(typeInfo.additionalInfo != null) {
				sb.append('<');
				if(typeInfo.type == BinaryType.SystemClass) {
					tmp = String.valueOf(typeInfo.additionalInfo);
					sb.append(tmp.split("`", 2)[0]);
				}
				else {
					sb.append(typeInfo.additionalInfo);
				}
				sb.append('>');
			}
			sb.append(' ');
			sb.append(name);
			sb.append(" = ");
			sb.append(value);
			sb.append('\n');
		}
		sb.append('}');
		return sb.toString();
	}

	@Override
	protected void serializeInternal(Writer writer) {
		throw new UnsupportedOperationException();
	}
}
