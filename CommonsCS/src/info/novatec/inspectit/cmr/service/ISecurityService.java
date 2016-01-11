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
	 * Method to add a new role.
	 * @param name
	 * 				Name of role.
	 * @param rolePermissions
	 * 				Permissions of role in string-form.
	 */
	void addRole(String name, List<String> rolePermissions);

	// | USER |---------------
	/**
	 * We only want to send the user emails to the client. If a user is about to be modified, other
	 * data will be retrieved.
	 * 
	 * @return An List containing all user emails
	 */
	List<String> getAllUsers();

	/**
	 * Should return all the users with the given roleID.
	 * @param id
	 * 			Given roleID.
	 * @return List<String>
	 * 				Found User by email.
	 */
	List<String> getUsersByRole(long id);
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
	 * 
	 * @param userOld 
	 * 		the user that is edited and now needs to be deleted
	 * @param email
	 * 		the new email
	 * @param password
	 * 		the new password
	 * @param roleID
	 * 		the new roleID
	 * @param passwordChanged
	 * 		boolean to see if password was changed and needs to be hashed
	 */
	void changeUserAttribute(User userOld, String email, String password, long roleID, boolean passwordChanged);

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

	

	

}

