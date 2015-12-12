package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.communication.data.cmr.Permission;

/**
 * The default implementation of the {@link PermissionDao} interface by using the
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
	public Permission load(long id) {
		return (Permission) getHibernateTemplate().get(Permission.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(Permission permission) {
		//title is unique, it should return exactly this permission but with the correct id		
		Permission tmpPermission = findOneByExample(permission);
		//if the given permission is present in the database, adapt the id of the permission
		if (tmpPermission != null) {
			permission.setId(tmpPermission.getId());
			tmpPermission = null;
		}
		getHibernateTemplate().saveOrUpdate(permission);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Permission> findByTitle(Permission permission) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Permission.class);
		criteria.add(Restrictions.eq("title", permission.getTitle()));
		return getHibernateTemplate().findByCriteria(criteria);
	}
	
	///**
	// * {@inheritDoc}
	// */
	/*
	@SuppressWarnings("unchecked")
	public Permission findByCriteria(Permission permission, boolean id, boolean title, boolean desciption) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Permission.class);
		if (id) {
			criteria.add(Restrictions.eq("id", permission.getId()));			
		}
		if (title) {
			criteria.add(Restrictions.eq("title", permission.getTitle()));			
		}
		if (desciption) {
			criteria.add(Restrictions.eq("desciption", permission.getDescription()));			
		}
		List<Permission> possiblePermissions = getHibernateTemplate().findByCriteria(criteria);
		
		if (possiblePermissions.size() > 1) {
			return null; //hier evtl auch ein fehler werfen
		} else if (possiblePermissions.size() == 1) {
			return possiblePermissions.get(0);
		} else {
			return null;
		}
	}*/
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Permission findOneByExample(Permission permission) {
		List<Permission> possiblePermissions = getHibernateTemplate().findByExample(permission);
		
		if (possiblePermissions.size() > 1) {
			return null; //hier evtl auch ein fehler werfen
		} else if (possiblePermissions.size() == 1) {
			return possiblePermissions.get(0);
		} else {
			return null;
		}
	}
}
