package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.communication.data.cmr.Role;

/**
 * The default implementation of the {@link RoleDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
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
	@SuppressWarnings("unchecked")
	public Role findByTitle(String title) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Role.class);
		criteria.add(Restrictions.eq("title", title));
		List<Role> possibleRoles = getHibernateTemplate().findByCriteria(criteria);
		
		if (possibleRoles.size() > 1) {
			//if there is more than one, we don't know which to choose, so just return nul
			return null;
		} else if (possibleRoles.size() == 1) {
			return possibleRoles.get(0);
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Role load(long id) {
		return (Role) getHibernateTemplate().get(Role.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(Role role) {
		//title is unique, it should return exactly this role but with the correct id				
		Role tmpRole = findOneByExample(role);
				
		//if the given permission is already present in the database, set the corresponding id of the role
		if (tmpRole != null) {
			role.setId(tmpRole.getId());
		}
		getHibernateTemplate().saveOrUpdate(role);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Role> findByID(long id) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Role.class);
		criteria.add(Restrictions.eq("id", id));
		return getHibernateTemplate().findByCriteria(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Role findOneByExample(Role role) {
		List<Role> possibleRoles = getHibernateTemplate().findByExample(role);
		
		if (possibleRoles.size() > 1) {
			//if there is more than one, we don't know which to choose, so just return nul
			return null;
		} else if (possibleRoles.size() == 1) {
			return possibleRoles.get(0);
		} else {
			return null;
		}
	}
}
