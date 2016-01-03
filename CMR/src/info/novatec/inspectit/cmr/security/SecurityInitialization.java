package info.novatec.inspectit.cmr.security;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Permutation;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;

/**
 * 
 * @author Joshua Hartmann
 * @author Lucca Hellriegel
 *
 */
public class SecurityInitialization {
	/**
	 * PermissionDao.
	 */
	@Autowired
	private PermissionDao permissionDao;
	/**
	 * RoleDao.
	 */
	@Autowired
	private RoleDao roleDao;
	/**
	 * UserDao.
	 */
	@Autowired
	private UserDao userDao;

	/**
	 * Initializes the database with the given roles and permissions.
	 */
	public void start() {
		Permission cmrRecordingPermission = new Permission("cmrRecordingPermission", "Permission start recording from Agent");
		Permission cmrShutdownAndRestartPermission = new Permission("cmrShutdownAndRestartPermission", "Permission for shuting down and restarting the CMR");
		Permission cmrDeleteAgentPermission = new Permission("cmrDeleteAgentPermission", "Permission for deleting Agent");
		Permission cmrStoragePermission = new Permission("cmrStoragePermission", "Permission for accessing basic storage options");
		Permission cmrAdministrationPermission = new Permission("cmrAdministrationPermission", "Permission for accessing the CMR Administration");
				
		//Transfers permissions to database.
		permissionDao.saveOrUpdate(cmrRecordingPermission);
		permissionDao.saveOrUpdate(cmrShutdownAndRestartPermission);
		permissionDao.saveOrUpdate(cmrDeleteAgentPermission);
		permissionDao.saveOrUpdate(cmrStoragePermission);
		permissionDao.saveOrUpdate(cmrAdministrationPermission);
		
		//Predefined roles
		Role freshUser = new Role("freshRole", new ArrayList<Permission>());
		Role restrictedUser = new Role("restrictedRole", Arrays.asList(cmrRecordingPermission, cmrStoragePermission));
		Role adminUser = new Role("adminRole", Arrays.asList(cmrRecordingPermission, cmrStoragePermission, cmrDeleteAgentPermission, cmrShutdownAndRestartPermission, cmrAdministrationPermission));
		
		//Transfers roles to database.
		roleDao.saveOrUpdate(freshUser);
		roleDao.saveOrUpdate(restrictedUser);
		roleDao.saveOrUpdate(adminUser);
			
		//Standarduser - has to be changed on first login
		User admin = new User(Permutation.hashString("admin"), "admin", adminUser.getId());
				
		//Transfers users to databse.		
		userDao.saveOrUpdate(admin);			   
	}
}
