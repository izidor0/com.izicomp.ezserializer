package com.izicomp.ezserializer.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.XStream;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReflectionUtils {
	
	private static final XStream xStream = new XStream(); 
	
	public static String toXStreamXml(Object object) {
		return xStream.toXML(object);
	}

	public static Object fromXStreamXml(String xml, Object object) {
		return xStream.fromXML(xml, object);
	}
	
	public static void loadLazyFields(Object object) {
		if (isObject(object)) {
			loadLazyFields(object, new HashSet<String>());
		}
	}

	public static void clearCylicReferences(Object object) {
		clearCylicReferences(object, new LinkedHashSet<String>(), false);
	}

	private static void clearCylicReferences(Object object, Set<String> readedReferences, boolean readingGroup) {

		if (!isObject(object) || alreadyRead(object, readedReferences, readingGroup)) {
			return;
		}
		
		if (!readingGroup) {
			putReference(readedReferences, object);
		}
		
		try {
			if (isList(object)) {
				for (Object objectListItem : parseList(object)) {
					putReference(readedReferences, object, objectListItem);
					clearCylicReferences(objectListItem, readedReferences, true);
				}

			} else if (isMap(object)) {
				for (Object objectListItem : ((Map) object).values()) {
					putReference(readedReferences, object, objectListItem);
					clearCylicReferences(objectListItem, readedReferences, true);
				}
				
			} else if (isIterator(object)) {
				Iterator iterator = ((Iterator) object);
				while (iterator.hasNext()) {
					
					Object objectListItem = iterator.next();
					putReference(readedReferences, object, objectListItem);
					clearCylicReferences(objectListItem, readedReferences, true);
				}

			} else {
				for (Field field : object.getClass().getDeclaredFields()) {

					field.setAccessible(true);
					Object fieldValue = field.get(object);
					
					if ((isList(fieldValue) || isMap(fieldValue) || isIterator(fieldValue)) && !alreadyRead(fieldValue, readedReferences, readingGroup)) {
						clearCylicReferences(fieldValue, readedReferences, true);
					
					} else if(isObject(fieldValue) && !alreadyRead(fieldValue, readedReferences, readingGroup)) {
						clearCylicReferences(fieldValue, readedReferences, false);
					
					} else if(isObject(fieldValue) && alreadyRead(fieldValue, readedReferences, readingGroup)) {
						field.set(object, null);
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static void loadLazyFields(Object object, Set<String> readedReferences) {
		
		if (readedReferences.contains(getObjectHash(object))) {
			return;
		}
		readedReferences.add(getObjectHash(object));
		
		try {
			if (isList(object)) {
				for (Object objectListItem : parseList(object)) {
					loadLazyFields(objectListItem, readedReferences);
				}

			} else if (isMap(object)) {
				for (Object objectListItem : ((Map) object).values()) {
					loadLazyFields(objectListItem, readedReferences);
				}
				
			} else if (isIterator(object)) {
				Iterator iterator = ((Iterator) object);
				while (iterator.hasNext()) {
					loadLazyFields(iterator.next(), readedReferences);
				}

			} else {
				for (Field field : object.getClass().getDeclaredFields()) {
	
					field.setAccessible(true);
					Object fieldValue = field.get(object);
	
					if (isList(fieldValue) && !readedReferences.contains(fieldValue.toString())) {

						Method sizeMethod = findMethodByRegexName(fieldValue, ".*\\.size\\(\\)");
						if (sizeMethod != null) {
	
							sizeMethod.setAccessible(true);
							int size = (Integer) sizeMethod.invoke(fieldValue);
							
							for (int i = 0; i < size; i++) {
								Method getMethod = findMethodByRegexName(fieldValue, ".*\\.get\\(int\\).*");
								Object objectItem = getMethod.invoke(fieldValue, i);
								loadLazyFields(objectItem, readedReferences);
							}
						}
						
					} else if (isMap(fieldValue) && !readedReferences.contains(fieldValue.toString())) {

						Method valuesMethod = findMethodByRegexName(fieldValue, ".*\\.values\\(\\)");
						if (valuesMethod != null) {

							valuesMethod.setAccessible(true);
							Collection values = (Collection) valuesMethod.invoke(fieldValue);

							for (Object value : values) {
								loadLazyFields(value, readedReferences);
							}
						}
						
					} else if (isIterator(fieldValue) && !readedReferences.contains(fieldValue.toString())) {
						
						Method hasNextMethod = findMethodByRegexName(fieldValue, ".*\\.hasNext\\(\\)");
						if (hasNextMethod != null) {
	
							hasNextMethod.setAccessible(true);
							boolean hasNext = (Boolean) hasNextMethod.invoke(fieldValue);
							
							if(hasNext) {
								Method nextMethod = findMethodByRegexName(fieldValue, ".*\\.next\\(\\).*");
								nextMethod.setAccessible(true);
								
								Object objectItem = nextMethod.invoke(fieldValue);
								loadLazyFields(objectItem, readedReferences);
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isList(Object object) {
		return isObject(object) && (object instanceof Collection || object.getClass().toString().matches("class \\[.*"));
	}

	private static boolean isMap(Object object) {
		return isObject(object) && object instanceof Map;
	}
	
	private static boolean isIterator(Object object) {
		return isObject(object) && object instanceof Iterator;
	}	

	private static boolean isObject(Object object) {
		if (object == null || object instanceof String || object instanceof Number || object instanceof Boolean
				|| object instanceof Character || object instanceof Byte) {
			return false;
		}
		return true;
	}
	
	private static Method findMethodByRegexName(Object object, String methodName) {
		try {
			for (Method method : object.getClass().getDeclaredMethods()) {
				if (method.toString().matches(methodName)) {
					return method;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	private static List<Object> parseList(Object object) {
		List<Object> objects = null;

		if (object.getClass().toString().matches("class \\[.*")) {
			try {
				objects = Arrays.asList((Object[]) object);
			} catch (ClassCastException e) {

				if (object.getClass().toString().matches("class \\[\\w{1}")) {
					object = new ArrayList<Object>(); //TODO, is primitive array type, future task =)
				}
			}
		} else if (object instanceof Collection) {
			objects = new ArrayList<Object>((Collection) object);
		}
		return objects;
	}
	
	private static void putReference(Set<String> readedReferences, Object... objects) {
		
		StringBuilder reference = new StringBuilder();
		for (Object object : objects) {
			reference.append(getObjectHash(object));
		}
		readedReferences.add(reference.toString());
	}

	private static boolean alreadyRead(Object object, Set<String> readedReferences, boolean readingGroup) {

		String hash = getObjectHash(object);
		boolean readed = false;

		if (!readingGroup) {
			readed = readedReferences.contains(hash);

		} else {
			for (String reference : readedReferences) {
				readed = reference.matches(".*" + hash);
				break;
			}
		}
		return readed;
	}

	private static String getObjectHash(Object object) {
		return object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
	}
}