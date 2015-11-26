package info.novatec.inspectit.rcp.tester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;

import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.provider.IInputDefinitionProvider;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

/**
 * Tester for Permissions. Searchs the grantedPermissions list and gives an corresponding boolean.
 * 
 * @author Lucca Hellriegel
 * @author Thomas Sachs
 * @author Mario Rose
 *
 */
public class PermissionTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		if (receiver instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof ICmrRepositoryAndAgentProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryAndAgentProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof IStorageDataProvider) {
			cmrRepositoryDefinition = ((IStorageDataProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof IInputDefinitionProvider) {
			RepositoryDefinition repository = ((IInputDefinitionProvider) receiver).getInputDefinition()
					.getRepositoryDefinition();
			if (repository instanceof CmrRepositoryDefinition) {
				cmrRepositoryDefinition = (CmrRepositoryDefinition) repository;
			} else {
				return false;
			}
		} else {
			return false;
		}
		
		List<String> grantedPermissions = cmrRepositoryDefinition.getGrantedPermissions();
		
		try {
		return grantedPermissions.contains(property);
		} catch (NullPointerException lo) { 
			return false; 
		}
	}

}
