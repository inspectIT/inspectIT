package rocks.inspectit.server.dao.impl;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.JmxDefinitionDataIdentDao;
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;

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
	 * {@inheritDoc}
	 */
	@Override
	public JmxDefinitionDataIdent load(Long id) {
		return getEntityManager().find(JmxDefinitionDataIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveOrUpdate(JmxDefinitionDataIdent jmxDefinitionDataIdent) {
		// we save if id is not set, otherwise merge
		if (0L == jmxDefinitionDataIdent.getId()) {
			super.create(jmxDefinitionDataIdent);
		} else {
			super.update(jmxDefinitionDataIdent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<JmxDefinitionDataIdent> findForPlatformIdent(long platformId, JmxDefinitionDataIdent jmxDefinitionDataIdentExample) {
		TypedQuery<JmxDefinitionDataIdent> query = getEntityManager().createNamedQuery(JmxDefinitionDataIdent.FIND_BY_PLATFORM_AND_EXAMPLE, JmxDefinitionDataIdent.class);
		query.setParameter("platformIdentId", platformId);
		query.setParameter("mBeanObjectName", jmxDefinitionDataIdentExample.getmBeanObjectName());
		query.setParameter("mBeanAttributeName", jmxDefinitionDataIdentExample.getmBeanAttributeName());

		return query.getResultList();
	}
}
