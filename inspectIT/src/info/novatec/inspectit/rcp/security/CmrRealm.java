package info.novatec.inspectit.rcp.security;

import info.novatec.inspectit.cmr.security.Permission;
import info.novatec.inspectit.cmr.security.Role;
import info.novatec.inspectit.cmr.service.ISecurityService;
import info.novatec.inspectit.exception.RemoteException;

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

/**
 * 
 * @author SetYourOwnName
 *
 */
public class CmrRealm extends AuthorizingRealm {
	/**
	 * The security service interface of this class.
	 */
	private ISecurityService securityService;
	
	/**
	 * Default constructor.
	 * @param securityService securityService
	 */
	public CmrRealm(ISecurityService securityService) {
		this.securityService = securityService;
	}
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		String email = upToken.getUsername();
		String pw = new String(upToken.getPassword());
		//check if the submitted userdata is valid, if so proceed, if not abort here
		try {
			securityService.authenticate(pw, email);
		} catch (RemoteException re) {
			throw new AuthenticationException("An Error occurred while logging into the cmr.");
		}

		SimpleAuthenticationInfo authInfo = new SimpleAuthenticationInfo();
		SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(upToken.getPrincipal(), getName());

		authInfo.setPrincipals(principalCollection);
		authInfo.setCredentials(upToken.getCredentials());

		return authInfo;
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

		//get the principal this realm cares about:
		String email = (String) getAvailablePrincipal(principals);

		Role role = securityService.retrieveRole(email);
		
		for (Permission perm : role.getPermissions()) {
			authorizationInfo.addStringPermission(perm.getTitle());
		}
		authorizationInfo.addRole(role.getTitle());
		
		//SecurityManager securityManager = new DefaultSecurityManager(this);

		//add some static permission for demonstration purpose

		//authorizationInfo.addStringPermission("deleteStorage");
		//authorizationInfo.addStringPermission("restartAndShutdown");
		
		//call the underlying EIS for the account data:
		return authorizationInfo;
	}
}
