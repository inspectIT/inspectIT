package info.novatec.inspectit.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.provider.IInputDefinitionProvider;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;



/**
 * Tester for Permissions.
 * 
 * @author Lucca Hellriegel
 *
 */
public class PermissionTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
				
		String cmrDeleteAgentPermission = "CmrDeleteAgentPermission";
		String cmrShutdownAndRestartPermission = "CmrShutdownAndRestartPermission";
		String cmrRecordingPermission = "CmrRecordingPermission";
		String cmrStoragePermission = "CmrStoragePermission";
		
		
		if (cmrShutdownAndRestartPermission.equals(property)) {
			// return currentUser.hasPermission(CmrShutdownAndRestartPermission)
			return true;
			
			}
		
		if (cmrDeleteAgentPermission.equals(property)) {
			// return currentUser.hasPermission(CmrShutdownAndRestartPermission)
			return true;
			
			}
		
		
		//Only in StartRecording, because Stop will only show Up if Start is pushed
		if (cmrRecordingPermission.equals(property)) {
			// return currentUser.hasPermission(CmrShutdownAndRestartPermission)
			return false;
			
			}
		
		if (cmrStoragePermission.equals(property)) {
			// return currentUser.hasPermission(CmrShutdownAndRestartPermission)
			return true;
			
			}
		
		
			
			/**
			 * Generic Test:
			 * 
			 * 1. Get CMRRepo
			 * 
			 * 
			 * 
			 * CmrRepositoryDefinition cmrRepositoryDefinition = null;
		if (receiver instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof ICmrRepositoryAndAgentProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryAndAgentProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof IStorageDataProvider) {
			cmrRepositoryDefinition = ((IStorageDataProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof IInputDefinitionProvider) {
			RepositoryDefinition repository = ((IInputDefinitionProvider) receiver).getInputDefinition().getRepositoryDefinition();
			if (repository instanceof CmrRepositoryDefinition) {
				cmrRepositoryDefinition = (CmrRepositoryDefinition) repository;
			} else {
				return false;
			}
		} else {
			return false;
		}
			 * 
			 * 2.
			 * Subject currentUser = SecurityUtils.getSubject();
			 * 
			 * String[] permissionStrings = String[cmrRepositoryDefinition.getPermissionCounter()];
			 * permissionStrings = cmrRepositoryDefinition.getPermissionStrings();
			 * Permission[] permissions = Permission[cmrRepositoryDefinition.getPermissionCounter()];
			 * permissions = cmrRepositoryDefinition.getPermissions();
			 * 
			 * for(i=0;i<permissionStrings.length();i++){
			 * if(permissionStrings[i]==property) return currentUser.hasPermission(permissions[i]); 
			 * }
			 * return false; 
			 * 
			 * 
			 * 
			 */
		
		
		return false;
	}

}
