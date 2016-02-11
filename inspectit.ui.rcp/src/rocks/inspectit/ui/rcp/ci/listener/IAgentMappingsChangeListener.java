package info.novatec.inspectit.rcp.ci.listener;

import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.EventListener;

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