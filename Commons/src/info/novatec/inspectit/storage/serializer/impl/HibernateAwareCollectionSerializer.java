package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.util.IHibernateUtil;

import java.util.Collection;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

/**
 * Collections serializer that check if the collection is of hibernate type and if it is not
 * initialized just writes the empty collection.
 * 
 * @author Ivan Senic
 * 
 */
public class HibernateAwareCollectionSerializer extends CollectionSerializer {

	/**
	 * {@link IHibernateUtil} to use.
	 */
	private IHibernateUtil hibernateUtil;

	/**
	 * Default constructor.
	 * 
	 * @param hibernateUtil
	 *            {@link IHibernateUtil} to use. If <code>null</code> is provided this serializer
	 *            will behave as {@link CollectionSerializer}.
	 */
	public HibernateAwareCollectionSerializer(IHibernateUtil hibernateUtil) {
		this.hibernateUtil = hibernateUtil;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void write(Kryo kryo, Output output, Collection collection) {
		if (null != hibernateUtil) {
			if (hibernateUtil.isPersistentCollection(collection.getClass()) && !hibernateUtil.isInitialized(collection)) {
				// if Hibernate collection is not initialized just write the empty collection
				int length = 0;
				output.writeInt(length, true);
				return;
			}
		}
		super.write(kryo, output, collection);
	}

}
