package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.cmr.usermanagement.Permission;

/**
 * The default implementation of the {@link PermissionDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Joshua Hartmann
 * 
 */
@Repository
public class PermissionDaoImpl extends HibernateDaoSupport implements PermissionDao {

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
	public PermissionDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(Permission permission) {
		getHibernateTemplate().delete(permission);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<Permission> permissions) {
		getHibernateTemplate().deleteAll(permissions);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Permission> loadAll() {
		return getHibernateTemplate().loadAll(Permission.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Permission> findByExample(Permission permission) {
		return getHibernateTemplate().findByExample(permission);
	}

	/**
	 * {@inheritDoc}
	 */
	public Permission load(String title) {
		return (Permission) getHibernateTemplate().get(Permission.class, title);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(Permission permission) {
		getHibernateTemplate().saveOrUpdate(permission);
	}


}
