package rocks.inspectit.shared.cs.storage.processor.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.ExceptionEvent;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.AbstractExtractorDataProcessor;

/**
 * This is a special type of processor. It extract the children information from a
 * {@link InvocationSequenceData} and passes the data to any chained processor that it has.
 *
 * @author Ivan Senic
 *
 */
public class InvocationExtractorDataProcessor extends AbstractExtractorDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3793308278679460679L;

	/**
	 * No-arg constructor.
	 */
	public InvocationExtractorDataProcessor() {
		this(Collections.<AbstractDataProcessor> emptyList());
	}

	/**
	 * Default constructor.
	 *
	 * @param chainedDataProcessors
	 *            List of the processors that will have the children of invocation passed to.
	 */
	public InvocationExtractorDataProcessor(List<AbstractDataProcessor> chainedDataProcessors) {
		super(chainedDataProcessors);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<Future<Void>> processData(DefaultData defaultData) {
		if (defaultData instanceof InvocationSequenceData) {
			InvocationSequenceData invocation = (InvocationSequenceData) defaultData;
			extractDataFromInvocation(invocation);
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

	/**
	 * Extract data from the invocation and return it to the storage writer to process it.
	 *
	 * @param invocation
	 *            {@link InvocationSequenceData}
	 */
	private void extractDataFromInvocation(InvocationSequenceData invocation) {
		if (null != invocation.getTimerData()) {
			passToChainedProcessors(invocation.getTimerData());
		}
		if (null != invocation.getSqlStatementData()) {
			passToChainedProcessors(invocation.getSqlStatementData());
		}
		if (null != invocation.getRemoteCallData()) {
			passToChainedProcessors(invocation.getRemoteCallData());
		}
		if (null != invocation.getExceptionSensorDataObjects()) {
			for (ExceptionSensorData exceptionSensorData : invocation.getExceptionSensorDataObjects()) {
				if (exceptionSensorData.getExceptionEvent() == ExceptionEvent.CREATED) {
					passToChainedProcessors(exceptionSensorData);
				}
			}
		}

		for (InvocationSequenceData child : invocation.getNestedSequences()) {
			extractDataFromInvocation(child);
		}
	}

}
