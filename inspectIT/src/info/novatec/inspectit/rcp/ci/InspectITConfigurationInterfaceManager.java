package info.novatec.inspectit.rcp.ci;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.rcp.ci.listener.IEnvironmentChangeListener;
import info.novatec.inspectit.rcp.ci.listener.IProfileChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Senic
 * 
 */
public class InspectITConfigurationInterfaceManager implements IProfileChangeListener, IEnvironmentChangeListener {

	/**
	 * List of {@link IProfileChangeListener}s.
	 */
	private List<IProfileChangeListener> profileChangeListeners = new ArrayList<>();

	/**
	 * List of {@link IEnvironmentChangeListener}s.
	 */
	private List<IEnvironmentChangeListener> environmentChangeListeners = new ArrayList<>();

	/**
	 * {@inheritDoc}
	 */
	public void profileAdded(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		synchronized (profileChangeListeners) {
			for (IProfileChangeListener listener : profileChangeListeners) {
				listener.profileAdded(profile, repositoryDefinition);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void profileEdited(Profile profile, boolean onlyProperties) {
		synchronized (profileChangeListeners) {
			for (IProfileChangeListener listener : profileChangeListeners) {
				listener.profileEdited(profile, onlyProperties);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void profileDeleted(Profile profile) {
		synchronized (profileChangeListeners) {
			for (IProfileChangeListener listener : profileChangeListeners) {
				listener.profileDeleted(profile);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void environmentAdded(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		synchronized (environmentChangeListeners) {
			for (IEnvironmentChangeListener listener : environmentChangeListeners) {
				listener.environmentAdded(environment, repositoryDefinition);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void environmentEdited(Environment environment) {
		synchronized (environmentChangeListeners) {
			for (IEnvironmentChangeListener listener : environmentChangeListeners) {
				listener.environmentEdited(environment);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void environmentDeleted(Environment environment) {
		synchronized (environmentChangeListeners) {
			for (IEnvironmentChangeListener listener : environmentChangeListeners) {
				listener.environmentDeleted(environment);
			}
		}
	}

	/**
	 * Registers a {@link IProfileChangeListener} if the same listener does not already exist.
	 * 
	 * @param profileChangeListener
	 *            {@link IProfileChangeListener} to add.
	 */
	public void addProfileChangeListener(IProfileChangeListener profileChangeListener) {
		synchronized (profileChangeListeners) {
			if (!profileChangeListeners.contains(profileChangeListener)) {
				profileChangeListeners.add(profileChangeListener);
			}
		}
	}

	/**
	 * Removes a {@link IProfileChangeListener}.
	 * 
	 * @param profileChangeListener
	 *            {@link IProfileChangeListener} to remove.
	 */
	public void removeProfileChangeListener(IProfileChangeListener profileChangeListener) {
		synchronized (profileChangeListeners) {
			profileChangeListeners.remove(profileChangeListener);
		}
	}

	/**
	 * Registers a {@link IEnvironmentChangeListener} if the same listener does not already exist.
	 * 
	 * @param environmentChangeListener
	 *            {@link IEnvironmentChangeListener} to add.
	 */
	public void addEnvironmentChangeListener(IEnvironmentChangeListener environmentChangeListener) {
		synchronized (environmentChangeListeners) {
			if (!environmentChangeListeners.contains(environmentChangeListener)) {
				environmentChangeListeners.add(environmentChangeListener);
			}
		}
	}

	/**
	 * Removes a {@link IEnvironmentChangeListener}.
	 * 
	 * @param environmentChangeListener
	 *            {@link IEnvironmentChangeListener} to remove.
	 */
	public void removeEnvironmentChangeListener(IEnvironmentChangeListener environmentChangeListener) {
		synchronized (environmentChangeListeners) {
			environmentChangeListeners.remove(environmentChangeListener);
		}
	}

}
