package info.novatec.inspectit.util;

/**
 * Interface for Hibernate utility class.
 * 
 * @author Ivan Senic
 * 
 */
public interface IHibernateUtil {

	/**
	 * Checks if the Hibernate proxy or collection is initialized.
	 * 
	 * @param proxy
	 *            Proxy or collection
	 * @return True if it initialized, false otherwise.
	 */
	boolean isInitialized(Object proxy);

	/**
	 * Checks if the given class is Hibernate persistent collection.
	 * 
	 * @param collectionClass
	 *            Class to check.
	 * @return True if is Hibernate persistent collection.
	 */
	boolean isPersistentCollection(Class<?> collectionClass);

	/**
	 * Checks if the given class is Hibernate persistent map.
	 * 
	 * @param collectionClass
	 *            Class to check.
	 * @return True if is Hibernate persistent map.
	 */
	boolean isPersistentMap(Class<?> collectionClass);

	/**
	 * Checks if the given class is Hibernate persistent list.
	 * 
	 * @param collectionClass
	 *            Class to check.
	 * @return True if is Hibernate persistent list.
	 */
	boolean isPersistentList(Class<?> collectionClass);

	/**
	 * Checks if the given class is Hibernate persistent set.
	 * 
	 * @param collectionClass
	 *            Class to check.
	 * @return True if is Hibernate persistent set.
	 */
	boolean isPersistentSet(Class<?> collectionClass);

	/**
	 * Returns if the class is implementing HibernateProxy interface.
	 * 
	 * @param proxyClass
	 *            Class to check.
	 * @return Returns if the class is implementing HibernateProxy interface.
	 */
	boolean isProxy(Class<?> proxyClass);

	/**
	 * If given object is a proxy, returns the initialized entity.
	 * 
	 * @param proxy
	 *            Proxy object.
	 * @return Initialized entity.
	 */
	Object getUnproxiedObject(Object proxy);

}