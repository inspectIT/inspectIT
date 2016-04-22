package rocks.inspectit.ui.rcp.ci.listener;

import java.util.EventListener;

import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Interface for the listeners on the {@link AgentMappings} changes.
 *
 * @author Ivan Senic
 *
 */
public interface IAgentMappingsChangeListener extends EventListener {

	/**
	 * {@link AgentMappings} is edited.
	 *
	 * @param agentMappings
	 *            {@link AgentMappings}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} on which action occurred
	 */
	void agentMappingsUpdated(AgentMappings agentMappings, CmrRepositoryDefinition repositoryDefinition);

}