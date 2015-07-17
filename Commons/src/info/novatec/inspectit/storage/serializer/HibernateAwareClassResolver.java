package info.novatec.inspectit.storage.serializer;

import info.novatec.inspectit.storage.serializer.impl.HibernateProxySerializer;
import info.novatec.inspectit.util.IHibernateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultClassResolver;

/**
 * Class resolver that writes java collections and maps instead of the hibernate ones.
 * <p>
 * The current mapping is:
 * <ul>
 * <li>Hibernate PersistentSet -> HashSet</li>
 * <li>Hibernate PersistentMap -> HashMap</li>
 * <li>Hibernate PersistentList -> ArrayList</li>
 * </ul>
 * <p>
 * Also it intercepts the Hibernate proxies, writes the correct entity class and returns the alerted
 * registration that has a {@link HibernateProxySerializer} delegating to the correct serializer for
 * the entity.
 * 
 * @author Ivan Senic
 * 
 */
public class HibernateAwareClassResolver extends DefaultClassResolver {

	/**
	 * {@link IHibernateUtil} to use.
	 */
	private IHibernateUtil hibernateUtil;

	/**
	 * Map for caching altered registrations for the proxies.
	 */
	private final Map<Class<?>, Registration> hibernateProxiesRegistrations;

	/**
	 * Default constructor.
	 * 
	 * @param hibernateUtil
	 *            {@link IHibernateUtil} to use.
	 */
	public HibernateAwareClassResolver(IHibernateUtil hibernateUtil) {
		if (null == hibernateUtil) {
			throw new IllegalArgumentException("Hibernate util is needed with creation of Hibernate aware class resolver");
		}
		this.hibernateUtil = hibernateUtil;
		this.hibernateProxiesRegistrations = new HashMap<Class<?>, Registration>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Registration writeClass(Output output, Class type) {
		Class<?> writeType = type;
		if (null != type) {
			if (hibernateUtil.isPersistentList(type)) {
				writeType = ArrayList.class; // NOPMD
			} else if (hibernateUtil.isPersistentSet(type)) {
				writeType = HashSet.class; // NOPMD
			} else if (hibernateUtil.isPersistentMap(type)) {
				writeType = HashMap.class; // NOPMD
			} else if (hibernateUtil.isProxy(writeType)) {
				writeType = writeType.getSuperclass();
				Registration registration = super.writeClass(output, writeType);
				Registration returnRegistration = hibernateProxiesRegistrations.get(writeType);
				if (null == returnRegistration) {
					returnRegistration = new Registration(registration.getType(), new HibernateProxySerializer(hibernateUtil, registration.getSerializer()), registration.getId());
					hibernateProxiesRegistrations.put(writeType, returnRegistration);
				}
				return returnRegistration;
			}
		}
		return super.writeClass(output, writeType);
	}
}
