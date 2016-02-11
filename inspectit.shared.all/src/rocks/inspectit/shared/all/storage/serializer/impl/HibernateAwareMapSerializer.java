package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.util.IHibernateUtil;

import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

/**
 * Map serializer that check if the map is of hibernate type and if it is not initialized just
 * writes the empty map.
 * 
 * @author Ivan Senic
 * 
 */
public class HibernateAwareMapSerializer extends MapSerializer {

	/**
	 * {@link IHibernateUtil} to use.
	 */
	private IHibernateUtil hibernateUtil;

	/**
	 * Default constructor.
	 * 
	 * @param hibernateUtil
	 *            {@link IHibernateUtil} to use. If <code>null</code> is provided this serializer
	 *            will behave as {@link MapSerializer}.
	 */
	public HibernateAwareMapSerializer(IHibernateUtil hibernateUtil) {
		this.hibernateUtil = hibernateUtil;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void write(Kryo kryo, Output output, Map map) {
		if (null != hibernateUtil) {
			if (hibernateUtil.isPersistentMap(map.getClass()) && !hibernateUtil.isInitialized(map)) {
				// if Hibernate map is not initialized just write the empty map
				int length = 0;
				output.writeInt(length, true);
				return;
			}
		}
		super.write(kryo, output, map);
	}
}
