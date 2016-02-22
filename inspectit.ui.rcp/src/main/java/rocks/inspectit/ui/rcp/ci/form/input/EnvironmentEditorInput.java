package rocks.inspectit.ui.rcp.ci.form.input;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.provider.IEnvironmentProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Input for environment editor.
 * 
 * @author Ivan Senic
 * 
 */
public class EnvironmentEditorInput implements IEditorInput, IEnvironmentProvider {

	/**
	 * {@link Environment}.
	 */
	private final Environment environment;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private final CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Profiles environment can be linked with.
	 */
	private final Collection<Profile> profiles;

	/**
	 * @param environment
	 *            {@link Environment}.
	 * @param profiles
	 *            Profiles environment can be linked with.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public EnvironmentEditorInput(Environment environment, Collection<Profile> profiles, CmrRepositoryDefinition cmrRepositoryDefinition) {
		Assert.isNotNull(environment);
		Assert.isNotNull(profiles);
		Assert.isNotNull(cmrRepositoryDefinition);

		this.environment = environment;
		this.profiles = profiles;
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
		return environment.getName();
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
		if (Environment.class.equals(adapter)) {
			return environment;
		} else if (CmrRepositoryDefinition.class.equals(adapter)) {
			return cmrRepositoryDefinition;
		}
		return null;
	}

	/**
	 * Gets {@link #environment}.
	 * 
	 * @return {@link #environment}
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Gets {@link #profiles}.
	 * 
	 * @return {@link #profiles}
	 */
	public Collection<Profile> getProfiles() {
		return profiles;
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
		// hash code only for CMR and environments, as we want editor per environment and CMR
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmrRepositoryDefinition == null) ? 0 : cmrRepositoryDefinition.hashCode());
		result = prime * result + ((environment == null) ? 0 : environment.hashCode());
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
		EnvironmentEditorInput other = (EnvironmentEditorInput) obj;
		if (cmrRepositoryDefinition == null) {
			if (other.cmrRepositoryDefinition != null) {
				return false;
			}
		} else if (!cmrRepositoryDefinition.equals(other.cmrRepositoryDefinition)) {
			return false;
		}
		if (environment == null) {
			if (other.environment != null) {
				return false;
			}
		} else if (!environment.equals(other.environment)) {
			return false;
		}
		return true;
	}

}
