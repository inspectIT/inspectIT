package rocks.inspectit.ui.rcp.ci;

import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.ci.listener.IAgentMappingsChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IAlertDefinitionChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IApplicationDefinitionChangeListener;
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
public class InspectITConfigurationInterfaceManager
		implements IProfileChangeListener, IEnvironmentChangeListener, IAgentMappingsChangeListener, IApplicationDefinitionChangeListener, IAlertDefinitionChangeListener {

	/**
	 * List of {@link IProfileChangeListener}s.
	 */
	private final ListenerList<IProfileChangeListener> profileChangeListeners = new ListenerList<>();

	/**
	 * List of {@link IEnvironmentChangeListener}s.
	 */
	private final ListenerList<IEnvironmentChangeListener> environmentChangeListeners = new ListenerList<>();

	/**
	 * List of {@link IAgentMappingsChangeListener}s.
	 */
	private final ListenerList<IAgentMappingsChangeListener> agentMappingChangeListeners = new ListenerList<>();

	/**
	 * List of {@link IApplicationDefinitionChangeListener}s.
	 */
	private final ListenerList<IApplicationDefinitionChangeListener> applicationChangeListeners = new ListenerList<>();

	/**
	 * List of {@link IApplicationDefinitionChangeListener}s.
	 */
	private final ListenerList<IAlertDefinitionChangeListener> alertDefinitionChangeListeners = new ListenerList<>();

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
	 * {@inheritDoc}
	 */
	@Override
	public void applicationCreated(ApplicationDefinition application, int positionIndex, CmrRepositoryDefinition repositoryDefinition) {
		for (IApplicationDefinitionChangeListener listener : applicationChangeListeners) {
			listener.applicationCreated(application, positionIndex, repositoryDefinition);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationMoved(ApplicationDefinition application, int oldPositionIndex, int newPositionIndex, CmrRepositoryDefinition repositoryDefinition) {
		for (IApplicationDefinitionChangeListener listener : applicationChangeListeners) {
			listener.applicationMoved(application, oldPositionIndex, newPositionIndex, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationUpdated(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
		for (IApplicationDefinitionChangeListener listener : applicationChangeListeners) {
			listener.applicationUpdated(application, repositoryDefinition);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationDeleted(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
		for (IApplicationDefinitionChangeListener listener : applicationChangeListeners) {
			listener.applicationDeleted(application, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void alertDefinitionCreated(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition) {
		for (IAlertDefinitionChangeListener listener : alertDefinitionChangeListeners) {
			listener.alertDefinitionCreated(alertDefinition, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void alertDefinitionUpdated(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition) {
		for (IAlertDefinitionChangeListener listener : alertDefinitionChangeListeners) {
			listener.alertDefinitionUpdated(alertDefinition, repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void alertDefinitionDeleted(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition) {
		for (IAlertDefinitionChangeListener listener : alertDefinitionChangeListeners) {
			listener.alertDefinitionDeleted(alertDefinition, repositoryDefinition);
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

	/**
	 * Registers a {@link IApplicationDefinitionChangeListener} if the same listener does not
	 * already exist.
	 *
	 * @param applicationDefinitionChangeListener
	 *            {@link IApplicationDefinitionChangeListener} to add.
	 */
	public void addApplicationDefinitionChangeListener(IApplicationDefinitionChangeListener applicationDefinitionChangeListener) {
		applicationChangeListeners.add(applicationDefinitionChangeListener);
	}

	/**
	 * Removes a {@link IApplicationDefinitionChangeListener}.
	 *
	 * @param applicationDefinitionChangeListener
	 *            {@link IApplicationDefinitionChangeListener} to remove.
	 */
	public void removeApplicationDefinitionChangeListener(IApplicationDefinitionChangeListener applicationDefinitionChangeListener) {
		applicationChangeListeners.remove(applicationDefinitionChangeListener);
	}

	/**
	 * Registers a {@link IAlertDefinitionChangeListener} if the same listener does not already
	 * exist.
	 *
	 * @param alertDefinitionChangeListener
	 *            {@link IAlertDefinitionChangeListener} to add.
	 */
	public void addAlertDefinitionChangeListener(IAlertDefinitionChangeListener alertDefinitionChangeListener) {
		alertDefinitionChangeListeners.add(alertDefinitionChangeListener);
	}

	/**
	 * Removes a {@link IAlertDefinitionChangeListener}.
	 *
	 * @param alertDefinitionChangeListener
	 *            {@link IAlertDefinitionChangeListener} to remove.
	 */
	public void removeAlertDefinitionChangeListener(IAlertDefinitionChangeListener alertDefinitionChangeListener) {
		alertDefinitionChangeListeners.remove(alertDefinitionChangeListener);
	}

}
