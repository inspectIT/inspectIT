package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;

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
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
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
