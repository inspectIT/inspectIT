package info.novatec.inspectit.cmr.security;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Permutation;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
/**
 * Provides administrative methods for the CMR.
 * 
 * TODO: restrict access to this methods!
 * 
 * @author Joshua Hartmann
 *
 */
public class Administration {
	/**
	 * Data Access Object.
	 */
	@Autowired
	UserDao userDao;

	/**
	 * Data Access Object.
	 */
	@Autowired
	RoleDao roleDao;
	
	/**
	 * Adds a new User for the CMR.
	 * @param email The email of the user.
	 * @param roleId The ID of the users role.
	 * @return the assigned password.
	 */
	public String addUser(String email, int roleId)	{
		String password = generatePassword(10);
		addUser(email, password, roleId);
		return password;
	}
	
	/**
	 * Adds a new User for the CMR.
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @param roleId The ID of the users role.
	 */
	private void addUser(String email, String password, int roleId)	{
		User user = new User(Permutation.hashString(password), email, roleId);
		
		userDao.saveOrUpdate(user);
	}
	
	/**
	 * Generates a random password.
	 * @param length Length of the password.
	 * @return The generated password.
	 */
	private String generatePassword(int length) {		
		return RandomStringUtils.randomAlphanumeric(length);
	}
	
	/**
	 * Deletes the specified user.
	 * @param email Email of the user.
	 */
	public void deleteUser(String email) {
		User user = userDao.load(email);
		userDao.delete(user);
	}
	
	/**
	 * Changes the User attributes in the database.
	 * @param oldEmail The email specifying the user to be modiefied
	 * @param newEmail The new email the user should have
	 * @param newPassword The new password the user should have
	 * @param newRole The new role the user should have
	 */
	public void changeUser(String oldEmail, String newEmail, String newPassword, int newRole) {
		deleteUser(oldEmail);
		addUser(newEmail, newPassword, newRole);
	}	
	
	/**
	 * Adds a new Role to the CMR. 
	 * @param id The id of the new Role.
	 * @param title The title of the new role.
	 * @param permissions The permissions assigned to this role.
	 */
	public void addRole(int id, String title, List<Permission> permissions) {
		Role role = new Role(id, title, permissions);
		
		roleDao.saveOrUpdate(role);
	}	
}
