package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.PlatformSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

/**
 * The default implementation of the {@link PlatformSensorTypeIdentDao} interface by using the
 * Entity manager.
 * 
 * @author Patrice Bouillet
 * 
 */
@Repository
public class PlatformSensorTypeIdentDaoImpl extends AbstractJpaDao<PlatformSensorTypeIdent> implements PlatformSensorTypeIdentDao {

	/**
	 * Default constructor.
	 */
	public PlatformSensorTypeIdentDaoImpl() {
		super(PlatformSensorTypeIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(PlatformSensorTypeIdent platformSensorTypeIdent) {
		if (null == platformSensorTypeIdent.getId()) {
			super.create(platformSensorTypeIdent);
		} else {
			super.update(platformSensorTypeIdent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PlatformSensorTypeIdent> findAll() {
		return getEntityManager().createNamedQuery(PlatformSensorTypeIdent.FIND_ALL, PlatformSensorTypeIdent.class).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PlatformSensorTypeIdent> findByClassNameAndPlatformId(String fullyQualifiedClassName, long platformId) {
		TypedQuery<PlatformSensorTypeIdent> query = getEntityManager().createNamedQuery(PlatformSensorTypeIdent.FIND_BY_CLASS_AND_PLATFORM_ID, PlatformSensorTypeIdent.class);
		query.setParameter("fullyQualifiedClassName", fullyQualifiedClassName);
		query.setParameter("platformIdent", platformId);

		return query.getResultList();
	}

}
