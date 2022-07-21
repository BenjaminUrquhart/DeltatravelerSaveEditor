package net.benjaminurquhart.dtsaveeditor.file;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ObjectMapper {
	
	static class SystemObjectMapping<T> {
		public final Class<T> clazz;
		public String className;
		
		public BiConsumer<T, Map<String, Object>> mapping;
		
		protected SystemObjectMapping(String className, Class<T> clazz, BiConsumer<T, Map<String, Object>> mapping) {
			this.className = className;
			this.mapping = mapping;
			this.clazz = clazz;
		}
	}

	private static final Map<String, Function<Map<String, Object>, Object>> systemObjectMappings = new HashMap<>();
	private static final Map<Class<?>, SystemObjectMapping<?>> javaObjectMappings = new HashMap<>();
	
	
	public static void setSystemObjectMapping(String name, Function<Map<String, Object>, Object> mapping) {
		systemObjectMappings.put(name, mapping);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> void setJavaObjectMapping(String name, Class<T> clazz, BiConsumer<T, Map<String, Object>> mapping) {
		if(javaObjectMappings.containsKey(clazz)) {
			SystemObjectMapping<T> javaMapping = (SystemObjectMapping<T>) javaObjectMappings.get(clazz);
			javaMapping.className = name;
			javaMapping.mapping = mapping;
		}
		else {
			javaObjectMappings.put(clazz, new SystemObjectMapping<T>(name, clazz, mapping));
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> SystemObjectMapping<T> findMapping(Class<T> clazz) {
		Class<?> realClass = clazz;
		
		while(realClass != Object.class) {
			if(javaObjectMappings.containsKey(realClass)) {
				return (SystemObjectMapping<T>) javaObjectMappings.get(realClass);
			}
			realClass = realClass.getSuperclass();
		}
		return (SystemObjectMapping<T>) javaObjectMappings.get(Object.class);
	}
	
	public static boolean hasSystemObjectMapping(Class<?> clazz) {
		return findMapping(clazz) != null;
	}
	
	public static boolean hasJavaObjectMapping(String className) {
		return systemObjectMappings.containsKey(className);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> SystemClass mapToSystemObject(T object) {
		if(object == null) {
			return null;
		}
		
		SystemObjectMapping<T> mapping = findMapping((Class<T>)object.getClass());
		
		if(mapping == null) {
			throw new UnsupportedOperationException("No C# system class mapping exists for " + object.getClass());
		}
		
		SystemClass out = new SystemClass(mapping.className, new HashMap<>());
		mapping.mapping.accept(object, out.values);
		return out;
	}
	
	public static <T> T mapToJavaObject(ClassWithMembersAndTypes clazz) {
		return mapToJavaObject(clazz.getClassName(), clazz.values);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T mapToJavaObject(String className, Map<String, Object> values) {
		Function<Map<String, Object>, T> mapping = (Function<Map<String, Object>, T>) systemObjectMappings.get(className);
		if(mapping == null) {
			throw new UnsupportedOperationException("No Java class mapping exists for " + className);
		}
		
		return (T) mapping.apply(values);
	}
}
