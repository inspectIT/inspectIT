package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.cache.impl.BufferElement;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Buffer inserter data processor. Inserts only {@link MethodSensorData} data objects that are not
 * part of an invocation.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferInserterCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * Buffer to inser elements to.
	 */
	@Autowired
	IBuffer<MethodSensorData> buffer;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		buffer.put(new BufferElement<MethodSensorData>((MethodSensorData) defaultData));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		if (null == defaultData) {
			return false;
		} else if (!(defaultData instanceof MethodSensorData)) {
			// we only put to buffer method sensor data
			return false;
		} else if (defaultData instanceof InvocationAwareData) {
			// we don't put to buffer elements that are inside of invocation
			if (!((InvocationAwareData) defaultData).isOnlyFoundOutsideInvocations()) {
				return false;
			}
		} else if (defaultData instanceof InvocationSequenceData) {
			// we don't put to buffer invocations that are not root
			if (((InvocationSequenceData) defaultData).getParentSequence() != null) {
				return false;
			}
		}
		return true;
	}

}
