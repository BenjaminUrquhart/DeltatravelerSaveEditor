package net.benjaminurquhart.dtsaveeditor.file;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Serializer {

	public static byte[] serialize(String assemblyName, Object object) {
		
		buildObjectList(object).forEach(System.out::println);
		
		return null;
	}
	
	private static List<BinaryMember> buildObjectList(Object object) {
		return buildObjectList(object.getClass(), object);
	}
	
	private static List<BinaryMember> buildObjectList(Class<?> clazz, Object object) {
		List<BinaryMember> out = new ArrayList<>();
		
		try {
			Object obj;
			Class<?> fieldType;
			for(Field field : clazz.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				
				field.setAccessible(true);
				fieldType = field.getType();
				obj = field.get(object);
				if(fieldType.isPrimitive() || Number.class.isAssignableFrom(fieldType)) {
					out.add(new BinaryMember(BinaryType.Primitive, "TODO"));
					//throw new UnsupportedOperationException("TODO: Primitive types");
				}
				else if(fieldType == String.class) {
					out.add(new BinaryMember(BinaryType.String, obj));
				}
				else if(fieldType.isArray()) {
					if(String[].class.isAssignableFrom(fieldType)) {
						out.add(new BinaryMember(BinaryType.StringArray, obj));
					}
					else if(Object[].class.isAssignableFrom(fieldType)) {
						out.add(new BinaryMember(BinaryType.ObjectArray, obj));
					}
					else {
						out.add(new BinaryMember(BinaryType.PrimitiveArray, "TODO"));
						//throw new UnsupportedOperationException("TODO: Primitive arrays");
					}
				}
				else if(obj == null) {
					out.add(new BinaryMember(BinaryType.Object, null));
				}
				else if(ObjectMapper.hasSystemObjectMapping(obj.getClass())) { // getClass is intentional here
					out.add(ObjectMapper.mapToSystemObject(obj));
				}
				else {
					out.addAll(buildObjectList(fieldType, obj));
				}
			}
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return out;
	}
}