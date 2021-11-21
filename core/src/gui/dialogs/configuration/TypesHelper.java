/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.dialogs.configuration;

import core.build.checking.types.ArrayType;
import core.build.checking.types.CollectionType;
import core.build.checking.types.MapType;
import core.build.checking.types.NullableType;
import core.build.checking.types.SimpleType;
import core.build.checking.types.Type;
import core.signal.Signal;
import gui.exception.ParameterCreationException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author sylvester
 */
public class TypesHelper {

	static Object getDefaultValue(Type type) throws ParameterCreationException {
		if (type.canAssign(boolean.class) || type.canAssign(Boolean.class))
			return false;
		else if (type.canAssign(byte.class) || type.canAssign(Byte.class))
			return Integer.valueOf(0).byteValue();
		else if (type.canAssign(char.class) || type.canAssign(Character.class))
			return ' ';
		else if (type.canAssign(short.class) || type.canAssign(Short.class))
			return Integer.valueOf(0).shortValue();
		else if (type.canAssign(int.class) || type.canAssign(Integer.class))
			return 0;
		else if (type.canAssign(long.class) || type.canAssign(Long.class))
			return 0L;
		else if (type.canAssign(float.class) || type.canAssign(Float.class))
			return 0.0F;
		else if (type.canAssign(double.class) || type.canAssign(Double.class))
			return 0.0D;
		else if (type.canAssign(String.class))
			return "";
		else if (Type.getType(Enum.class).canAssign(type))
			return 0;
		else if (type.canAssign(Signal.class))
			return new Signal(1);
		else if (type instanceof CollectionType ||
				type instanceof ArrayType ||
				type instanceof MapType)
			return 0;
		else if (type instanceof NullableType)
			return null;
		throw new ParameterCreationException("Could not determine default value for type " + type.toString());
	}

	static Object castValue(Type type, Object value, Object[] children) throws ParameterCreationException {
		if (type instanceof SimpleType) {
			if (Type.getType(Enum.class).canAssign(((SimpleType) type).getWrappedClass()) && value instanceof Integer)
				return ((SimpleType) type).getWrappedClass().getEnumConstants()[(Integer) value];
			else if (getObjectClass(type).isInstance(value))
				return value;
			throw new ParameterCreationException("Could not cast value " + value.toString() + " to simple type " + type.toString());
		} else if (type instanceof CollectionType)
			if (!Modifier.isAbstract(((CollectionType) type).getInnerType().getWrappedClass().getModifiers()))
				throw new ParameterCreationException("Cannot create instance of abstract class " + ((CollectionType) type).getInnerType().getWrappedClass().getCanonicalName());
			else {
				Object collectionObject;
				Collection collection;
				try {
					collectionObject = ((CollectionType) type).getInnerType().getWrappedClass().newInstance();
				} catch (Exception exc) {
					throw new ParameterCreationException("Failed to create collection.", exc);
				}
				if (!(collectionObject instanceof Collection))
					throw new ParameterCreationException("Failed to create collection, created object cannot be cast to Collection.");
				collection = (Collection) collectionObject;
				for (int i = 0; i < children.length; i++)
					collection.add(children[i]);
				return collection;
			}
		else if (type instanceof ArrayType) {
//			if (((ArrayType) type).getElementType() instanceof SimpleType) {
//				if (type.canAssign(boolean[].class) && checkAgainstClass(boolean.class, children))
//					return castToArray(boolean.class, children);
//				else if (type.canAssign(Boolean[].class) && checkAgainstClass(Boolean.class, children))
//					return castToArray(Boolean.class, children);
//				else if (type.canAssign(byte[].class) && checkAgainstClass(byte.class, children))
//					return castToArray(byte.class, children);
//				else if (type.canAssign(Byte[].class) && checkAgainstClass(Byte.class, children))
//					return castToArray(Byte.class, children);
//				else if (type.canAssign(char[].class) && checkAgainstClass(char.class, children))
//					return castToArray(char.class, children);
//				else if (type.canAssign(Character[].class) && checkAgainstClass(Character.class, children))
//					return castToArray(Character.class, children);
//				else if (type.canAssign(short[].class) && checkAgainstClass(short.class, children))
//					return castToArray(short.class, children);
//				else if (type.canAssign(Short[].class) && checkAgainstClass(Short.class, children))
//					return castToArray(Short.class, children);
//				else if (type.canAssign(int[].class) && checkAgainstClass(int.class, children))
//					return castToArray(int.class, children);
//				else if (type.canAssign(Integer[].class) && checkAgainstClass(Integer.class, children))
//					return castToArray(Integer.class, children);
//				else if (type.canAssign(long[].class) && checkAgainstClass(long.class, children))
//					return castToArray(long.class, children);
//				else if (type.canAssign(Long[].class) && checkAgainstClass(Long.class, children))
//					return castToArray(Long.class, children);
//				else if (type.canAssign(float[].class) && checkAgainstClass(float.class, children))
//					return castToArray(float.class, children);
//				else if (type.canAssign(Float[].class) && checkAgainstClass(Float.class, children))
//					return castToArray(Float.class, children);
//				else if (type.canAssign(double[].class) && checkAgainstClass(double.class, children))
//					return castToArray(double.class, children);
//				else if (type.canAssign(Double[].class) && checkAgainstClass(Double.class, children))
//					return castToArray(Double.class, children);
//				else if (type.canAssign(String[].class) && checkAgainstClass(String.class, children))
//					return castToArray(String.class, children);
//				throw new ParameterCreationException("Array of simple types was requested, but none of these types was correct: " +
//						"boolean, Boolean, " +
//						"byte, Byte, " +
//						"char, Character, " +
//						"short, Short, " +
//						"int, Integer, " +
//						"long, Long, " +
//						"float, Float, " +
//						"double, Double, " +
//						"String.");
//			} else if (((ArrayType) type).getElementType() instanceof ArrayType) {
			int length = children != null ? children.length : 0;
			Class clazz = getClass(((ArrayType) type).getElementType());
			if (checkAgainstClass(clazz, children))
				return castToArray(clazz, children);
//			}
		} else if (type instanceof MapType)
			if (!Modifier.isAbstract(((MapType) type).getInnerType().getWrappedClass().getModifiers()))
				throw new ParameterCreationException("Cannot create instance of abstract class " + ((CollectionType) type).getInnerType().getWrappedClass().getCanonicalName());
			else {
				Object mapObject;
				Map map;
				try {
					mapObject = ((MapType) type).getInnerType().getWrappedClass().newInstance();
				} catch (Exception exc) {
					throw new ParameterCreationException("Failed to create map.", exc);
				}
				if (!(mapObject instanceof Map))
					throw new ParameterCreationException("Failed to create map, created object cannot be cast to Map.");
				map = (Map) mapObject;
				for (int i = 0; i < children.length; i++)
					if (!(children[i] instanceof Map.Entry))
						throw new ParameterCreationException("Failed to create map, given children were not all of type Entry");
					else {
						Entry entry = (Entry) children[i];
						map.put(entry.getKey(), entry.getValue());
					}
				return map;
			}
		else if (type instanceof NullableType)
			if (value == null)
				return null;
			else
				return castValue(((NullableType) type).getInnerType(), value, null);

		throw new IllegalArgumentException("Could not cast given value " + value.toString() + " to given type " + type.toString());
	}

	private static boolean checkAgainstClass(Class clazz, Object... values) {
		if (values == null)
			return true;
		if (clazz.isPrimitive()) {
			if (clazz.getCanonicalName().equals("byte"))
				for (int i = 0; i < values.length; i++)
					try {
						byte test = (Byte) values[i];
					} catch (ClassCastException exc) {
						return false;
					}
			else if (clazz.getCanonicalName().equals("char"))
				for (int i = 0; i < values.length; i++)
					try {
						char test = (Character) values[i];
					} catch (ClassCastException exc) {
						return false;
					}
			else if (clazz.getCanonicalName().equals("short"))
				for (int i = 0; i < values.length; i++)
					try {
						short test = (Short) values[i];
					} catch (ClassCastException exc) {
						return false;
					}
			else if (clazz.getCanonicalName().equals("int"))
				for (int i = 0; i < values.length; i++)
					try {
						int test = (Integer) values[i];
					} catch (ClassCastException exc) {
						return false;
					}
			else if (clazz.getCanonicalName().equals("long"))
				for (int i = 0; i < values.length; i++)
					try {
						long test = (Long) values[i];
					} catch (ClassCastException exc) {
						return false;
					}
			else if (clazz.getCanonicalName().equals("float"))
				for (int i = 0; i < values.length; i++)
					try {
						float test = (Float) values[i];
					} catch (ClassCastException exc) {
						return false;
					}
			else if (clazz.getCanonicalName().equals("double"))
				for (int i = 0; i < values.length; i++)
					try {
						double test = (Double) values[i];
					} catch (ClassCastException exc) {
						return false;
					}
		} else
			for (int i = 0; i < values.length; i++)
				if (!clazz.isInstance(values[i]))
					return false;
		return true;
	}

	private static Object castToArray(Class clazz, Object... values) {
		int length = values != null ? values.length : 0;
		Object array = Array.newInstance(clazz, length);
		if (clazz.isPrimitive()) {
			if (clazz.getCanonicalName().equals("byte"))
				for (int i = 0; i < length; i++)
					Array.set(array, i, (Byte) values[i]);
			else if (clazz.getCanonicalName().equals("char"))
				for (int i = 0; i < length; i++)
					Array.set(array, i, (Character) values[i]);
			else if (clazz.getCanonicalName().equals("short"))
				for (int i = 0; i < length; i++)
					Array.set(array, i, (Short) values[i]);
			else if (clazz.getCanonicalName().equals("int"))
				for (int i = 0; i < length; i++)
					Array.set(array, i, (Integer) values[i]);
			else if (clazz.getCanonicalName().equals("long"))
				for (int i = 0; i < length; i++)
					Array.set(array, i, (Long) values[i]);
			else if (clazz.getCanonicalName().equals("float"))
				for (int i = 0; i < length; i++)
					Array.set(array, i, (Float) values[i]);
			else if (clazz.getCanonicalName().equals("double"))
				for (int i = 0; i < length; i++)
					Array.set(array, i, (Double) values[i]);
		} else
			for (int i = 0; i < length; i++)
				Array.set(array, i, values[i]);
		return array;
	}

	private static Class getObjectClass(Type type) throws ParameterCreationException {
		if (type.canAssign(boolean.class) || type.canAssign(Boolean.class))
			return Boolean.class;
		else if (type.canAssign(byte.class) || type.canAssign(Byte.class))
			return Byte.class;
		else if (type.canAssign(char.class) || type.canAssign(Character.class))
			return Character.class;
		else if (type.canAssign(short.class) || type.canAssign(Short.class))
			return Short.class;
		else if (type.canAssign(int.class) || type.canAssign(Integer.class))
			return Integer.class;
		else if (type.canAssign(long.class) || type.canAssign(Long.class))
			return Long.class;
		else if (type.canAssign(float.class) || type.canAssign(Float.class))
			return Float.class;
		else if (type.canAssign(double.class) || type.canAssign(Double.class))
			return Double.class;
		else if (type.canAssign(String.class))
			return String.class;
		else if (type.canAssign(Signal.class))
			return Signal.class;

		throw new ParameterCreationException("Could not get object class of type " + type.toString());
	}

	private static Class getClass(Type type) throws ParameterCreationException {
		if (type instanceof SimpleType)
			return ((SimpleType) type).getWrappedClass();
		else if (type instanceof ArrayType)
			return Object[].class;
//		if (type.canAssign(boolean.class))
//			return boolean.class;
//		else if (type.canAssign(Boolean.class))
//			return Boolean.class;
//		else if (type.canAssign(byte.class))
//			return byte.class;
//		else if (type.canAssign(Byte.class))
//			return Byte.class;
//		else if (type.canAssign(char.class))
//			return char.class;
//		else if (type.canAssign(Character.class))
//			return Character.class;
//		else if (type.canAssign(short.class))
//			return short.class;
//		else if (type.canAssign(Short.class))
//			return Short.class;
//		else if (type.canAssign(int.class))
//			return int.class;
//		else if (type.canAssign(Integer.class))
//			return Integer.class;
//		else if (type.canAssign(long.class))
//			return long.class;
//		else if (type.canAssign(Long.class))
//			return Long.class;
//		else if (type.canAssign(float.class))
//			return float.class;
//		else if (type.canAssign(Float.class))
//			return Float.class;
//		else if (type.canAssign(double.class))
//			return double.class;
//		else if (type.canAssign(Double.class))
//			return Double.class;
//		else if (type.canAssign(String.class))
//			return String.class;
//		else if (type.canAssign(Signal.class))
//			return Signal.class;
//
		throw new ParameterCreationException("Could not get class of type " + type.toString());
	}

}
