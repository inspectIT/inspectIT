package info.novatec.inspectit.rcp.ci.form.input;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.rcp.provider.IProfileProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Input for profile editor.
 * 
 * @author Ivan Senic
 * 
 */
public class ProfileEditorInput implements IEditorInput, IProfileProvider {

	/**
	 * {@link Profile}.
	 */
	private Profile profile;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * @param profile
	 *            {@link Profile}.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public ProfileEditorInput(Profile profile, CmrRepositoryDefinition cmrRepositoryDefinition) {
		Assert.isNotNull(profile);
		Assert.isNotNull(cmrRepositoryDefinition);

		this.profile = profile;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return profile.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToolTipText() {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (Profile.class.equals(adapter)) {
			return profile;
		} else if (CmrRepositoryDefinition.class.equals(adapter)) {
			return cmrRepositoryDefinition;
		}
		return null;
	}

	/**
	 * Gets {@link #profile}.
	 * 
	 * @return {@link #profile}
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * Gets {@link #cmrRepositoryDefinition}.
	 * 
	 * @return {@link #cmrRepositoryDefinition}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmrRepositoryDefinition == null) ? 0 : cmrRepositoryDefinition.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
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
		ProfileEditorInput other = (ProfileEditorInput) obj;
		if (cmrRepositoryDefinition == null) {
			if (other.cmrRepositoryDefinition != null) {
				return false;
			}
		} else if (!cmrRepositoryDefinition.equals(other.cmrRepositoryDefinition)) {
			return false;
		}
		if (profile == null) {
			if (other.profile != null) {
				return false;
			}
		} else if (!profile.equals(other.profile)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(IProfileProvider o) {
		return 0;
	}

}
