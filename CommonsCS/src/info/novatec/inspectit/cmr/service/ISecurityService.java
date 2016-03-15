package info.novatec.inspectit.cmr.service;

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
 * @author Mario Rose
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
	 * @return whether the login was successful
	 */
	boolean authenticate(String pw, String email);

	/**
	 * Ends the session.
	 * 
	 */
	void logout();
	
	/**
	 * Returns whether the user is authenticated.
	 * 
	 * @return Returns whether the user is authenticated.
	 */
	boolean isAuthenticated();


	// | ROLE | --------------
	/**
	 * Returns a Role object with given Email of the user.
	 * 
	 * @param email
	 *            email
	 * @return a Role object with given Email of the user.
	 */
	Role getRoleOfUser(String email);
	
	/**
	 * Changes the description of the role, just a wrapper for changeRoleAttributes.
	 * @param role the role to be changed
	 * @param newDescription the new description
	 */
	void changeRoleDescription(Role role, String newDescription);
	
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
	 * @param description Description of the role.
	 */
	void addRole(String name, List<String> rolePermissions, String description);
	/**
	 * Method to edit the attributes of a role.
	 * @param role
	 * 		the role to edit
	 * @param newDescription
	 * 		new description of the role
	 * @param newTitle
	 * 		new title of the role
	 * @param newPermissions
	 * 		list of new permissions
	 */
	void changeRoleAttribute(Role role, String newTitle, String newDescription, List<Permission> newPermissions);
	
	/**
	 * Deletes the given Role Object from the Database.
	 * 
	 * @param role
	 *            role
	 */
	void deleteRole(Role role);

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
	 *     	 user
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
	 * @param isLocked
	 * 		boolean to see if user was locked by admin
	 */
	void changeUserAttribute(User userOld, String email, String password, long roleID, boolean passwordChanged, boolean isLocked);

	// | PERMISSION |---------	
	
	/**
	 * Changes all Attributes of the given Permission.
	 * @param perm The Permission to be modified.
	 * @param newTitle The new title of the permission.
	 * @param newDescription The new description of the permission.
	 * @param newParamter The new parameter of the permission.
	 */
	void changePermissionAttributes(Permission perm, String newTitle, String newDescription, String newParamter);
	
	/**
	 * Change the description of a Permission. Other changes should not be possible, just a wrapper for changePermissionAttributes.
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
	 * Changes the parameter for a permission, just a wrapper for changePermissionAttributes.
	 * @param permission
	 * 				the permission with the actualized parameter.
	 */
	void changePermissionParameter(Permission permission);	

	/**
	 * Returns titles of permissions as Strings.
	 * 
	 * @return List with the users permissions.
	 */
	List<Permission> getPermissions();

}
