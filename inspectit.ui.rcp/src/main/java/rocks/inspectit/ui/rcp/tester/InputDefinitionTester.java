package rocks.inspectit.ui.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.provider.IInputDefinitionProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.StorageRepositoryDefinition;

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
