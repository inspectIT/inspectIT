package info.novatec.inspectit.cmr.playground;

import static org.hamcrest.Matchers.instanceOf;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.novatec.inspectit.cmr.CMR;
import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.cmr.usermanagement.Permission;
import info.novatec.inspectit.cmr.usermanagement.Role;
import info.novatec.inspectit.cmr.usermanagement.User;

/**
 * 
 * @author Joshua Hartmann
 *
 */
public class Playground {
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
	 * The logger of this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Playground.class);

	/**
	 * Playground.
	 */
	public void play() {
		Permission permReadOption = new Permission(1, "ReadOptionOption", "Permission to read options");
		Permission permAlterOption = new Permission(2, "AlterOption", "Permission to alter options");
		Permission permCreatePermission = new Permission(3, "CreatePermission", "Permission to create a new permission");
		Permission permDeltePermission = new Permission(4, "DeltePermission", "Permission to delte a permission");
		Permission permShutdownCMR = new Permission(5, "ShutdownCMR", "Permission to shut down the CMR");
		

        permissionDao.saveOrUpdate(permReadOption);
        permissionDao.saveOrUpdate(permAlterOption);
        permissionDao.saveOrUpdate(permCreatePermission);
        permissionDao.saveOrUpdate(permDeltePermission);
        permissionDao.saveOrUpdate(permShutdownCMR);
		
		
        
		Role powerUser = new Role(1, "PowerUser", Arrays.asList(permReadOption, permAlterOption, permCreatePermission, permDeltePermission, permShutdownCMR));
        Role restrictedUser = new Role(2, "RestrictedUser", Arrays.asList(permReadOption));

        roleDao.saveOrUpdate(powerUser);
        roleDao.saveOrUpdate(restrictedUser);
        
        
        
        
        User jakePowerUser = new User("Jake", "JakesSuperSecretPassword!", "jake@mail.com", powerUser.getId());
        User tomRestrcitedUser = new User("Tom", "TomsNotsoSecurePassword", "tom@mail.com", restrictedUser.getId());
        userDao.saveOrUpdate(jakePowerUser);
        userDao.saveOrUpdate(tomRestrcitedUser);
        
        
        User jakeLoaded = userDao.load("Jake");
        
        LOGGER.info(jakeLoaded.toString());
        
        Role powerUserLoaded = roleDao.load("PowerUser");

        LOGGER.info(powerUserLoaded.toString());
	}
	
	
}
