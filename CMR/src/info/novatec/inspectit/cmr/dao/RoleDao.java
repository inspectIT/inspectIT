package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.cmr.Role;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This DAO is used to handle all {@link Role} objects.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
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
	Role load(long id);

	/**
	 * Find a Users Role.
	 * @param id the Role ID of a User.
	 * @return List of the assigned roles.
	 */
	List<Role> findByID(long id);
	
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
	 * Searches for a role with the given title.
	 * @param title The title
	 * @return The role
	 */
	Role findByTitle(String title);
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
	
	/**
	 * Searches for a Role in the Database matching the example.
	 * E.g. when a Role Object is created, the id field is not set and with this we can get the Role Object with the corresponding id in the Database.
	 * @param role A sample Role
	 * @return A matching Role if found, null if not found or multiple entries
	 */
	Role findOneByExample(Role role);
}
