package zh.lisa.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


@SuppressWarnings("rawtypes")
public class ClassUtil {
	
	private static final Map<Class, Class> primitiveWrapperTypeMap = new HashMap<Class, Class>(8);
	
	private static final Map<String, Class> primitiveTypeNameMap = new HashMap<String, Class>(16);
	
	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);

		Set<Object> primitiveTypeNames = new HashSet<Object>(16);
		primitiveTypeNames.addAll(primitiveWrapperTypeMap.values());
		primitiveTypeNames.addAll(Arrays.asList(new Class[] {
				boolean[].class, byte[].class, char[].class, double[].class,
				float[].class, int[].class, long[].class, short[].class}));
		for (Iterator<Object> it = primitiveTypeNames.iterator(); it.hasNext();) {
			Class primitiveClass = (Class) it.next();
			primitiveTypeNameMap.put(primitiveClass.getName(), primitiveClass);
		}
	}

	public static Object newInstanceOfType(String className) {
		Object result = null;
		try {
			result = getClassFor(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("fail to initialize type: " + className, e);
		} 
		
		return result;
	}
	
	public static Class getClassFor(String className) {
		try {
			return ClassUtil.class.getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("fail to find class: " + className, e);
		}
	}
	
	public static Object convertValue(Object value, Class<?> targetType) {
		
		if (value.getClass().equals(targetType)) return value;
		
		if (targetType.isPrimitive()) {
			targetType = getWrapperClass(targetType);
		}
		
		if (targetType.isAssignableFrom(value.getClass())) return value;
		
		if ((value instanceof String || value instanceof Number) && Number.class.isAssignableFrom(targetType)) {
			try {
				Constructor ctor = targetType.getConstructor(new Class[]{String.class});
				return ctor.newInstance(new Object[] { value.toString() });
			} catch (Exception e) {
				throw new RuntimeException("Cannot convert from "+value.getClass().getName() + " to " + targetType, e);
			}
		}
		
		if (targetType.isArray() && Collection.class.isAssignableFrom(value.getClass())) {
			Collection collection = (Collection)value;
			Object array = Array.newInstance(targetType.getComponentType(), collection.size());
			int i = 0; 
			for (Iterator iter = collection.iterator(); iter.hasNext();) {
				Object val = iter.next();
				Array.set(array, i++, val);
			}
			
			return array;
		}
		
		if (Collection.class.isAssignableFrom(targetType) && value.getClass().isArray()) {
			return Arrays.asList((Object[]) value);
		}
		
		throw new IllegalArgumentException("Cannot convert from "+value.getClass().getName() + " to " + targetType);
	}
	public static boolean isPrimitiveWrapper(Class clazz) {
		return primitiveWrapperTypeMap.containsKey(clazz);
	}
	
	public static boolean isPrimitiveOrWrapper(Class clazz) {
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}
	
	public static boolean isPrimitiveWrapperArray(Class clazz) {
		return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
	}
	public static boolean isPrimitiveOrWrapperArray(Class clazz) {
		return (clazz.isArray() || isPrimitiveWrapperArray(clazz));
	}
	
	public static Class getWrapperClass(Class primitiveClass) {
		return primitiveClass == int.class ? Integer.class : 
			   primitiveClass == long.class ? Long.class : 
			   primitiveClass == short.class ? Short.class :
               primitiveClass == byte.class ? Byte.class :
			   primitiveClass == float.class ? Float.class :
			   primitiveClass == double.class ? Double.class : 
			   primitiveClass == boolean.class ? Boolean.class :
			   primitiveClass;
	}
	
	public static Class getPrimitiveClass(Class wrapperClass) {
		return 	wrapperClass == Integer.class ? int.class : 
				wrapperClass == Long.class ? long.class : 
				wrapperClass == Short.class ? short.class :
				wrapperClass == Byte.class ? byte.class :
				wrapperClass == Float.class ? float.class :
				wrapperClass == Double.class ? double.class : 
				wrapperClass == Boolean.class ? boolean.class :
				wrapperClass;
	}
	
	@SuppressWarnings("unchecked")
	public static Object invokeMethod(Object instance, Method method, Object[] arguments) throws 
									IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			if (!parameterTypes[i].isAssignableFrom(arguments[i].getClass())) {
				arguments[i] = convertValue(arguments[i], parameterTypes[i]);
			}
		}
		if(arguments[0] != null)
			return method.invoke(instance, arguments);
		else
			return method.invoke(instance);
	}
	
	@SuppressWarnings("unchecked")
	public static Method getMethod(Class clazz,String methodName,Class[] parameterTypes){
		try {
			if(parameterTypes[0] != null)
				return clazz.getMethod( methodName, parameterTypes );
			else
				return clazz.getMethod( methodName);
		}
		catch (Exception e) {
			String params = "";
			if(parameterTypes[0] != null)
				for(int i=0;i<parameterTypes.length;i++){
					params+=parameterTypes[i].getName()+" ";
				}
			throw new RuntimeException("No Method find!"+" The Class is:"+clazz.getName()
					+" The Method is:"+methodName+" The Parameter is:"+params);
		}
	}
}
