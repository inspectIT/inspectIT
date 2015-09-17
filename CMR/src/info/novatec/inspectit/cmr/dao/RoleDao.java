package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.usermanagement.Role;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This DAO is used to handle all {@link Role} objects.
 * 
 * @author Joshua Hartmann
 * 
 */
public interface RoleDao {

	/**
	 * Load a specific {@link Role} from the underlying storage by passing the id.
	 * 
	 * @param id
	 *            The id of the Role.
	 * @return The found {@link Role} object.
	 */
	Role get(Long id);


	/**
	 * Execute a findByExample query against the underlying storage.
	 * 
	 * @param role
	 *            The {@link Role} object which serves as the example.
	 * @return The list of {@link Role} objects which have the same contents as the passed
	 *         example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<Role> findByExample(Role role);

	/**
	 * Saves or updates this {@link Role} in the underlying storage.
	 * 
	 * @param role
	 *            The {@link Role} object to save or update.
	 */
	void saveOrUpdate(Role role);

	/**
	 * Deletes this specific {@link Role} object.
	 * 
	 * @param role
	 *            The {@link Role} object to delete.
	 */
	void delete(Role role);

	/**
	 * Deletes all {@link Role} objects which are stored in the passed list.
	 * 
	 * @param roles
	 *            The list containing the {@link Role} objects to delete.
	 */
	void deleteAll(List<Role> roles);

	/**
	 * Returns all {@link Role} objects which are saved in the underlying storage.
	 * 
	 * @return Returns all stored {@link Role} objects.
	 */
	List<Role> loadAll();
}
