package rocks.inspectit.server.dao.impl;

import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.MethodSensorTypeIdentDao;
import rocks.inspectit.shared.all.cmr.model.MethodSensorTypeIdent;

/**
 * The default implementation of the {@link MethodSensorTypeIdentDao} interface by using the Entity
 * manager.
 *
 * @author Patrice Bouillet
 *
 */
@Repository
public class MethodSensorTypeIdentDaoImpl extends AbstractJpaDao<MethodSensorTypeIdent> implements MethodSensorTypeIdentDao {

	/**
	 * Default constructor.
	 */
	public MethodSensorTypeIdentDaoImpl() {
		super(MethodSensorTypeIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveOrUpdate(MethodSensorTypeIdent methodSensorTypeIdent) {
		// we save if id is not set, otherwise merge
		if (null == methodSensorTypeIdent.getId()) {
			super.create(methodSensorTypeIdent);
		} else {
			super.update(methodSensorTypeIdent);
		}
	}

	@Override
	public List<MethodSensorTypeIdent> findAll() {
		return getEntityManager().createNamedQuery(MethodSensorTypeIdent.FIND_ALL, MethodSensorTypeIdent.class).getResultList();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Long> findIdByClassNameAndPlatformId(String fullyQualifiedClassName, long platformId) {
		TypedQuery<Long> query = getEntityManager().createNamedQuery(MethodSensorTypeIdent.FIND_ID_BY_CLASS_AND_PLATFORM_ID, Long.class);
		query.setParameter("fullyQualifiedClassName", fullyQualifiedClassName);
		query.setParameter("platformIdent", platformId);

		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateParameters(Long id, Map<String, Object> parameters) {
		Query query = getEntityManager().createNamedQuery(MethodSensorTypeIdent.UPDATE_PARAMETERS);
		query.setParameter("id", id);
		query.setParameter("parameters", parameters);

		query.executeUpdate();
	}
}
