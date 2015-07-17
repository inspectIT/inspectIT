package info.novatec.inspectit.rcp.editor.root;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * This editor input is used for all views and can only be set in the composite view controller as
 * this is the only one which can be set with an input.
 * 
 * @author Patrice Bouillet
 * 
 */
public class RootEditorInput implements IEditorInput {

	/**
	 * The input definition which holds everything a view needs to create the content.
	 */
	private InputDefinition inputDefinition;

	/**
	 * Only constructor which needs an input definition.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	public RootEditorInput(InputDefinition inputDefinition) {
		Assert.isNotNull(inputDefinition);

		this.inputDefinition = inputDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return inputDefinition.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getToolTipText() {
		return inputDefinition.getEditorPropertiesData().getPartTooltip();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (InputDefinition.class == adapter) {
			return this.inputDefinition;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputDefinition == null) ? 0 : inputDefinition.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RootEditorInput other = (RootEditorInput) obj;
		if (inputDefinition == null) {
			if (other.inputDefinition != null) {
				return false;
			}
		} else if (!inputDefinition.equals(other.inputDefinition)) {
			return false;
		}
		return true;
	}

}
