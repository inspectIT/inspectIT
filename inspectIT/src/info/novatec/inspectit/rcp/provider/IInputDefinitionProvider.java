package info.novatec.inspectit.rcp.provider;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;

/**
 * Marker for all classes that can provide an {@link InputDefinition}.
 * 
 * @author Ivan Senic
 * 
 */
public interface IInputDefinitionProvider {

	/**
	 * @return Returns input definition.
	 */
	InputDefinition getInputDefinition();
}
