package info.novatec.inspectit.cmr.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
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
import info.novatec.inspectit.cmr.security.SecurityInitialization;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Permutation;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.spring.logger.Log;

/**
 * Provides general security-system operations for client<->cmr interaction.
 * Watches over Data Integrity.
 * 
 * @author Andreas Herzog
 * @author Clemens Geibel
 * @author Lucca Hellriegel
 * @author Mario Rose
 * @author Joshua Hartmann
 * @author Phil Szalay
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
	CmrSecurityManager securityManager;

	/**
	 * Data Access Object.
	 */
	@Autowired
	RoleDao roleDao;
	
	/**
	 * Initialization-methods for the database.
	 */
	@Autowired
	SecurityInitialization securityInitialization;
	
	/**
	 * KeyPair.
	 */
	private KeyPair keyPair;

	/**
	 * Is executed after dependency injection is done to perform any
	 * initialization.
	 */
	@PostConstruct
	public void postConstruct() {
		try {
			setKeyPair();
		} catch (NoSuchAlgorithmException nsaEx) {
			log.info(nsaEx.getMessage());
		}
		
		if (log.isInfoEnabled()) {
			log.info("|-Security Service active...");
		}
	}

	// +-------------------------------------------------------------------------------------------+
	// | Communication with the Apache Shiro Security Framework |
	// +-------------------------------------------------------------------------------------------+

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(byte[] encryptedRandomKey, byte[] secondEncryptionLevel, String email) {
		String pw;
		try {
			pw = Permutation.hashString(Permutation.decryptPassword(encryptedRandomKey, secondEncryptionLevel, keyPair.getPrivate().getEncoded()));
		} catch (Exception e) {
			log.info(e.getMessage());
			return false;
		}
		
		UsernamePasswordToken token = new UsernamePasswordToken(email, pw);

		Subject currentUser = SecurityUtils.getSubject();

		User user = userDao.findByEmail(email);
		if (user == null || user.isLocked()) {
			return false;
		}

		if (!currentUser.isAuthenticated()) {
			try {
				currentUser.login(token);
				log.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");
			} catch (Exception uae) {
				log.info(uae.getMessage() + uae.getClass().toString());
				log.info("User [" + currentUser.getPrincipal() + "] failed to log in successfully.");
				currentUser.logout();
				return false;
			}
		}

		return true;
	}
	
	
	/**
	 * First Step of the modified login process.
	 * @param symmetricKey secretKeyBytes 
	 * @return symmetrically encrypted public key
	 * @throws Exception Exception
	 */
	@Override
	public byte[] callPublicKey(byte[] symmetricKey) throws Exception {
		byte[] encryptedPublicKey = Permutation.encryptPublicKey(keyPair.getPublic(), symmetricKey);
		return encryptedPublicKey;
	}
	
	/**
	 * Initializes the public/private keys.
	 * @throws NoSuchAlgorithmException if RSA not available.
	 */
	private void setKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(Permutation.ASYMMETRIC_ALGORITHM);
		generator.initialize(Permutation.ASYMMETRIC_KEY_SIZE);
	    this.keyPair = generator.generateKeyPair();
	}

	/**
	 * Ends the session.
	 */
	@Override
	public void logout() {
		SecurityUtils.getSubject().logout();
	}

	/**
	 * Returns whether the user is authenticated.
	 * 
	 * @return Returns whether the user is authenticated.
	 */
	public boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}

	/**
	 * Returns list of permissions.
	 * 
	 * @return List with the users permissions.
	 */
	@Override
	public List<Permission> getPermissions() {
		Subject currentUser = SecurityUtils.getSubject();

		List<Permission> grantedPermissions = new ArrayList<Permission>();
		List<Permission> existingPermissions = permissionDao.loadAll();
		for (int i = 0; i < existingPermissions.size(); i++) {
			if (currentUser.isPermitted(existingPermissions.get(i).getTitle())) {
				grantedPermissions.add(existingPermissions.get(i));
			}
		}

		return grantedPermissions;
	}

	// +-------------------------------------------------------------------------------------------+
	// | Managing Security Data in the Database |
	// +-------------------------------------------------------------------------------------------+

	/**
	 * Combines the integrity check for all security data types. Uniqueness etc.
	 * is specifically checked in every method.
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
			// TODO: make real data integrity tests
			return true;
		}

		return false;
	}

	// | USER |---------------
	@Override
	public List<String> getAllUsers() {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return new ArrayList<String>();
		}
		
		List<User> users = userDao.loadAll();
		List<String> userEmails = new ArrayList<String>();
		for (User user : users) {
			userEmails.add(user.getEmail());
		}

		return userEmails;
	}

	@Override
	public List<String> getUsersByRole(long id) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return new ArrayList<String>();
		}
		
		List<User> foundUsers = userDao.findByRole(id);
		List<String> userEmails = new ArrayList<String>();
		for (User user : foundUsers) {
			userEmails.add(user.getEmail());
		}

		return userEmails;
	}

	@Override
	public void addUser(User user) throws DataIntegrityViolationException {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		if (!checkDataIntegrity(user)) {
			throw new DataIntegrityViolationException("Data integrity test failed!");
		}
		User userWithSameEmail = userDao.findByEmail(user.getEmail());
		Role existingRole = roleDao.findByID(user.getRoleId());
		if (userWithSameEmail != null) {
			throw new DataIntegrityViolationException("User with this email does already exist!");
		} else if (existingRole == null) {
			throw new DataIntegrityViolationException("Invalid role id assigned to this user!");
		} else {
			String hashedPassword = "";
			try {
				hashedPassword = Permutation.hashString(Permutation.hashString(user.getPassword()));
			} catch (NoSuchAlgorithmException e) {
				log.info("NoSuchAlgorithException: Failed to create password hash. User not created!");
				return;
			}
			user.setPassword(hashedPassword);
			userDao.saveOrUpdate(user);
		}
	}

	@Override
	public User getUser(String email) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return null;
		}
		
		return userDao.findByEmail(email);
	}

	@Override
	public void deleteUser(User user) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		String currentName = (String) currentUser.getPrincipal();
		if (currentName.equals(user.getEmail())) {
			currentUser.logout();
		}
		userDao.delete(user);
	}

	@Override
	public void changeUserAttribute(User userOld, String email, String password, long roleID, boolean passwordChanged,
			boolean isLocked) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		String currentName = (String) currentUser.getPrincipal();
		if (currentName.equals(userOld.getEmail())) {
			currentUser.logout();
		}

		if (!email.equals(userOld.getEmail()) && userDao.findByEmail(email) != null) {
			throw new DataIntegrityViolationException("User with this email does already exist!");
		}
		if (roleDao.findByID(roleID) == null) {
			throw new DataIntegrityViolationException("Invalid role id assigned to this user!");
		}

		userOld.setEmail(email);
		userOld.setRoleId(roleID);
		userOld.setLocked(isLocked);
		if (passwordChanged) {
			String hashedPassword = "";
			try {
				hashedPassword = Permutation.hashString(password);
			} catch (NoSuchAlgorithmException e) {
				log.info("NoSuchAlgorithException: Failed to create password hash. User attributes no changed!");
				return;
			}
			userOld.setPassword(hashedPassword);
		}
		if (!checkDataIntegrity(userOld)) {
			throw new DataIntegrityViolationException("Data integrity test failed!");
		}
		userDao.saveOrUpdate(userOld);
	}

	@Override
	public boolean checkCurrentUser(User user) {
		Subject currentUser = SecurityUtils.getSubject();
		String currentName = (String) currentUser.getPrincipal();
		if (currentName.equals(user.getEmail())) {
			return true;
		}
		return false;
	}
	// | PERMISSION |---------

	@Override
	public void changePermissionDescription(Permission permission) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		changePermissionAttributes(permission, permission.getTitle(), permission.getDescription(),
				permission.getParameter());
	}

	@Override
	public void changePermissionAttributes(Permission perm, String newTitle, String newDescription,
			String newParamter) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		perm.setTitle(newTitle);
		perm.setDescription(newDescription);
		perm.setParameter(newParamter);

		permissionDao.saveOrUpdate(perm);
	}

	@Override
	public List<Permission> getAllPermissions() {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return new ArrayList<Permission>();
		}
		
		return permissionDao.loadAll();
	}

	// | ROLE | --------------
	@Override
	public Role getRoleByID(long id) throws DataRetrievalFailureException, DataIntegrityViolationException {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return null;
		}
		
		Role roles = roleDao.findByID(id);

		if (roles == null) {
			throw new DataRetrievalFailureException("No roles in the database matching the given id!");
		} else {
			return roles;
		}
	}

	@Override
	public Role getRoleOfUser(String email) throws AuthenticationException, DataIntegrityViolationException {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return null;
		}
		
		User foundUser = userDao.findByEmail(email);

		if (foundUser == null) {
			throw new DataRetrievalFailureException("No user in the database matching the given email!");
		} else {
			return getRoleByID(foundUser.getRoleId());
		}
	}

	@Override
	public List<Role> getAllRoles() {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return new ArrayList<Role>();
		}
		
		return roleDao.loadAll();
	}

	@Override
	public void addRole(String name, List<String> rolePermissions, String description)
			throws DataIntegrityViolationException {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
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
		Role role = new Role(name, grantedPermissions, description);

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

	@Override
	public void changeRoleDescription(Role role, String newDescription) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		changeRoleAttribute(role, role.getTitle(), newDescription, role.getPermissions());
	}

	@Override
	public void changeRoleAttribute(Role role, String newTitle, String newDescription,
			List<Permission> newPermissions) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		role.setTitle(newTitle);
		role.setDescription(newDescription);
		role.setPermissions(newPermissions);
		roleDao.saveOrUpdate(role);
	}

	@Override
	public void deleteRole(Role role) {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}
		
		roleDao.delete(role);
	}
	
	@Override
	public boolean checkCurrentRole(Role role) {
		Subject currentUser = SecurityUtils.getSubject();
		String currentName = (String) currentUser.getPrincipal();
		return getRoleOfUser(currentName).getTitle().equals(role.getTitle()); 
		
	}
	@Override
	public void resetDB() {
		if (!securityManager.isPermitted("cmrAdministrationPermission")) {
			return;
		}

		/**
		 * All users logout.
		 */
		DefaultSessionManager sm = (DefaultSessionManager) securityManager.getSessionManager();

		for (Session session : sm.getSessionDAO().getActiveSessions()) {
			new Subject.Builder().session(session).buildSubject().logout();
		}

		userDao.deleteAll(userDao.loadAll());
		roleDao.deleteAll(roleDao.loadAll());
		permissionDao.deleteAll(permissionDao.loadAll());

		securityInitialization.start();
	}
}