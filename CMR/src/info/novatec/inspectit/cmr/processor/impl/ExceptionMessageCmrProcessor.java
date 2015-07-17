package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import org.hibernate.StatelessSession;

/**
 * Processor that connects error messages in the {@link ExceptionSensorData}.
 * 
 * @author Ivan Senic
 * 
 */
public class ExceptionMessageCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, StatelessSession session) {
		connectErrorMessagesInExceptionData((ExceptionSensorData) defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof ExceptionSensorData;
	}

	/**
	 * Connects exception message between linked exception data.
	 * 
	 * @param exceptionSensorData
	 *            Parent exception data, thus the one that has exception event CREATED.
	 */
	private void connectErrorMessagesInExceptionData(ExceptionSensorData exceptionSensorData) {
		ExceptionSensorData child = exceptionSensorData.getChild();
		if (null != child) {
			child.setErrorMessage(exceptionSensorData.getErrorMessage());
			connectErrorMessagesInExceptionData(child);
		}
	}

}
