package rocks.inspectit.server.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Abstract JPA DAO class that can execute simple operations on an entity class.
 *
 * @author Ivan Senic
 *
 * @param <T>
 *            Type of entities to load.
 */
public abstract class AbstractJpaDao<T> {

	/**
	 * Type of entity to load with this DAO.
	 */
	private Class<T> entityType;

	/**
	 * Entity manager.
	 */
	@PersistenceContext
	EntityManager entityManager;

	/**
	 * @param entityType
	 *            Type of entity to load with this DAO.
	 */
	public AbstractJpaDao(Class<T> entityType) {
		this.entityType = entityType;
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

	/**
	 * Find entity by id.
	 *
	 * @param id
	 *            Not <code>null</code> id value
	 *
	 * @return Loaded entity or <code>null</code>
	 */
	public T load(Long id) {
		return entityManager.find(entityType, id);
	}

	/**
	 * Creates new entity.
	 *
	 * @param object
	 *            Object to persist.
	 */
	public void create(T object) {
		getEntityManager().persist(object);
	}

	/**
	 * Updates an entity object.
	 *
	 * @param object
	 *            Object to update.
	 * @return Updated object.
	 */
	public T update(T object) {
		return getEntityManager().merge(object);
	}

	/**
	 * Deletes an object by executing {@link #entityManager} remove method.
	 *
	 * @param object
	 *            Object to delete.
	 */
	public void delete(T object) {
		AbstractJpaDao.delete(entityManager, object);
	}

	/**
	 * Deletes collection of objects.
	 *
	 * @param objects
	 *            Objects to remove.
	 */
	public void deleteAll(List<T> objects) {
		for (T object : objects) {
			delete(object);
		}
	}

	/**
	 * Gets {@link #entityManager}.
	 *
	 * @return {@link #entityManager}
	 */
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}
