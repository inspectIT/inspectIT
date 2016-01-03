package info.novatec.inspectit.cmr.service;

import java.io.Serializable;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;

import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.cmr.security.CmrSecurityManager;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Permutation;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.spring.logger.Log;

/**
 * Provides general security-system operations for client<->cmr interaction. Watches over Data
 * Integrity.
 * 
 * @author Andreas Herzog
 * @author Clemens Geibel
 * @author Lucca Hellriegel
 */
@Service
public class SecurityService implements ISecurityService {

	/**
	 * Logger of this Class.
	 */
	@Log
	Logger log;

	/**
	 * Data Access Object.
	 */
	@Autowired
	UserDao userDao;

	/**
	 * Data Access Object.
	 */
	@Autowired
	PermissionDao permissionDao;

	/**
	 * Manager for general security purposes.
	 */
	@Autowired
	CmrSecurityManager cmrSecurityManager;

	/**
	 * Data Access Object.
	 */
	@Autowired
	RoleDao roleDao;

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 */
	@PostConstruct
	public void postConstruct() {
		SecurityUtils.setSecurityManager(cmrSecurityManager);
		if (log.isInfoEnabled()) {
			log.info("|-Security Service active...");
		}
	}

	// +-------------------------------------------------------------------------------------------+
	// | Communication with the Apache Shiro Security Framework |
	// +-------------------------------------------------------------------------------------------+

	/**
	 * Authentication via the CmrSecurityManager.
	 * 
	 * @param pw
	 *            users password
	 * @param email
	 *            email
	 * @return sessionId if the user was authenticated
	 */
	@Override
	public Serializable authenticate(String pw, String email) {
		UsernamePasswordToken token = new UsernamePasswordToken(email, pw);
		PrincipalCollection identity = new SimplePrincipalCollection(email, "cmrRealm");

		Subject currentUser = new Subject.Builder().principals(identity).buildSubject();

		if (!currentUser.isAuthenticated()) {
			try {
				currentUser.login(token);
				log.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");
			} catch (Exception uae) {
				log.info(uae.getMessage() + uae.getClass().toString());
				log.info("User [" + currentUser.getPrincipal() + "] failed to log in successfully.");
				currentUser.logout();
				return null;
			}
		}

		return currentUser.getSession().getId();
	}

	/**
	 * Ends the session.
	 * 
	 * @param sessionId
	 *            Session id from the session to end
	 */
	@Override
	public void logout(Serializable sessionId) {
		if (existsSession(sessionId)) {
			Subject currentUser = new Subject.Builder().sessionId(sessionId).buildSubject();
			log.info("SessionId [" + currentUser.getSession(false).getId() + "], Name [" + currentUser.getPrincipal() + "].");
			currentUser.logout();
			log.info("Logged out successfully.");
		}
	}

	/**
	 * Returns titles of permissions as Strings.
	 * 
	 * @param sessionId
	 *            sessionId
	 * @return List with the users permissions.
	 */
	@Override
	public List<String> getPermissions(Serializable sessionId) {
		Subject currentUser = new Subject.Builder().sessionId(sessionId).buildSubject();

		List<String> grantedPermissions = new ArrayList<String>();
		List<Permission> existingPermissions = permissionDao.loadAll();
		for (int i = 0; i < existingPermissions.size(); i++) {
			if (currentUser.isPermitted(existingPermissions.get(i).getTitle())) {
				grantedPermissions.add(existingPermissions.get(i).getTitle());
			}
		}

		return grantedPermissions;
	}

	/**
	 * Checks whether session of a specific sessionId exists.
	 * 
	 * @param sessionId
	 *            The id to check.
	 * @return Boolean whether the session exists.
	 */
	@Override
	public boolean existsSession(Serializable sessionId) {
		DefaultSessionManager sm = (DefaultSessionManager) cmrSecurityManager.getSessionManager();
		for (Session session : sm.getSessionDAO().getActiveSessions()) {
			if (session.getId().equals(sessionId)) {
				return true;
			}
		}
		return false;
	}

	// +-------------------------------------------------------------------------------------------+
	// | Managing Security Data in the Database |
	// +-------------------------------------------------------------------------------------------+

	/**
	 * Combines the integrity check for all security data types. Uniqueness etc. is specifically
	 * checked in every method.
	 * 
	 * @param data
	 *            data
	 * @return true, if the tested object passes all integrity checks.
	 */
	private boolean checkDataIntegrity(Object data) {
		if (data instanceof User) {
			User user = (User) data;
			return (user.getEmail().length() <= 256);
		} else if (data instanceof Permission) {
			Permission permission = (Permission) data;
			return (permission.getDescription().length() < 100);
		} else if (data instanceof Role) {
			//TODO: make real data integrity tests
			Role role = (Role) data;
			return true;
		}
		
		return false;
	}

	// | USER |---------------
	@Override
	public List<String> getAllUsers() {
		List<User> users = userDao.loadAll();
		List<String> userEmails = new ArrayList<String>();
		for (User user : users) {
			userEmails.add(user.getEmail());
		}

		return userEmails;
	}

	@Override
	public void addUser(User user) throws DataIntegrityViolationException {
		if (!checkDataIntegrity(user)) {
			throw new DataIntegrityViolationException("Data integrity test failed!");
		}
		List<User> sameEmail = userDao.findByEmail(user.getEmail());
		List<Role> existingRole = roleDao.findByID(user.getRoleId());
		if (!sameEmail.isEmpty()) {
			throw new DataIntegrityViolationException("User with this email does already exist!");
		} else if (existingRole.isEmpty()) {
			throw new DataIntegrityViolationException("Invalid role id assigned to this user!");
		} else {
			String hashedPassword = Permutation.hashString(user.getPassword());
			user.setPassword(hashedPassword);
			userDao.saveOrUpdate(user);
					
		}
	}

	@Override
	public User getUser(String email) {
		return userDao.load(email);
	}

	@Override
	public void deleteUser(User user) {
		userDao.delete(user);
	}

	@Override
	public void changeUserAttribute(User user) throws DataIntegrityViolationException, DataRetrievalFailureException {
		List<User> foundUsers = userDao.findByEmail(user.getEmail());
		if (!checkDataIntegrity(user)) {
			throw new DataIntegrityViolationException("Data integrity test failed!");
		} else if (foundUsers.size() == 1) {
			userDao.delete(foundUsers.get(0));
			userDao.saveOrUpdate(user);
		} else if (foundUsers.size() > 1) {
			throw new DataIntegrityViolationException("Multiple users with same email found!");
		} else {
			throw new DataRetrievalFailureException("The user you wanted to update does not exist!");
		}
	}

	// | PERMISSION |---------
	@Override
	public void changePermissionDescription(Permission permission) {
		List<Permission> foundPermissions = permissionDao.findByTitle(permission);
		if (!checkDataIntegrity(permission)) {
			throw new DataIntegrityViolationException("Data integrity test failed!");
		} else if (foundPermissions.size() == 1) {
			permissionDao.delete(foundPermissions.get(0));
			permissionDao.saveOrUpdate(permission);
		} else if (foundPermissions.size() > 1) {
			throw new DataIntegrityViolationException("Multiple permissions with same title found!");
		} else {
			throw new DataRetrievalFailureException("The permission you wanted to update does not exist!");
		}
	}

	@Override
	public List<Permission> getAllPermissions() {
		return permissionDao.loadAll();
	}

	// | ROLE | --------------
	@Override
	public Role getRoleByID(long id) throws DataRetrievalFailureException, DataIntegrityViolationException {
		List<Role> roles = roleDao.findByID(id);
		if (roles.size() == 1) {
			return roles.get(0);
		} else if (roles.isEmpty()) {
			throw new DataRetrievalFailureException("No roles in the database matching the given id!");
		} else {
			throw new DataIntegrityViolationException("Multiple roles with the same id in the database!");
		}
	}

	@Override
	public Role getRoleOfUser(String email) throws AuthenticationException, DataIntegrityViolationException {
		List<User> foundUsers = userDao.findByEmail(email);
		if (foundUsers.isEmpty()) {
			throw new AuthenticationException("Email or password is incorrect.");
		} else if (foundUsers.size() != 1) {
			throw new DataIntegrityViolationException("There are multiple users with same email.");
		} else {
			User user = foundUsers.get(0);
			return getRoleByID(user.getRoleId());
		}
	}

	@Override
	public List<Role> getAllRoles() {
		return roleDao.loadAll();
	}

	@Override
	public void addRole(String name, List<String> rolePermissions) throws DataIntegrityViolationException {
		List<Permission> allPermissions = getAllPermissions();
		List<Permission> grantedPermissions = new ArrayList<Permission>();
		for (int i = 0; i < rolePermissions.size(); i++) {
			for (int y = 0; y < allPermissions.size(); y++) {
				if (rolePermissions.get(i).equals(allPermissions.get(y).getTitle())) {
					grantedPermissions.add(allPermissions.get(y));
					break;
					}
			}
		}
	   Role role = new Role(name, grantedPermissions);
		
		
		if (!checkDataIntegrity(role)) {
			throw new DataIntegrityViolationException("Data integrity test failed!");
		}
		List<Role> allRole = roleDao.loadAll();
		if (allRole.contains(role)) {
			throw new DataIntegrityViolationException("Role already exist!");
		} else {
			
			roleDao.saveOrUpdate(role);
		}
	}

	// TODO Make more methods available for the administrator module...
}