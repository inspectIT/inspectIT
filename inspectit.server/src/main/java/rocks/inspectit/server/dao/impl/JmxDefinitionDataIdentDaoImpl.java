package rocks.inspectit.server.dao.impl;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
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
	public List<Long> findIdForPlatformIdent(long platformId, JmxDefinitionDataIdent jmxDefinitionDataIdentExample, boolean updateTimestamp) {
		TypedQuery<Long> query = getEntityManager().createNamedQuery(JmxDefinitionDataIdent.FIND_ID_BY_PLATFORM_AND_EXAMPLE, Long.class);
		query.setParameter("platformIdentId", platformId);
		query.setParameter("mBeanObjectName", jmxDefinitionDataIdentExample.getmBeanObjectName());
		query.setParameter("mBeanAttributeName", jmxDefinitionDataIdentExample.getmBeanAttributeName());
		List<Long> resultList = query.getResultList();

		if (updateTimestamp && CollectionUtils.isNotEmpty(resultList)) {
			Query updateQuery = getEntityManager().createNamedQuery(JmxDefinitionDataIdent.UPDATE_TIMESTAMP);
			updateQuery.setParameter("ids", resultList);
			updateQuery.executeUpdate();
		}

		return resultList;
	}
}
