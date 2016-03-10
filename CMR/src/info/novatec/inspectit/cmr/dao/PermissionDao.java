package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.cmr.Permission;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This DAO is used to handle all {@link Permission} objects.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * 
 */
public interface PermissionDao {

	/**
	 * Load a specific {@link Permission} from the underlying storage by passing the id.
	 * 
	 * @param id
	 *            The id of the Permission.
	 * @return The found {@link Permission} object.
	 */
	Permission load(long id);


	/**
	 * Execute a findByExample query against the underlying storage.
	 * 
	 * @param permission
	 *            The {@link Permission} object which serves as the example.
	 * @return The list of {@link Permission} objects which have the same contents as the passed
	 *         example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<Permission> findByExample(Permission permission);

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
	 * Deletes all {@link Permission} objects which are stored in the passed list.
	 * 
	 * @param permissions
	 *            The list containing the {@link Permission} objects to delete.
	 */
	void deleteAll(List<Permission> permissions);

	/**
	 * Returns all {@link Permission} objects which are saved in the underlying storage.
	 * 
	 * @return Returns all stored {@link Permission} objects.
	 */
	List<Permission> loadAll();

	/**
	 * Returns a permission object with the same title as the parameter.
	 * @param title the title of the permission
	 * @return a permission
	 */
	Permission findByTitle(String title);
	
	/**
	 * Searches for a Permission in the Database matching the example.
	 * E.g. when a Permission Object is created, the id field is not set and with this we can get the Permission Object with the corresponding id in the Database.
	 * @param permission A sample permission
	 * @return A matching Permission if found, null if not found or multiple entries
	 */
	Permission findOneByExample(Permission permission);
}
