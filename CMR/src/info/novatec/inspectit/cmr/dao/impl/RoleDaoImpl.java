package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.usermanagement.Role;

/**
 * The default implementation of the {@link RoleDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Joshua Hartmann
 * 
 */
@Repository
public class RoleDaoImpl extends HibernateDaoSupport implements RoleDao {

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
	public RoleDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(Role role) {
		getHibernateTemplate().delete(role);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<Role> roles) {
		getHibernateTemplate().deleteAll(roles);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Role> loadAll() {
		return getHibernateTemplate().loadAll(Role.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Role> findByExample(Role role) {
		return getHibernateTemplate().findByExample(role);
	}

	/**
	 * {@inheritDoc}
	 */
	public Role load(String title) {
		return (Role) getHibernateTemplate().get(Role.class, title);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(Role role) {
		getHibernateTemplate().saveOrUpdate(role);
	}


}
