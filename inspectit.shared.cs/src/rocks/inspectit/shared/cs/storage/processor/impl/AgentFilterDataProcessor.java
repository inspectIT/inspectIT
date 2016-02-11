package info.novatec.inspectit.storage.processor.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.processor.AbstractChainedDataProcessor;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Processor that filters out data based on the agent id.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentFilterDataProcessor extends AbstractChainedDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8536247869506417703L;

	/**
	 * IDs of the Agents which data should be passed to the chained processors.
	 */
	private Set<Long> agentIds;

	/**
	 * No-arg constructor.
	 */
	public AgentFilterDataProcessor() {
		this.agentIds = new HashSet<Long>();
	}

	/**
	 * Default constructor.
	 * 
	 * @param dataProcessors
	 *            List of chained processors.
	 * @param agentIds
	 *            IDs of the Agents which data should be passed to the chained processors.
	 */
	public AgentFilterDataProcessor(List<AbstractDataProcessor> dataProcessors, Set<Long> agentIds) {
		super(dataProcessors);
		this.agentIds = agentIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean shouldBePassedToChainedProcessors(DefaultData defaultData) {
		return agentIds.contains(Long.valueOf(defaultData.getPlatformIdent()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return true;
	}

}
