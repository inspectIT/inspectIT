package info.novatec.inspectit.cmr.service;


import java.util.List;

import javax.annotation.PostConstruct;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.communication.data.cmr.Permutation;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.spring.logger.Log;

import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Provides general security operations for client<->cmr interaction.
 * 
 * @author Andreas Herzog
 * @author Clemens Geibel
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
	 * Is executed after dependency injection is done to perform any initialization.
	 */
	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-Security Service active...");
		}
	}


	@Override
	public User authenticate(String pw, String email) {
		List<User> foundUsers = userDao.findByEmail(email);
		if (foundUsers.isEmpty()) {
			throw new AuthenticationException("E-Mail or Password is incorrect.");
			
		} else if (foundUsers.size() != 1) {
			throw new AuthenticationException("There are multiple Users with same E-Mail.");
			
		} else if (!foundUsers.get(0).getPassword().equals(Permutation.hashString(pw))) {
			throw new AuthenticationException("E-Mail or Password is incorrect.");
			
		} else {
			User result = foundUsers.get(0);
			return new User(result.getName(), null, result.getEmail(), result.getRoleId());
		}
	}
	
}
