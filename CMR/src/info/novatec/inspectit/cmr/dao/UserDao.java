package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.cmr.User;

import java.util.List;

/**
 * This DAO is used to handle all {@link User} objects.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * 
 */
public interface UserDao {
	/**
	 * Get User by Email-Address.
	 * 
	 * @param email
	 *            email
	 * @return a User object with matching Email-Address.
	 */
	User findByEmail(String email);

	/**
	 * Get User by Role.
	 * 
	 * @param roleId
	 *            roleId
	 * @return a User object with matching Role.
	 */
	List<User> findByRole(long roleId);

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
	 * Returns all {@link User} objects which are saved in the underlying
	 * storage.
	 * 
	 * @return Returns all stored {@link User} objects.
	 */
	List<User> loadAll();
}
