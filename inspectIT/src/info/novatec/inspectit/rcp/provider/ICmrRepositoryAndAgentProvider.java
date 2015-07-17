package info.novatec.inspectit.rcp.provider;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * Interface for UI elements that can provide {@link PlatformIdent} alongside CMR repository
 * definition.
 * 
 * @author Ivan Senic
 * 
 */
public interface ICmrRepositoryAndAgentProvider {

	/**
	 * Gives the {@link CmrRepositoryDefinition}.
	 * 
	 * @return Gives the {@link CmrRepositoryDefinition}.
	 */
	CmrRepositoryDefinition getCmrRepositoryDefinition();

	/**
	 * Returns the agent.
	 * 
	 * @return {@link PlatformIdent}.
	 */
	PlatformIdent getPlatformIdent();

}
