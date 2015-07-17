package info.novatec.inspectit.rcp.preferences;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * Utilities to transform string or string collections and maps to the same with primitive wrapper
 * types.
 * 
 * @author Ivan Senic
 * 
 */
public final class StringToPrimitiveTransformUtil {

	/**
	 * Private constructor.
	 */
	private StringToPrimitiveTransformUtil() {
	}

	/**
	 * Transforms all the strings in the original collection to the given class objects and adds
	 * them to the given resulting collection.
	 * 
	 * @param <E>
	 *            Type of collection.
	 * @param original
	 *            Source collection.
	 * @param collection
	 *            Collection to add transformed elements to.
	 * @param elementClass
	 *            Runtime class of objects to be added to the collection.
	 * @throws PreferenceException
	 *             If transformation fails.
	 */
	@SuppressWarnings("unchecked")
	public static <E> void transformStringCollection(Collection<String> original, Collection<E> collection, Class<E> elementClass) throws PreferenceException {
		Method parseMethod = findParseMethod(elementClass);
		if (null != parseMethod) {
			for (String toTransform : original) {
				try {
					Object transformed = parseMethod.invoke(null, toTransform);
					if (elementClass.isAssignableFrom(transformed.getClass())) {
						collection.add((E) transformed);
					}
				} catch (Exception e) {
					throw new PreferenceException("Error transforming Collection<java.lang.String> to Collection<" + elementClass.getName() + ">.", e);
				}
			}
		} else {
			throw new PreferenceException("Error transforming Collection<java.lang.String> to Collection<" + elementClass.getName() + ">. Parsing method can not be found in class "
					+ elementClass.getName() + ".");
		}
	}

	/**
	 * Transforms all the strings key/value pairs in the original map to the given class key/value
	 * pairs and adds them to the given resulting map.
	 * 
	 * @param <K>
	 *            Type of key.
	 * @param <V>
	 *            Type of value.
	 * @param original
	 *            Original map.
	 * @param map
	 *            Map to add transformed entries to.
	 * @param keyClass
	 *            Runtime class of key objects.
	 * @param valueClass
	 *            Runtime class of value objects.
	 * @throws PreferenceException
	 *             If transformation fails.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> void transformStringMap(Map<String, String> original, Map<K, V> map, Class<K> keyClass, Class<V> valueClass) throws PreferenceException {
		Method parseKeyMethod = findParseMethod(keyClass);
		Method parseValueMethod = findParseMethod(valueClass);
		if (null != parseKeyMethod && null != parseValueMethod) {
			for (Map.Entry<String, String> toTransformEntry : original.entrySet()) {
				try {
					Object transformedKey = parseKeyMethod.invoke(null, toTransformEntry.getKey());
					Object transformedValue = parseValueMethod.invoke(null, toTransformEntry.getValue());
					if (keyClass.isAssignableFrom(transformedKey.getClass()) && valueClass.isAssignableFrom(transformedValue.getClass())) {
						map.put((K) transformedKey, (V) transformedValue);
					}
				} catch (Exception e) {
					throw new PreferenceException("Error transforming Map<java.lang.String, java.lang.String> to Map<" + keyClass.getName() + ", " + valueClass.getName() + ">.", e);
				}
			}
		} else if (null == parseKeyMethod) {
			throw new PreferenceException("Error transforming Map<java.lang.String, java.lang.String> to Map<" + keyClass.getName() + ", " + valueClass.getName() + ">."
					+ "Parsing method can not be found in class " + keyClass.getName() + ".");
		} else {
			throw new PreferenceException("Error transforming Map<java.lang.String, java.lang.String> to Map<" + keyClass.getName() + ", " + valueClass.getName() + ">."
					+ "Parsing method can not be found in class " + valueClass.getName() + ".");
		}
	}

	/**
	 * Finds the parseXXX(String) method in given class if it exists.
	 * 
	 * @param clazz
	 *            Class to examine.
	 * @return Method or null if method can no be found.
	 */
	private static Method findParseMethod(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("parse")) {
				Class<?>[] params = method.getParameterTypes();
				if (params.length == 1 && params[0].equals(String.class)) {
					return method;
				}
			}
		}
		return null;
	}
}
