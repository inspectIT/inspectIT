package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.MethodSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

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
	public List<MethodSensorTypeIdent> findByClassNameAndPlatformId(String fullyQualifiedClassName, long platformId) {
		TypedQuery<MethodSensorTypeIdent> query = getEntityManager().createNamedQuery(MethodSensorTypeIdent.FIND_BY_CLASS_AND_PLATFORM_ID, MethodSensorTypeIdent.class);
		query.setParameter("fullyQualifiedClassName", fullyQualifiedClassName);
		query.setParameter("platformIdent", platformId);

		return query.getResultList();
	}
}
