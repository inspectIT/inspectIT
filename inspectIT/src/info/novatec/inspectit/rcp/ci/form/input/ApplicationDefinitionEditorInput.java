package info.novatec.inspectit.rcp.ci.form.input;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.rcp.ci.form.editor.ApplicationDefinitionEditor;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.esotericsoftware.kryo.Kryo;

/**
 * Editor input for {@link ApplicationDefinitionEditor}.
 * 
 * @author Alexander Wert
 *
 */
public class ApplicationDefinitionEditorInput implements IEditorInput {

	/**
	 * {@link IApplicationDefinition} to be edited.
	 */
	private final IApplicationDefinition application;

	/**
	 * {@link CmrRepositoryDefinition} to use when saving changes.
	 */
	private final CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 * 
	 * @param application
	 *            {@link IApplicationDefinition} to be edited.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to use when saving changes.
	 */
	public ApplicationDefinitionEditorInput(IApplicationDefinition application, CmrRepositoryDefinition cmrRepositoryDefinition) {
		Assert.isNotNull(application);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		// creates a deep copy of the application to ensure that modifications are not propagated as
		// long as no save action is performed
		this.application = new Kryo().copy(application);

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IApplicationDefinition.class.equals(adapter)) {
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
	public IApplicationDefinition getApplication() {
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
