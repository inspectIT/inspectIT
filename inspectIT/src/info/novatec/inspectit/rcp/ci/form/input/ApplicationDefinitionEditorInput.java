package info.novatec.inspectit.rcp.ci.form.input;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.rcp.ci.form.editor.ApplicationDefinitionEditor;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input for {@link ApplicationDefinitionEditor}.
 *
 * @author Alexander Wert
 *
 */
public class ApplicationDefinitionEditorInput implements IEditorInput {

	/**
	 * {@link ApplicationDefinition} to be edited.
	 */
	private final ApplicationDefinition application;

	/**
	 * {@link CmrRepositoryDefinition} to use when saving changes.
	 */
	private final CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 *
	 * @param application
	 *            {@link ApplicationDefinition} to be edited.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to use when saving changes.
	 */
	public ApplicationDefinitionEditorInput(ApplicationDefinition application, CmrRepositoryDefinition cmrRepositoryDefinition) {
		Assert.isNotNull(application);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.application = application;

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (ApplicationDefinition.class.equals(adapter)) {
			return getApplication();
		} else if (CmrRepositoryDefinition.class.equals(adapter)) {
			return cmrRepositoryDefinition;
		}
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	@Override
	public String getName() {
		return application.getApplicationName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "";
	}

	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * Gets {@link #application}.
	 *
	 * @return {@link #application}
	 */
	public ApplicationDefinition getApplication() {
		return application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((cmrRepositoryDefinition == null) ? 0 : cmrRepositoryDefinition.hashCode());
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
		ApplicationDefinitionEditorInput other = (ApplicationDefinitionEditorInput) obj;
		if (application == null) {
			if (other.application != null) {
				return false;
			}
		} else if (!application.equals(other.application)) {
			return false;
		}
		if (cmrRepositoryDefinition == null) {
			if (other.cmrRepositoryDefinition != null) {
				return false;
			}
		} else if (!cmrRepositoryDefinition.equals(other.cmrRepositoryDefinition)) {
			return false;
		}
		return true;
	}

}
