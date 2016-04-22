package rocks.inspectit.ui.rcp.ci;

import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.ci.listener.IAgentMappingsChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IEnvironmentChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IProfileChangeListener;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.util.ListenerList;

/**
 * Manager for the CI related UI actions. Listens and delegates the CI events.
 *
 * @author Ivan Senic
 *
 */
public class InspectITConfigurationInterfaceManager implements IProfileChangeListener, IEnvironmentChangeListener, IAgentMappingsChangeListener {

	/**
	 * List of {@link IProfileChangeListener}s.
	 */
	private ListenerList<IProfileChangeListener> profileChangeListeners = new ListenerList<>();

	/**
	 * List of {@link IEnvironmentChangeListener}s.
	 */
	private ListenerList<IEnvironmentChangeListener> environmentChangeListeners = new ListenerList<>();

	/**
	 * List of {@link IAgentMappingsChangeListener}s.
	 */
	private ListenerList<IAgentMappingsChangeListener> agentMappingChangeListeners = new ListenerList<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileCreated(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		for (IProfileChangeListener listener : profileChangeListeners) {
			listener.profileCreated(profile, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileUpdated(Profile profile, CmrRepositoryDefinition repositoryDefinition, boolean onlyProperties) {
		for (IProfileChangeListener listener : profileChangeListeners) {
			listener.profileUpdated(profile, repositoryDefinition, onlyProperties);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileDeleted(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		for (IProfileChangeListener listener : profileChangeListeners) {
			listener.profileDeleted(profile, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentCreated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		for (IEnvironmentChangeListener listener : environmentChangeListeners) {
			listener.environmentCreated(environment, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentUpdated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		for (IEnvironmentChangeListener listener : environmentChangeListeners) {
			listener.environmentUpdated(environment, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentDeleted(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		for (IEnvironmentChangeListener listener : environmentChangeListeners) {
			listener.environmentDeleted(environment, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void agentMappingsUpdated(AgentMappings agentMappings, CmrRepositoryDefinition repositoryDefinition) {
		for (IAgentMappingsChangeListener listener : agentMappingChangeListeners) {
			listener.agentMappingsUpdated(agentMappings, repositoryDefinition);
		}
	}

	/**
	 * Registers a {@link IProfileChangeListener} if the same listener does not already exist.
	 *
	 * @param profileChangeListener
	 *            {@link IProfileChangeListener} to add.
	 */
	public void addProfileChangeListener(IProfileChangeListener profileChangeListener) {
		profileChangeListeners.add(profileChangeListener);
	}

	/**
	 * Removes a {@link IProfileChangeListener}.
	 *
	 * @param profileChangeListener
	 *            {@link IProfileChangeListener} to remove.
	 */
	public void removeProfileChangeListener(IProfileChangeListener profileChangeListener) {
		profileChangeListeners.remove(profileChangeListener);
	}

	/**
	 * Registers a {@link IEnvironmentChangeListener} if the same listener does not already exist.
	 *
	 * @param environmentChangeListener
	 *            {@link IEnvironmentChangeListener} to add.
	 */
	public void addEnvironmentChangeListener(IEnvironmentChangeListener environmentChangeListener) {
		environmentChangeListeners.add(environmentChangeListener);
	}

	/**
	 * Removes a {@link IEnvironmentChangeListener}.
	 *
	 * @param environmentChangeListener
	 *            {@link IEnvironmentChangeListener} to remove.
	 */
	public void removeEnvironmentChangeListener(IEnvironmentChangeListener environmentChangeListener) {
		environmentChangeListeners.remove(environmentChangeListener);
	}

	/**
	 * Registers a {@link IAgentMappingsChangeListener} if the same listener does not already exist.
	 *
	 * @param agentMappingsChangeListener
	 *            {@link IAgentMappingsChangeListener} to add.
	 */
	public void addAgentMappingsChangeListener(IAgentMappingsChangeListener agentMappingsChangeListener) {
		agentMappingChangeListeners.add(agentMappingsChangeListener);
	}

	/**
	 * Removes a {@link IAgentMappingsChangeListener}.
	 *
	 * @param agentMappingsChangeListener
	 *            {@link IAgentMappingsChangeListener} to remove.
	 */
	public void removeAgentMappingsChangeListener(IAgentMappingsChangeListener agentMappingsChangeListener) {
		agentMappingChangeListeners.remove(agentMappingsChangeListener);
	}

}
