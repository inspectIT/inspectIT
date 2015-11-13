package info.novatec.inspectit.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tester for Permissions.
 * 
 * @author Lucca Hellriegel
 * @author Thomas Sachs
 *
 */
public class PermissionTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		
		//TODO: Connect with Shiro functionality
		//TODO: Check permissions generically from CmrDefitinition
		if ("cmrShutdownAndRestartPermission".equals(property)) {
			return true;
			}
		
		if ("cmrDeleteAgentPermission".equals(property)) {
			return true;
			}
		
		
		if ("cmrRecordingPermission".equals(property)) {
			return true;
			}
		
		if ("cmrStoragePermission".equals(property)) {
			return false;
			}
		
		return false;
	}

}
