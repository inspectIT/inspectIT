package info.novatec.inspectit.cmr.security;

//import info.novatec.inspectit.spring.logger.Log;

import javax.annotation.PostConstruct;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.cmr.service.ISecurityService;
import info.novatec.inspectit.spring.logger.Log;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
//import org.slf4j.Logger;
import org.slf4j.Logger;

/**
 * doc.
 * @author Andreas Herzog
 *
 */
public class CmrSecurityManager extends DefaultSecurityManager {
	
	/**
	 * Logger of this Class.
	 */
	@Log
	Logger log;
	
	/**
	 * doc.
	 */
	private CmrRealm cmrRealm;
	
	private UserDao userDao;
	
	public CmrSecurityManager() {
	}
	/**
	 * doc.
	 */
//	public CmrSecurityManager(UserDao userDao) {
//		this.userDao = userDao;
//		this.cmrRealm = new CmrRealm(userDao);
//		//SecurityUtils.setSecurityManager(this);
//	}
	
	@Override
	public AuthenticationInfo authenticate(AuthenticationToken token) throws AuthenticationException {
		return cmrRealm.doGetAuthenticationInfo(token);
	}
	
	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 */
	@PostConstruct
	public void postConstruct() {
		this.cmrRealm = new CmrRealm();
		if (log.isInfoEnabled()) {
			log.info("|-CmrSecurityManager active...");
		}
	}
	
	public UserDao getUserDao() {
		return userDao;
	}
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public CmrRealm getCmrRealm() {
		return cmrRealm;
	}
	public void setCmrRealm(CmrRealm cmrRealm) {
		this.cmrRealm = cmrRealm;
	}
}
