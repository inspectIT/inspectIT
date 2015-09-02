package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.usermanagement.User;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This DAO is used to handle all {@link User} objects.
 * 
 * @author Joshua Hartmann
 * 
 */
public interface UserDao {

	/**
	 * Load a specific {@link User} from the underlying storage by passing the id.
	 * 
	 * @param id
	 *            The id of the User.
	 * @return The found {@link User} object.
	 */
	User load(Long id);


	/**
	 * Execute a findByExample query against the underlying storage.
	 * 
	 * @param user
	 *            The {@link User} object which serves as the example.
	 * @return The list of {@link User} objects which have the same contents as the passed
	 *         example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<User> findByExample(User user);

	/**
	 * Saves or updates this {@link User} in the underlying storage.
	 * 
	 * @param user
	 *            The {@link User} object to save or update.
	 */
	void saveOrUpdate(User user);

	/**
	 * Deletes this specific {@link User} object.
	 * 
	 * @param user
	 *            The {@link User} object to delete.
	 */
	void delete(User user);

	/**
	 * Deletes all {@link User} objects which are stored in the passed list.
	 * 
	 * @param users
	 *            The list containing the {@link User} objects to delete.
	 */
	void deleteAll(List<User> users);

	/**
	 * Returns all {@link User} objects which are saved in the underlying storage.
	 * 
	 * @return Returns all stored {@link User} objects.
	 */
	List<User> loadAll();

}
