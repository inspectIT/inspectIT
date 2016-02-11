package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.cache.IBuffer;
import rocks.inspectit.server.cache.impl.BufferElement;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

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
