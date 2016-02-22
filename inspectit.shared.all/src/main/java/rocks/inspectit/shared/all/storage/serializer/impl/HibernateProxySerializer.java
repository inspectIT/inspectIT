package rocks.inspectit.shared.all.storage.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import rocks.inspectit.shared.all.util.IHibernateUtil;

/**
 * This serializer is unproxing the Hibernate proxies and passes the correctly initialized entity to
 * the delegate serializer. Note that delegate serializer must be correct for the underlying entity
 * class, otherwise the write will fail.
 * 
 * @author Ivan Senic
 * 
 */
public class HibernateProxySerializer extends Serializer<Object> {

	/**
	 * {@link IHibernateUtil}.
	 */
	private IHibernateUtil hibernateUtil;

	/**
	 * Delegate serializer.
	 */
	private Serializer<Object> delegateSerializer;

	/**
	 * Default constructor.
	 * 
	 * @param hibernateUtil
	 *            Hibernate util. Must not be <code>null</code>.
	 * @param delegateSerializer
	 *            Delegate constructor. Must not be <code>null</code>.
	 */
	public HibernateProxySerializer(IHibernateUtil hibernateUtil, Serializer<Object> delegateSerializer) {
		if (null == hibernateUtil) {
			throw new IllegalArgumentException("HibernateUtil must not be null.");
		}
		if (null == delegateSerializer) {
			throw new IllegalArgumentException("Delegate serializer can not be null.");
		}
		this.hibernateUtil = hibernateUtil;
		this.delegateSerializer = delegateSerializer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object read(Kryo kryo, Input input, Class<Object> type) {
		throw new RuntimeException("HibernateProxySerializer should never be used for reading.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(Kryo kryo, Output output, Object object) {
		delegateSerializer.write(kryo, output, hibernateUtil.getUnproxiedObject(object));
	}

}
