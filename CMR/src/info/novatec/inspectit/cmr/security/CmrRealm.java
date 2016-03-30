package info.novatec.inspectit.cmr.security;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.spring.logger.Log;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Performs all database lookups concerning the security management.
 * 
 * @author Andreas Herzog
 */
public class CmrRealm extends AuthorizingRealm {
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
	RoleDao roleDao;

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		String email = upToken.getUsername();
		String pw = new String(upToken.getPassword());

		User foundUser = userDao.findByEmail(email);
		if (foundUser == null) {
			throw new AuthenticationException("An Error occurred while logging into the cmr.");
		} else if (!foundUser.getPassword().equals(pw)) {

			throw new AuthenticationException("An Error occurred while logging into the cmr.");
		}

		SimpleAuthenticationInfo authInfo = new SimpleAuthenticationInfo();
		SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(upToken.getPrincipal(),
				getName());

		authInfo.setPrincipals(principalCollection);
		authInfo.setCredentials(upToken.getCredentials());

		return authInfo;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		String email = (String) getAvailablePrincipal(principals);
		Role role;

		User foundUser = userDao.findByEmail(email);
		if (foundUser == null) {
			throw new AuthenticationException("Email or password is incorrect.");
		}

		role = getRoleByID(foundUser.getRoleId());

		for (Permission perm : role.getPermissions()) {
			authorizationInfo.addStringPermission(perm.getTitle());
		}
		authorizationInfo.addRole(role.getTitle());

		return authorizationInfo;
	}

	/**
	 * Searches for the Role matching a given ID.
	 * 
	 * @param id
	 *            RoleID.
	 * @return Role with this ID.
	 * @throws DataRetrievalFailureException
	 *             if no role with given id exists.
	 */
	public Role getRoleByID(long id) throws DataRetrievalFailureException {
		Role role = roleDao.findByID(id);

		if (role == null) {
			throw new DataRetrievalFailureException("No roles in the database matching the given id!");
		} else {
			return role;
		}
	}
}
