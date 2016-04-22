package rocks.inspectit.server.dao.impl;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.MethodIdentToSensorTypeDao;
import rocks.inspectit.shared.all.cmr.model.MethodIdentToSensorType;

/**
 * The default implementation of the {@link MethodIdentToSensorTypeDao} interface by using Entity
 * manager.
 *
 * @author Ivan Senic
 *
 */
@Repository
public class MethodIdentToSensorTypeDaoImpl extends AbstractJpaDao<MethodIdentToSensorType> implements MethodIdentToSensorTypeDao {

	/**
	 * Default constructor.
	 */
	public MethodIdentToSensorTypeDaoImpl() {
		super(MethodIdentToSensorType.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveOrUpdate(MethodIdentToSensorType methodIdentToSensorType) {
		// we save if id is not set, otherwise merge
		if (null == methodIdentToSensorType.getId()) {
			super.create(methodIdentToSensorType);
		} else {
			super.update(methodIdentToSensorType);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodIdentToSensorType find(long methodIdentId, long methodSensorTypeIdentId) {
		TypedQuery<MethodIdentToSensorType> query = getEntityManager().createNamedQuery(MethodIdentToSensorType.FIND_FOR_METHOD_ID_AND_METOHD_SENSOR_TYPE_ID, MethodIdentToSensorType.class);
		query.setParameter("methodIdentId", methodIdentId);
		query.setParameter("methodSensorTypeIdentId", methodSensorTypeIdentId);

		List<MethodIdentToSensorType> results = query.getResultList();
		if (1 == results.size()) {
			return results.get(0);
		} else {
			return null;
		}
	}
}
