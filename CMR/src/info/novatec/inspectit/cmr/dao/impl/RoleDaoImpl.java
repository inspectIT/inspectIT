package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.communication.data.cmr.Role;

/**
 * The default implementation of the {@link RoleDao} interface by using the
 * Entity Manager.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * @author Lucca Hellriegel
 * 
 */
@Repository
public class RoleDaoImpl extends AbstractJpaDao<Role>implements RoleDao {
	/**
	 * Default constructor.
	 */
	public RoleDaoImpl() {
		super(Role.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void delete(Role role) {
		EntityManager em = getEntityManager();		
		em.remove(em.merge(role));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void deleteAll(List<Role> roles) {
		for (Role role : roles) {
			delete(role);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Role> loadAll() {
		return getEntityManager().createNamedQuery(Role.FIND_ALL, Role.class).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Role findByTitle(String title) {
		TypedQuery<Role> query = getEntityManager().createNamedQuery(Role.FIND_BY_TITLE, Role.class);
		query.setParameter("title", title);
		List<Role> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Role findByID(long id) {
		TypedQuery<Role> query = getEntityManager().createNamedQuery(Role.FIND_BY_ID, Role.class);
		query.setParameter("id", id);
		List<Role> results = query.getResultList();
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
	public void saveOrUpdate(Role role) {
		if (role.getId() == null) {
			super.create(role);
		} else {
			super.update(role);
		}
	}
}
