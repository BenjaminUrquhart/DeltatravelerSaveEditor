package net.benjaminurquhart.dtsaveeditor.deserialize;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;	
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.RecordType;
import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.BinaryType;
import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.PrimitiveType;
import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer.Reader;
import sun.misc.Unsafe;

public class ClassWithMembersAndTypes extends Record {
	
	private static final Unsafe unsafe;
	private static final Map<String, Function<Map<String, Object>, Object>> systemObjectMappings;
	
	static {
		unsafe = getUnsafe();
		systemObjectMappings = new HashMap<>();
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
	
	public static void setSystemObjectMapping(String name, Function<Map<String, Object>, Object> mapping) {
		systemObjectMappings.put(name, mapping);
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
	
	public ClassWithMembersAndTypes(Reader reader) {
		this(reader, true);
	}

	public ClassWithMembersAndTypes(Reader reader, boolean hasLibrary) {
		this.classInfo = new ClassInfo(reader);
		this.memberTypeInfo = new MemberTypeInfo(reader, classInfo.memberNames.size());
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
	
	public <T> T getAs(Class<T> clazz) throws InstantiationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return getAs(clazz, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAs(Class<T> clazz, Function<ClassWithMembersAndTypes, T> converter) throws InstantiationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(converter != null) {
			return converter.apply(this);
		}
		String systemClassName;
		Function<Map<String, Object>, Object> systemObjectMapping;
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
				value = ((Record)value).type == RecordType.ArraySinglePrimitive ? 
						this.convertToPrimitiveArray((ArraySinglePrimitive)value) : 
						((ArraySingle)value).getValues();
			}
			if(value instanceof SystemClassWithMembersAndTypes) {
				SystemClassWithMembersAndTypes cls = (SystemClassWithMembersAndTypes)value;
				systemClassName = cls.getClassName();
				systemObjectMapping = systemObjectMappings.get(systemClassName);
				
				if(systemObjectMapping == null) {
					throw new UnsupportedOperationException("No mapping exists for SystemClass " + systemClassName);
				}
				value = systemObjectMapping.apply(cls.values);
			}
			field.set(inst, value);
		}
		
		return (T)inst;
	}
	
	private Object convertToPrimitiveArray(ArraySinglePrimitive arr) {
		Object out = null;
		
		Object[] values = arr.getValues();
		//System.out.println(arr.primitiveType + " " + Arrays.deepToString(values));
		
		// I too enjoy Java
		// TODO: More types, just so happens that Deltatraveler only uses Int32
		switch(arr.primitiveType) {
		case Int32:
			int[] a = new int[arr.length];
			for(int i = 0; i < arr.length; i++) {
				a[i] = (int)values[i];
			}
			out = a;
			break;
		default: throw new UnsupportedOperationException("no ArraySinglePrimitive type mapping exists for PrimitiveType " + arr.primitiveType);
		}
		
		return out;
	}
	
	@Override
	public void preProcess(Map<Integer, Record> objects) {
		objects.put(classInfo.objectId, this);
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
}
