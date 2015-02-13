package info.novatec.inspectit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Helper class that caches the class lookup.
 * 
 * @author Stefan Siegl
 */
public class ClassLookupCache {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ClassLookupCache.class);

	/**
	 * Internal cache.
	 */
	Cache<String, Class<?>> cache = CacheBuilder.newBuilder().weakKeys().softValues().build();

	/**
	 * looks up the class from the associated cache. Automatically fills the cache when not within
	 * the cache already. If the class lookup fails, returns null.
	 * 
	 * @param name the class to look up.
	 * @return the instance of the class or null if the lookup fails.
	 */
	public Class<?> getClassForName(String name) {
		if (null == name) {
			return null;
		}

		Class<?> clazz = cache.getIfPresent(name);
		if (null == clazz) {
			try {
				clazz = Class.forName(name);
			} catch (ClassNotFoundException e) {
				LOG.warn("Cannot find class " + name, e);
				clazz = null; // NOPMD NOCHK
			}
			cache.put(name, clazz);
		}
		return clazz;
	}
}
