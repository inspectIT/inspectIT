package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.cmr.Role;

import java.util.List;

/**
 * This DAO is used to handle all {@link Role} objects.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * 
 */
public interface RoleDao {

	/**
	 * Find a Users Role.
	 * 
	 * @param id
	 *            The ID of a role.
	 * @return The corresponding role.
	 */
	Role findByID(long id);

	/**
	 * Searches for a role with the given title.
	 * 
	 * @param title
	 *            The title
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
	 * Returns all {@link Role} objects which are saved in the underlying
	 * storage.
	 * 
	 * @return Returns all stored {@link Role} objects.
	 */
	List<Role> loadAll();
}
