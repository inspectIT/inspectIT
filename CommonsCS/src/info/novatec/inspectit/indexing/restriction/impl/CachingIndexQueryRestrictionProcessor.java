package info.novatec.inspectit.indexing.restriction.impl;

import info.novatec.inspectit.indexing.restriction.IIndexQueryRestriction;
import info.novatec.inspectit.indexing.restriction.IIndexQueryRestrictionProcessor;
import info.novatec.inspectit.spring.logger.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * This restriction processor caches the methods of each class that needs to be invoke. It also
 * marks in the cache all method that do not exist for specific class and an attempt to find them
 * was made.
 *
 * @author Ivan Senic
 *
 */
@Component
public class CachingIndexQueryRestrictionProcessor implements IIndexQueryRestrictionProcessor {

	/**
	 * The logger.
	 */
	@Log
	Logger log;

	/**
	 * Map for caching methods.
	 */
	private final ConcurrentHashMap<Integer, Method> cacheMap;

	/**
	 * Marker method.
	 */
	private Method markerMethod;

	/**
	 * Default constructor. Sets {@link #markerMethod} to refer to {@link Object#toString()}.
	 */
	public CachingIndexQueryRestrictionProcessor() {
		cacheMap = new ConcurrentHashMap<Integer, Method>();
		try {
			// setting marker method to point to Object.toString()
			markerMethod = Object.class.getMethod("toString", new Class[0]);
		} catch (Exception e) {
			throw new IllegalStateException("Method toString() can not be found", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean areAllRestrictionsFulfilled(Object object, List<IIndexQueryRestriction> restrictions) {
		for (IIndexQueryRestriction indexingRestriction : restrictions) {
			if (!isRestrictionFulfilled(object, indexingRestriction)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if one {@link IIndexQueryRestriction} is fulfilled.
	 *
	 * @param object
	 *            to start from
	 * @param indexingRestriction
	 *            {@link IIndexQueryRestriction} to check.
	 *
	 * @return <code>true</code> if the indexing restriction is fulfilled.
	 */
	private boolean isRestrictionFulfilled(Object object, IIndexQueryRestriction indexingRestriction) {
		List<String> methodNames = indexingRestriction.getQualifiedMethodNames();

		try {
			Object executeOn = object;
			for (String methodName : methodNames) {
				Method method = getMethod(executeOn, methodName);
				if (null == method) {
					return false;
				}
				executeOn = method.invoke(executeOn, new Object[0]);
			}

			return indexingRestriction.isFulfilled(executeOn);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error("Error in find object to execute indexing restricton check.", e);
			return false;
		}
	}

	/**
	 * Returns the {@link Method} for the given {@link Object} with the given method name.
	 *
	 * @param object
	 *            Object to find method on.
	 * @param methodName
	 *            Name of the method.
	 * @return Method if one can be found.
	 */
	private Method getMethod(Object object, String methodName) {
		int cacheKey = getMethodCacheKey(object.getClass(), methodName);
		Method method = cacheMap.get(cacheKey);

		if (method == null) { // method is not yet in cache
			try {
				Method methodFromClass = object.getClass().getMethod(methodName, new Class<?>[0]);
				Method existing = cacheMap.putIfAbsent(cacheKey, methodFromClass);
				if (null != existing) {
					methodFromClass = existing;
				}
				return methodFromClass;
			} catch (NoSuchMethodException e) {
				// not found, put marker method at this place in map
				cacheMap.putIfAbsent(cacheKey, markerMethod);
				return null;
			} catch (SecurityException | IllegalArgumentException e) {
				log.error("Error retrieve the method " + methodName + " for the object of class " + object.getClass(), e);
				return null;
			}
		} else if (markerMethod.equals(method)) {
			return null;
		} else {
			return method;
		}
	}

	/**
	 * Returns key for the hash map based on the supplied class and method name.
	 *
	 * @param clazz
	 *            class
	 * @param methodName
	 *            method name
	 * @return int key
	 */
	private int getMethodCacheKey(Class<?> clazz, String methodName) {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

}
