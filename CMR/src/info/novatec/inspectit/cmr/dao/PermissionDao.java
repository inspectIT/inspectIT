package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.usermanagement.Permission;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This DAO is used to handle all {@link Permission} objects.
 * 
 * @author Joshua Hartmann
 * 
 */
public interface PermissionDao {

	/**
	 * Load a specific {@link Permission} from the underlying storage by passing the title.
	 * 
	 * @param title
	 *            The title of the Permission.
	 * @return The found {@link Permission} object.
	 */
	Permission load(String title);


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

}
