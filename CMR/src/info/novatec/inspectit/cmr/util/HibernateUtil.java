package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.util.IHibernateUtil;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

/**
 * Our own Hibernate utility class.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class HibernateUtil implements IHibernateUtil {

	/**
	 * {@inheritDoc}
	 */
	public boolean isInitialized(Object proxy) {
		return Hibernate.isInitialized(proxy);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPersistentCollection(Class<?> collectionClass) {
		return PersistentCollection.class.isAssignableFrom(collectionClass);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPersistentMap(Class<?> collectionClass) {
		return PersistentMap.class.isAssignableFrom(collectionClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPersistentList(Class<?> collectionClass) {
		return PersistentList.class.isAssignableFrom(collectionClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPersistentSet(Class<?> collectionClass) {
		return PersistentSet.class.isAssignableFrom(collectionClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProxy(Class<?> proxyClass) {
		return ArrayUtils.contains(proxyClass.getInterfaces(), HibernateProxy.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getUnproxiedObject(Object proxy) {
		// getImplementation will try to initialize the object
		// but our objects should already be initialized, thus should work with no problem
		if (proxy instanceof HibernateProxy) {
			return ((HibernateProxy) proxy).getHibernateLazyInitializer().getImplementation();
		}
		return proxy;
	}

}
