package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.JmxDefinitionDataIdentDao;
import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.util.PlatformIdentCache;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * The default implementation of the {@link JmxDefinitionDataIdentDao} interface by using Entity
 * manager.
 * 
 * @author Alfred Krauss
 * @author Marius Oehler
 * 
 */
@Repository
public class JmxDefinitionDataIdentDaoImpl extends AbstractJpaDao<JmxDefinitionDataIdent> implements JmxDefinitionDataIdentDao {

	/**
	 * Default constructor.
	 */
	public JmxDefinitionDataIdentDaoImpl() {
		super(JmxDefinitionDataIdent.class);
	}

	/**
	 * {@link PlatformIdent} cache.
	 */
	@Autowired
	private PlatformIdentCache platformIdentCache;

	/**
	 * {@inheritDoc}
	 */
	public JmxDefinitionDataIdent load(Long id) {
		return getEntityManager().find(JmxDefinitionDataIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(JmxDefinitionDataIdent jmxDefinitionDataIdent) {
		// we save if id is not set, otherwise merge
		if (0L == jmxDefinitionDataIdent.getId()) {
			super.create(jmxDefinitionDataIdent);
		} else {
			super.update(jmxDefinitionDataIdent);
		}
		platformIdentCache.markDirty(jmxDefinitionDataIdent.getPlatformIdent());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<JmxDefinitionDataIdent> findForPlatformIdent(long platformId, JmxDefinitionDataIdent jmxDefinitionDataIdentExample) {
		TypedQuery<JmxDefinitionDataIdent> query = getEntityManager().createNamedQuery(JmxDefinitionDataIdent.FIND_BY_PLATFORM_AND_EXAMPLE, JmxDefinitionDataIdent.class);
		query.setParameter("platformIdentId", platformId);
		query.setParameter("mBeanObjectName", jmxDefinitionDataIdentExample.getmBeanObjectName());
		query.setParameter("mBeanAttributeName", jmxDefinitionDataIdentExample.getmBeanAttributeName());

		return query.getResultList();
	}
}
