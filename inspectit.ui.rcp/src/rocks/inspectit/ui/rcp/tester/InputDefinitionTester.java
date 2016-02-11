package info.novatec.inspectit.rcp.tester;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.provider.IInputDefinitionProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tests the input definition for different properties.
 * 
 * @author Ivan Senic
 * 
 */
public class InputDefinitionTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IInputDefinitionProvider) {
			InputDefinition inputDefinition = ((IInputDefinitionProvider) receiver).getInputDefinition();
			if ("repositoryType".equals(property)) {
				if ("cmrRepositoryDefinition".equals(expectedValue)) {
					return inputDefinition.getRepositoryDefinition() instanceof CmrRepositoryDefinition;
				} else if ("storageRepositoryDefinition".equals(expectedValue)) {
					return inputDefinition.getRepositoryDefinition() instanceof StorageRepositoryDefinition;
				}
			}
		}
		return false;
	}

}
