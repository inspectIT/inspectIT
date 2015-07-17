package info.novatec.inspectit.storage.processor.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

/**
 * This processor writes an cloned invocation without children to the {@link StorageWriter}.
 * 
 * @author Ivan Senic
 * 
 */
public class InvocationClonerDataProcessor extends AbstractDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4882060472220163089L;

	/**
	 * {@inheritDoc}
	 */
	protected Collection<Future<Void>> processData(DefaultData defaultData) {
		if (defaultData instanceof InvocationSequenceData) {
			InvocationSequenceData invocation = (InvocationSequenceData) defaultData;
			InvocationSequenceData clone = invocation.getClonedInvocationSequence();
			Future<Void> future = getStorageWriter().write(clone);
			if (null != future) {
				return Collections.singleton(future);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
