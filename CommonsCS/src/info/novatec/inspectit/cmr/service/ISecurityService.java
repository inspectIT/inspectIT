package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.cmr.User;

/**
 * Provides general security operations for client<->cmr interaction.
 * 
 * @author Andreas Herzog
 * @author Clemens Geibel
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ISecurityService {	
	/**
	 * Searches for a User in the CMR DB.
	 * @param email Email.
	 * @param hashedPW hashed password
	 * @return User by Email.
	 */
	User authenticate(String hashedPW, String email);
}
