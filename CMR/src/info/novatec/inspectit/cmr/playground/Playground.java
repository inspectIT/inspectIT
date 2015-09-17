package info.novatec.inspectit.cmr.playground;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.cmr.usermanagement.Permission;

/**
 * 
 * @author Joshua Hartmann
 *
 */
public class Playground {
	/**
	 * PermissionDao.
	 */
	
	PermissionDao permissionDao;
	
	public PermissionDao getPermissionDao() {
		return permissionDao;
	}
	@Autowired
	public void setPermissionDao(PermissionDao permissionDao) {
		this.permissionDao = permissionDao;
	}

	/**
	 * Playground.
	 */
	public void play() {
		Permission permission1 = new Permission("asdf-User", "heyho");

        permissionDao.saveOrUpdate(permission1);
	}
	
	
}
