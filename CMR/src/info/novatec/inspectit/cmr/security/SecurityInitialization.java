package info.novatec.inspectit.cmr.security;

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
				
		//Declaration of permissions. Name must be the same as in plugin.xml
		Permission cmrRecordingPermission = new Permission(1, "cmrRecordingPermission", "Permission start recording from Agent");
		Permission cmrShutdownAndRestartPermission = new Permission(2, "cmrShutdownAndRestartPermission", "Permission for shuting down and restarting the CMR");
		Permission cmrDeleteAgentPermission = new Permission(3, "cmrDeleteAgentPermission", "Permission for deleting Agent");
		Permission cmrStoragePermission = new Permission(4, "cmrStoragePermission", "Permission for accessing basic storage options");
		Permission cmrAdministrationPermission = new Permission(5, "cmrAdministrationPermission", "Permission for accessing the CMR Administration");
		
		//Transfers permissions to database.
		permissionDao.saveOrUpdate(cmrRecordingPermission);
		permissionDao.saveOrUpdate(cmrShutdownAndRestartPermission);
		permissionDao.saveOrUpdate(cmrDeleteAgentPermission);
		permissionDao.saveOrUpdate(cmrStoragePermission);
		permissionDao.saveOrUpdate(cmrAdministrationPermission);
		
		//Predefined roles
		Role freshUser = new Role(1, "freshUser", null);
		Role restrictedUser = new Role(2, "restrictedUser", Arrays.asList(cmrRecordingPermission, cmrStoragePermission));
		Role adminUser = new Role(3, "adminUser", Arrays.asList(cmrRecordingPermission, cmrStoragePermission , cmrDeleteAgentPermission , cmrShutdownAndRestartPermission, cmrAdministrationPermission));
		
		//Transfers roles to database.
		roleDao.saveOrUpdate(freshUser);
		roleDao.saveOrUpdate(restrictedUser);
		roleDao.saveOrUpdate(adminUser);
		
		
		System.out.println(roleDao.loadAll().toString());

		//Standarduser - changes with login
		User admin = new User(Permutation.hashString("admin"), "admin", adminUser.getId());
		
		//Testusers
		//TODO: delete before final merging
		User restricted = new User(Permutation.hashString("restricted"), "restricted", restrictedUser.getId());
		User fresh = new User(Permutation.hashString("fresh"), "freshUser", freshUser.getId());
		userDao.saveOrUpdate(fresh);
		userDao.saveOrUpdate(restricted);
		
		//Transfers users to databse.		
		userDao.saveOrUpdate(admin);
			   
	}

}
