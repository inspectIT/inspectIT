package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.JmxDefinitionDataIdentDao;
import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.util.PlatformIdentCache;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * The default implementation of the {@link JmxDefinitionDataIdentDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Alfred Krauss
 * 
 */
@Repository
public class JmxDefinitionDataIdentDaoImpl extends HibernateDaoSupport implements JmxDefinitionDataIdentDao {

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 *            the hibernate session factory.
	 */
	@Autowired
	public JmxDefinitionDataIdentDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
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
		return (JmxDefinitionDataIdent) getHibernateTemplate().get(JmxDefinitionDataIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(JmxDefinitionDataIdent jmxDefinitionDataIdent) {
		getHibernateTemplate().saveOrUpdate(jmxDefinitionDataIdent);
		platformIdentCache.markDirty(jmxDefinitionDataIdent.getPlatformIdent());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<JmxDefinitionDataIdent> findForPlatformIdent(long platformId, JmxDefinitionDataIdent jmxDefinitionDataIdentExample) {

		DetachedCriteria jmxDefinitionDataCriteria = DetachedCriteria.forClass(JmxDefinitionDataIdent.class);

		jmxDefinitionDataCriteria.add(Restrictions.eq("mBeanAttributeName", jmxDefinitionDataIdentExample.getmBeanAttributeName()));
		jmxDefinitionDataCriteria.add(Restrictions.eq("mBeanObjectName", jmxDefinitionDataIdentExample.getmBeanObjectName()));

		jmxDefinitionDataCriteria.setFetchMode("platformIdent", FetchMode.JOIN);
		DetachedCriteria platformCriteria = jmxDefinitionDataCriteria.createCriteria("platformIdent");
		platformCriteria.add(Restrictions.eq("id", platformId));

		return getHibernateTemplate().findByCriteria(jmxDefinitionDataCriteria);
	}
}
