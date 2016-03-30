package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.communication.data.cmr.Permission;

/**
 * The default implementation of the {@link PermissionDao} interface by using
 * the Entity Manager.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * @author Lucca Hellriegel
 * 
 */
@Repository
public class PermissionDaoImpl extends AbstractJpaDao<Permission>implements PermissionDao {
	/**
	 * Default constructor.
	 */
	public PermissionDaoImpl() {
		super(Permission.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void delete(Permission permission) {
		EntityManager em = getEntityManager();		
		em.remove(em.merge(permission));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void deleteAll(List<Permission> permissions) {
		for (Permission permission : permissions) {
			delete(permission);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Permission> loadAll() {
		return getEntityManager().createNamedQuery(Permission.FIND_ALL, Permission.class).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Permission findById(long id) {
		TypedQuery<Permission> query = getEntityManager().createNamedQuery(Permission.FIND_BY_ID, Permission.class);				
		query.setParameter("id", id);		
		List<Permission> results = query.getResultList();
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
	public void saveOrUpdate(Permission permission) {
		if (permission.getId() == null) {
			super.create(permission);
		} else {
			super.update(permission);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Permission findByTitle(String title) {
		TypedQuery<Permission> query = getEntityManager().createNamedQuery(Permission.FIND_BY_TITLE, Permission.class);
		query.setParameter("title", title);
		List<Permission> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
}
