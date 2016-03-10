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
		if (permissionDao.loadAll().isEmpty()) {

			Permission cmrRecordingPermission = new Permission("cmrRecordingPermission", "Permission to start recording from Agent");
			Permission cmrShutdownAndRestartPermission = new Permission("cmrShutdownAndRestartPermission", "Permission for shutting down and restarting the CMR");
			Permission cmrDeleteAgentPermission = new Permission("cmrDeleteAgentPermission", "Permission for deleting an Agent");
			Permission cmrStoragePermission = new Permission("cmrStoragePermission", "Permission for accessing basic storage options");
			Permission cmrAdministrationPermission = new Permission("cmrAdministrationPermission", "Permission for accessing the CMR Administration");
			Permission cmrLookAtAgentsPermission = new Permission("cmrLookAtAgentsPermission", "General permission to look at agents.");

			
			//Transfers permissions to database.
			permissionDao.saveOrUpdate(cmrRecordingPermission);
			permissionDao.saveOrUpdate(cmrShutdownAndRestartPermission);
			permissionDao.saveOrUpdate(cmrDeleteAgentPermission);
			permissionDao.saveOrUpdate(cmrStoragePermission);
			permissionDao.saveOrUpdate(cmrAdministrationPermission);
			permissionDao.saveOrUpdate(cmrLookAtAgentsPermission);
			
			//Predefined roles
			Role guestRole = new Role("guestRole", new ArrayList<Permission>(), "The role of a guest-user.");
			Role restrictedRole = new Role("restrictedRole", Arrays.asList(cmrRecordingPermission, cmrStoragePermission, cmrLookAtAgentsPermission), "The role of a restricted-user.");
			Role adminRole = new Role("adminRole", Arrays.asList(cmrRecordingPermission, cmrStoragePermission, cmrDeleteAgentPermission, cmrShutdownAndRestartPermission, cmrAdministrationPermission, cmrLookAtAgentsPermission), "The role of an admin-user.");
			
			//Transfers roles to database.
			roleDao.saveOrUpdate(guestRole);
			roleDao.saveOrUpdate(restrictedRole);
			roleDao.saveOrUpdate(adminRole);
				
			//Standarduser - has to be changed on first login
			User admin = new User(Permutation.hashString("admin"), "admin", adminRole.getId(), false);
			
			//Guestuser - can be edited to give a user without an account rights
			User guest = new User(Permutation.hashString("guest"), "guest", guestRole.getId(), false);
						
			//Transfers users to databse.		
			userDao.saveOrUpdate(guest);
			userDao.saveOrUpdate(admin);			
		}		
	}
}
