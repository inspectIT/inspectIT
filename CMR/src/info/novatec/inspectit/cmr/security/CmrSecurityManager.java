package info.novatec.inspectit.cmr.security;

import javax.annotation.PostConstruct;

import info.novatec.inspectit.spring.logger.Log;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Security Manager in the means of Apache Shiro.
 * 
 * @author Andreas Herzog
 * @author Lucca Hellriegel
 */
public class CmrSecurityManager extends DefaultSecurityManager {
	
	/**
	 * Logger of this Class.
	 */
	@Log
	Logger log;
	
	/**
	 * Communication instance towards the database.
	 */
	@Autowired
	private CmrRealm cmrRealm;
	
	/**
	 * Constructor for the Security Manager.
	 * @param realm Realm object for configuration of the manager.
	 */
	@Autowired	
	public CmrSecurityManager(CmrRealm realm) {
		super(realm);	
	}

	@Override
	public AuthenticationInfo authenticate(AuthenticationToken token) throws AuthenticationException {
		return cmrRealm.doGetAuthenticationInfo(token);
	}
	
	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 */
	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-CmrSecurityManager active...");
		}
	}
}
