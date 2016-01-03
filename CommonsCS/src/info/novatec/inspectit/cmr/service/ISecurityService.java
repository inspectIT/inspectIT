package info.novatec.inspectit.cmr.service;

import java.io.Serializable;
import java.util.List;

import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;

/**
 * Provides general security operations for client<->cmr interaction.
 * 
 * @author Andreas Herzog
 * @author Clemens Geibel
 * @author Lucca Hellriegel
 * @author Joshua Hartmann
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ISecurityService {
	/**
	 * Authentication via the CmrSecurityManager.
	 * 
	 * @param pw
	 *            users password
	 * @param email
	 *            email
	 * @return sessionId if the user was authenticated
	 */
	Serializable authenticate(String pw, String email);

	/**
	 * Ends the session.
	 * 
	 * @param sessionId
	 *            Session id from the session to end
	 */
	void logout(Serializable sessionId);

	/**
	 * Checks whether session of a specific sessionId exists.
	 * 
	 * @param sessionId
	 *            The id to check.
	 * @return Boolean whether the session exists.
	 */
	boolean existsSession(Serializable sessionId);

	/**
	 * Returns titles of permissions as Strings.
	 * 
	 * @param sessionId
	 *            sessionId
	 * @return List with the users permissions.
	 */
	List<String> getPermissions(Serializable sessionId);

	// | ROLE | --------------
	/**
	 * Returns a Role object with given Email of the user.
	 * 
	 * @param email
	 *            email
	 * @return a Role object with given Email of the user.
	 * @throws AuthenticationException
	 *             if the email was not found.
	 */
	Role getRoleOfUser(String email);

	/**
	 * Searches for the Role matching a given ID.
	 * 
	 * @param id
	 *            RoleID.
	 * @return Role with this ID.
	 */
	Role getRoleByID(long id);

	/**
	 * Retrieves all existing roles.
	 * 
	 * @return An List containing all Roles
	 */
	List<Role> getAllRoles();

	/**
	 * Adds a new Role to the CMR.
	 * 
	 * @param title
	 *            The title of the new role.
	 * @param permissions
	 *            The permissions assigned to this role.
	 * @return The id of the created Role.
	 */
	// int addRole(String title, List<Permission> permissions);

	// | USER |---------------
	/**
	 * We only want to send the user emails to the client. If a user is about to be modified, other
	 * data will be retrieved.
	 * 
	 * @return An List containing all user emails
	 */
	List<String> getAllUsers();

	/**
	 * Adds a new User to the Database. Throws an exception, if there is an existing registered User
	 * with the given email-address. Throws an exception, if the given role-id does not exist.
	 * 
	 * @param user
	 *            user
	 */
	void addUser(User user);

	/**
	 * Returns the user object with the given email.
	 * 
	 * @param email
	 *            Email address of the user.
	 * @return The user object.
	 */
	User getUser(String email);

	/**
	 * Deletes the given User Object from the Database.
	 * 
	 * @param user
	 *            user
	 */
	void deleteUser(User user);

	/**
	 * Change any attribute of a User. Email cannot be changed.
	 * 
	 * @param user
	 *            user
	 */
	void changeUserAttribute(User user);

	// | PERMISSION |---------
	/**
	 * Change the description of a Permission. Other changes should not be possible.
	 * 
	 * @param permission
	 *            permission
	 */
	void changePermissionDescription(Permission permission);

	/**
	 * Retrieves all existing permissions.
	 * 
	 * @return An List containing all Roles
	 */
	List<Permission> getAllPermissions();

	/**
	 * Method to add a new role.
	 * @param name
	 * 				Name of role.
	 * @param rolePermissions
	 * 				Permissions of role in string-form.
	 */
	void addRole(String name, List<String> rolePermissions);
}

