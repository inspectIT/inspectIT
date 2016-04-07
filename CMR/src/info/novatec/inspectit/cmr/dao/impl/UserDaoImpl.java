package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.communication.data.cmr.User;

/**
 * The default implementation of the {@link UserDao} interface by using the
 * Entity Manager.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * @author Lucca Hellriegel
 * 
 */
@Repository
public class UserDaoImpl extends AbstractJpaDao<User>implements UserDao {
	/**
	 * Default constructor.
	 */
	public UserDaoImpl() {
		super(User.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void delete(User user) {
		EntityManager em = getEntityManager();		
		em.remove(em.merge(user));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void deleteAll(List<User> users) {
		for (User user : users) {
			delete(user);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> loadAll() {
		return getEntityManager().createNamedQuery(User.FIND_ALL, User.class).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User findByEmail(String email) {
		TypedQuery<User> query = getEntityManager().createNamedQuery(User.FIND_BY_EMAIL, User.class);
		query.setParameter("email", email);
		List<User> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void saveOrUpdate(User user) {
		if (user.getId() == null) {
			super.create(user);
		} else {
			super.update(user);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> findByRole(long roleId) {
		TypedQuery<User> query = getEntityManager().createNamedQuery(User.FIND_BY_ROLE_ID, User.class);
		query.setParameter("roleId", roleId);
		List<User> results = query.getResultList();
		return results;
	}
}
