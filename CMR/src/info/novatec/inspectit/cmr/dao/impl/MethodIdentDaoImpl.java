package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.MethodIdentDao;
import info.novatec.inspectit.cmr.model.MethodIdent;
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
 * The default implementation of the {@link MethodIdentDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Patrice Bouillet
 * 
 */
@Repository
public class MethodIdentDaoImpl extends HibernateDaoSupport implements MethodIdentDao {

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
	public MethodIdentDaoImpl(SessionFactory sessionFactory) {
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
	public void delete(MethodIdent methodIdent) {
		getHibernateTemplate().delete(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<MethodIdent> methodIdents) {
		getHibernateTemplate().deleteAll(methodIdents);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MethodIdent> findAll() {
		return getHibernateTemplate().loadAll(MethodIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodIdent> findByExample(MethodIdent methodIdent) {
		return getHibernateTemplate().findByExample(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodIdent load(Long id) {
		return (MethodIdent) getHibernateTemplate().get(MethodIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(MethodIdent methodIdent) {
		getHibernateTemplate().saveOrUpdate(methodIdent);
		platformIdentCache.markDirty(methodIdent.getPlatformIdent());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodIdent> findForPlatformIdent(long platformId, MethodIdent methodIdentExample) {
		DetachedCriteria methodCriteria = DetachedCriteria.forClass(MethodIdent.class);
		if (null == methodIdentExample.getPackageName()) {
			methodCriteria.add(Restrictions.isNull("packageName"));
		} else {
			methodCriteria.add(Restrictions.eq("packageName", methodIdentExample.getPackageName()));
		}
		methodCriteria.add(Restrictions.eq("className", methodIdentExample.getClassName()));
		methodCriteria.add(Restrictions.eq("methodName", methodIdentExample.getMethodName()));
		methodCriteria.add(Restrictions.eq("parameters", methodIdentExample.getParameters()));
		methodCriteria.add(Restrictions.eq("returnType", methodIdentExample.getReturnType()));

		methodCriteria.setFetchMode("platformIdent", FetchMode.JOIN);
		DetachedCriteria platformCriteria = methodCriteria.createCriteria("platformIdent");
		platformCriteria.add(Restrictions.eq("id", platformId));

		return getHibernateTemplate().findByCriteria(methodCriteria);
	}

}
