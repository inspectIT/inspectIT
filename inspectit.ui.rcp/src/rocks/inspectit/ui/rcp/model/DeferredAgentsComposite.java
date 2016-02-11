package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.IElementCollector;

import com.google.common.base.Objects;

/**
 * This composite holds Agents of one CMR as the deferred children.
 * 
 * @author Ivan Senic
 * 
 */
public class DeferredAgentsComposite extends DeferredComposite implements ICmrRepositoryProvider {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Defines if so called 'old' agents are being shown. These agents never sent data since the CMR
	 * started.
	 */
	private final boolean showOldAgents;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository.
	 * @param showOldAgents
	 *            Defines if so called 'old' agents are being shown. These agents never sent data
	 *            since the CMR started.
	 */
	public DeferredAgentsComposite(CmrRepositoryDefinition cmrRepositoryDefinition, boolean showOldAgents) {
		this.showOldAgents = showOldAgents;
		setRepositoryDefinition(cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		monitor.beginTask("Loading agents..", IProgressMonitor.UNKNOWN);
		try {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				Map<PlatformIdent, AgentStatusData> agents = cmrRepositoryDefinition.getGlobalDataAccessService().getAgentsOverview();
				if (null != agents) {
					Map<PlatformIdent, AgentStatusData> filteredMap = new HashMap<PlatformIdent, AgentStatusData>(agents.size());
					for (Entry<PlatformIdent, AgentStatusData> entry : agents.entrySet()) {
						PlatformIdent platformIdent = entry.getKey();
						AgentStatusData agentStatusData = entry.getValue();
						// the agentstatusdata is null if the agent wasn't connected before
						if (showOldAgents || (!showOldAgents && agentStatusData != null)) {
							filteredMap.put(platformIdent, agentStatusData);
						}

					}

					List<Component> components = AgentFolderFactory.getAgentFolderTree(filteredMap, cmrRepositoryDefinition);
					for (Component component : components) {
						collector.add(component, monitor);
						((Composite) object).addChild(component);
					}
				}
			}
		} finally {
			collector.done();
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			this.cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryDefinition getRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return cmrRepositoryDefinition.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), cmrRepositoryDefinition);
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
		DeferredAgentsComposite that = (DeferredAgentsComposite) object;
		return Objects.equal(this.cmrRepositoryDefinition, that.cmrRepositoryDefinition);
	}

}
