package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.cmr.Permission;

import java.util.List;

/**
 * This DAO is used to handle all {@link Permission} objects.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * 
 */
public interface PermissionDao {
	/**
	 * Load a specific {@link Permission} from the underlying storage by passing
	 * the id.
	 * 
	 * @param id
	 *            The id of the Permission.
	 * @return The found {@link Permission} object.
	 */
	Permission findById(long id);

	/**
	 * Saves or updates this {@link Permission} in the underlying storage.
	 * 
	 * @param permission
	 *            The {@link Permission} object to save or update.
	 */
	void saveOrUpdate(Permission permission);

	/**
	 * Deletes this specific {@link Permission} object.
	 * 
	 * @param permission
	 *            The {@link Permission} object to delete.
	 */
	void delete(Permission permission);

	/**
	 * Deletes all {@link Permission} objects which are stored in the passed
	 * list.
	 * 
	 * @param permissions
	 *            The list containing the {@link Permission} objects to delete.
	 */
	void deleteAll(List<Permission> permissions);

	/**
	 * Returns all {@link Permission} objects which are saved in the underlying
	 * storage.
	 * 
	 * @return Returns all stored {@link Permission} objects.
	 */
	List<Permission> loadAll();

	/**
	 * Returns a permission object with the same title as the parameter.
	 * 
	 * @param title
	 *            the title of the permission
	 * @return a permission
	 */
	Permission findByTitle(String title);
}
