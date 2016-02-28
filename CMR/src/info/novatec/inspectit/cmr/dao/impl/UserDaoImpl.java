package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.communication.data.cmr.User;

/**
 * The default implementation of the {@link UserDao} interface by using the
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
public class UserDaoImpl extends HibernateDaoSupport implements UserDao {

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
	public UserDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(User user) {
		getHibernateTemplate().delete(user);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<User> users) {
		getHibernateTemplate().deleteAll(users);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> loadAll() {
		return getHibernateTemplate().loadAll(User.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<User> findByExample(User user) {
		return getHibernateTemplate().findByExample(user);
	}

	/**
	 * {@inheritDoc}
	 */
	public User load(String email) {
		return (User) getHibernateTemplate().get(User.class, email);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(User user) {
		getHibernateTemplate().saveOrUpdate(user);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<User> findByEmail(String email) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("email", email));
		return getHibernateTemplate().findByCriteria(criteria);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<User> findByRole(long roleId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("roleId", roleId));
		return getHibernateTemplate().findByCriteria(criteria);
	}

}
