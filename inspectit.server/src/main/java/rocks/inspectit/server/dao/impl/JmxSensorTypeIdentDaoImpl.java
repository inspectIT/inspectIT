package rocks.inspectit.server.dao.impl;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.JmxSensorTypeIdentDao;
import rocks.inspectit.shared.all.cmr.model.JmxSensorTypeIdent;

/**
 * The default implementation of the {@link JmxSensorTypeIdentDao} interface by using Entity
 * manager.
 *
 * @author Alfred Krauss
 * @author Marius Oehler
 *
 */
@Repository
public class JmxSensorTypeIdentDaoImpl extends AbstractJpaDao<JmxSensorTypeIdent> implements JmxSensorTypeIdentDao {

	/**
	 * Default constructor.
	 */
	public JmxSensorTypeIdentDaoImpl() {
		super(JmxSensorTypeIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<JmxSensorTypeIdent> findByExample(long platformId, JmxSensorTypeIdent jmxSensorTypeIdent) {
		TypedQuery<JmxSensorTypeIdent> query = getEntityManager().createNamedQuery(JmxSensorTypeIdent.FIND_BY_PLATFORM, JmxSensorTypeIdent.class);
		query.setParameter("platformIdentId", platformId);

		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JmxSensorTypeIdent load(Long id) {
		return getEntityManager().find(JmxSensorTypeIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveOrUpdate(JmxSensorTypeIdent jmxSensorTypeIdent) {
		// we save if id is not set, otherwise merge
		if (null == jmxSensorTypeIdent.getId()) {
			super.create(jmxSensorTypeIdent);
		} else {
			super.update(jmxSensorTypeIdent);
		}
	}

}
