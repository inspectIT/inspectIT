package rocks.inspectit.ui.rcp.provider;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

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
