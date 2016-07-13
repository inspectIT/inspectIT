package rocks.inspectit.server.util;

import javax.persistence.EntityManager;

/**
 * Utility class for JPA operations.
 *
 * @author Ivan Senic
 *
 */
public final class JpaUtil {

	/**
	 * Private constructor.
	 */
	private JpaUtil() {
	}

	/**
	 * Small util to correctly delete object from database. If object is not contained in the
	 * current session denoted by given entity manager it will be merged first.
	 *
	 * @param <T>
	 *            type of object
	 * @param entityManager
	 *            {@link EntityManager} to be used for deletion.
	 * @param object
	 *            Persisted object to delete.
	 */
	public static <T> void delete(EntityManager entityManager, T object) {
		if (entityManager.contains(object)) {
			entityManager.remove(object);
		} else {
			T merged = entityManager.merge(object);
			entityManager.remove(merged);
		}
	}
}
