package info.novatec.inspectit.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * 
 * Tester for CMR Login Status. Checks whether user is logged in.
 * 
 * @author Clemens Geibel
 *
 */

public class CmrLoginStatusTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		if (receiver instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof ICmrRepositoryAndAgentProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryAndAgentProvider) receiver).getCmrRepositoryDefinition();
		} else {
			return false;
		}

		if ("cmrLoginStatus".equals(property)) {
			if ("LOGGEDIN".equals(expectedValue)) {
				return null != cmrRepositoryDefinition.getGrantedPermissions();
			} else if ("LOGGEDOUT".equals(expectedValue)) {
				return null == cmrRepositoryDefinition.getGrantedPermissions();
			}
		}

		return false;
	}

}
