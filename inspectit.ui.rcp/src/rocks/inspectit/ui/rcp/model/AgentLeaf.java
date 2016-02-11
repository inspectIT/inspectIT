package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import com.google.common.base.Objects;

/**
 * Agent leaf for the tree in the Repository Manager.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentLeaf extends Leaf implements ICmrRepositoryAndAgentProvider {

	/**
	 * Agent.
	 */
	private PlatformIdent platformIdent;

	/**
	 * {@link AgentStatusData}.
	 */
	private AgentStatusData agentStatusData;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private final CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Is this leaf part of the folder.
	 */
	private final boolean inFolder;

	/**
	 * Default constructor.
	 * 
	 * @param platformIdent
	 *            Agent to display in leaf.
	 * @param agentStatusData
	 *            {@link AgentStatusData}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 * @param inFolder
	 *            Is this leaf part of the folder.
	 */
	public AgentLeaf(PlatformIdent platformIdent, AgentStatusData agentStatusData, CmrRepositoryDefinition cmrRepositoryDefinition, boolean inFolder) {
		this.platformIdent = platformIdent;
		this.agentStatusData = agentStatusData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.inFolder = inFolder;
	}

	/**
	 * Gets {@link #platformIdent}.
	 * 
	 * @return {@link #platformIdent}
	 */
	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * Sets {@link #platformIdent}.
	 * 
	 * @param platformIdent
	 *            New value for {@link #platformIdent}
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #agentStatusData}.
	 * 
	 * @return {@link #agentStatusData}
	 */
	public AgentStatusData getAgentStatusData() {
		return agentStatusData;
	}

	/**
	 * Sets {@link #agentStatusData}.
	 * 
	 * @param agentStatusData
	 *            New value for {@link #agentStatusData}
	 */
	public void setAgentStatusData(AgentStatusData agentStatusData) {
		this.agentStatusData = agentStatusData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return platformIdent.getAgentName();
	}

	/**
	 * Gets {@link #inFolder}.
	 * 
	 * @return {@link #inFolder}
	 */
	public boolean isInFolder() {
		return inFolder;
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
		return Objects.hashCode(super.hashCode(), platformIdent, cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		AgentLeaf that = (AgentLeaf) object;
		return Objects.equal(this.platformIdent, that.platformIdent) && Objects.equal(this.cmrRepositoryDefinition, that.cmrRepositoryDefinition);
	}
}
