package rocks.inspectit.ui.rcp.ci.listener.impl;

import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.ci.dialog.InstrumentationUpdateDialog;
import rocks.inspectit.ui.rcp.ci.handler.InstrumentationUpdateHandler;
import rocks.inspectit.ui.rcp.ci.listener.IAgentMappingsChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IEnvironmentChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IProfileChangeListener;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Listener to open the {@link InstrumentationUpdateDialog} when the instrumentation changed due to
 * agent mapping, environment or profile modifications.
 *
 * @author Marius Oehler
 *
 */
public class InstrumentationUpdateListener implements IProfileChangeListener, IEnvironmentChangeListener, IAgentMappingsChangeListener {

	/**
	 * The text of the closing button.
	 */
	private static final String CLOSE_BUTTON_LABEL = "Not Now";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void agentMappingsUpdated(AgentMappings agentMappings, CmrRepositoryDefinition repositoryDefinition) {
		// show instrumentation update dialog if necessary
		InstrumentationUpdateHandler.execute(repositoryDefinition, CLOSE_BUTTON_LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentCreated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		// not needed
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentUpdated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		// show instrumentation update dialog if necessary
		InstrumentationUpdateHandler.execute(repositoryDefinition, CLOSE_BUTTON_LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentDeleted(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		// show instrumentation update dialog if necessary
		InstrumentationUpdateHandler.execute(repositoryDefinition, CLOSE_BUTTON_LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileCreated(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		// not needed
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileUpdated(Profile profile, CmrRepositoryDefinition repositoryDefinition, boolean onlyProperties) {
		// show instrumentation update dialog if necessary
		InstrumentationUpdateHandler.execute(repositoryDefinition, CLOSE_BUTTON_LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileDeleted(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		// show instrumentation update dialog if necessary
		InstrumentationUpdateHandler.execute(repositoryDefinition, CLOSE_BUTTON_LABEL);
	}

}
