package rocks.inspectit.server.dao.impl;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.JmxSensorTypeIdentDao;
import rocks.inspectit.server.util.PlatformIdentCache;
import rocks.inspectit.shared.all.cmr.model.JmxSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

/**
 * The default implementation of the {@link JmxSensorTypeIdentDao}  interface by using Entity
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
	 * {@link PlatformIdent} cache.
	 */
	@Autowired
	private PlatformIdentCache platformIdentCache;

	/**
	 * {@inheritDoc}
	 */
	public List<JmxSensorTypeIdent> findByExample(long platformId, JmxSensorTypeIdent jmxSensorTypeIdent) {
		TypedQuery<JmxSensorTypeIdent> query = getEntityManager().createNamedQuery(JmxSensorTypeIdent.FIND_BY_PLATFORM, JmxSensorTypeIdent.class);
		query.setParameter("platformIdentId", platformId);

		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public JmxSensorTypeIdent load(Long id) {
		return getEntityManager().find(JmxSensorTypeIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(JmxSensorTypeIdent jmxSensorTypeIdent) {
		// we save if id is not set, otherwise merge
		if (null == jmxSensorTypeIdent.getId()) {
			super.create(jmxSensorTypeIdent);
		} else {
			super.update(jmxSensorTypeIdent);
		}
		platformIdentCache.markDirty(jmxSensorTypeIdent.getPlatformIdent());
	}

}
