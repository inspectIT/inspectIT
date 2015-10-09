package info.novatec.inspectit.cmr.service;


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
	 * If this works, we rock.
	 */
	private String message = "You rock!";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		return message;
	}

}
