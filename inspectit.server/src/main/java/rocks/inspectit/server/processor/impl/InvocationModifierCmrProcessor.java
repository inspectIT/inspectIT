package rocks.inspectit.server.processor.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractChainedCmrDataProcessor;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.ExceptionEvent;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * Processor performing necessary calculation and fixes. This is special type of chained processor
 * that does not pass the incoming object to the chained processors, but might do so with some other
 * objects.
 *
 * @author Ivan Senic
 *
 */
public class InvocationModifierCmrProcessor extends AbstractChainedCmrDataProcessor {

	/**
	 * Message processor for exception that we need to call directly. It's because we need to do
	 * that for all exceptions, but we will send only one to the chained processors, cause in the
	 * chain there will be indexed and stuff and we don’t want that for all exceptions, but only
	 * that survive constructor delegation.
	 */
	@Autowired
	ExceptionMessageCmrProcessor exceptionMessageCmrProcessor;

	/**
	 * Default constructor.
	 *
	 * @param dataProcessors
	 *            Chained processors.
	 */
	public InvocationModifierCmrProcessor(List<AbstractCmrDataProcessor> dataProcessors) {
		super(dataProcessors);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData invocation = (InvocationSequenceData) defaultData;
		extractDataFromInvocation(entityManager, invocation, invocation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean shouldBePassedToChainedProcessors(DefaultData defaultData) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

	/**
	 * Extract data from the invocation in the way that timer data is saved to the Db, while SQL
	 * statements and Exceptions are indexed into the root branch.
	 *
	 * @param entityManager
	 *            {@link EntityManager} needed for DB persistence.
	 * @param invData
	 *            Invocation data to be extracted.
	 * @param topInvocationParent
	 *            Top invocation object.
	 *
	 */
	private void extractDataFromInvocation(EntityManager entityManager, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		double exclusiveDurationDelta = 0d;

		for (InvocationSequenceData child : invData.getNestedSequences()) {
			// pass child to chained processors
			passToChainedProcessors(child, entityManager);

			// include times from timer, sql or invocation itself
			if (null != child.getTimerData()) {
				exclusiveDurationDelta += child.getTimerData().getDuration();
			} else if (null != child.getSqlStatementData()) {
				// I don't know if the situation that both timer and sql are set in one
				// invocation, but just to be sure I only include the time of the sql, if i did
				// not already included the time of the timer before
				exclusiveDurationDelta += child.getSqlStatementData().getDuration();
			} else {
				exclusiveDurationDelta += InvocationSequenceDataHelper.computeNestedDuration(child);
			}

			// go to the recursion
			extractDataFromInvocation(entityManager, child, topInvocationParent);
		}

		// process the SQL Statement, Timer and Remote
		processSqlStatementData(entityManager, invData, topInvocationParent);
		processTimerData(entityManager, invData, topInvocationParent, exclusiveDurationDelta);
		processExceptionSensorData(entityManager, invData, topInvocationParent);
		processRemoteCallData(entityManager, invData, topInvocationParent);
	}

	/**
	 * Process RemoteCalls if one exists in the invData object and passes it to the chained
	 * processors.
	 *
	 * @param entityManager
	 *            {@link EntityManager} needed for DB persistence.
	 * @param invData
	 *            Invocation data to be processed.
	 * @param topInvocationParent
	 *            Top invocation object.
	 */
	private void processRemoteCallData(EntityManager entityManager, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		RemoteCallData remoteCallData = invData.getRemoteCallData();
		if (null != remoteCallData) {
			if (remoteCallData.isCalling()) {
				topInvocationParent.setNestedOutgoingRemoteCalls(Boolean.TRUE);
			} else {
				topInvocationParent.setNestedIncommingRemoteCalls(Boolean.TRUE);
			}
			remoteCallData.addInvocationParentId(topInvocationParent.getId());
			passToChainedProcessors(remoteCallData, entityManager);
		}
	}

	/**
	 * Process SQL statement if one exists in the invData object and passes it to the chained
	 * processors.
	 *
	 * @param entityManager
	 *            {@link EntityManager} needed for DB persistence.
	 * @param invData
	 *            Invocation data to be processed.
	 * @param topInvocationParent
	 *            Top invocation object.
	 */
	private void processSqlStatementData(EntityManager entityManager, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		SqlStatementData sqlStatementData = invData.getSqlStatementData();
		if (null != sqlStatementData) {
			topInvocationParent.setNestedSqlStatements(Boolean.TRUE);
			sqlStatementData.addInvocationParentId(topInvocationParent.getId());
			passToChainedProcessors(sqlStatementData, entityManager);
		}
	}

	/**
	 * Process timer data if one exists in the invData object and passes it to the chained
	 * processors.
	 *
	 * @param entityManager
	 *            {@link EntityManager} needed for DB persistence.
	 * @param invData
	 *            Invocation data to be processed.
	 * @param topInvocationParent
	 *            Top invocation object.
	 * @param exclusiveDurationDelta
	 *            Duration to subtract from timer duration to get the exclusive duration.
	 */
	private void processTimerData(EntityManager entityManager, InvocationSequenceData invData, InvocationSequenceData topInvocationParent, double exclusiveDurationDelta) {
		TimerData timerData = invData.getTimerData();
		if (null != timerData) {
			double exclusiveTime = invData.getTimerData().getDuration() - exclusiveDurationDelta;
			timerData.setExclusiveCount(1L);
			timerData.setExclusiveDuration(exclusiveTime);
			timerData.calculateExclusiveMax(exclusiveTime);
			timerData.calculateExclusiveMin(exclusiveTime);
			timerData.addInvocationParentId(topInvocationParent.getId());
			passToChainedProcessors(invData.getTimerData(), entityManager);
		}
	}

	/**
	 * Process all the exceptions in the invData and passes exceptions to the chained processors.
	 * <br>
	 * <br>
	 * Note also that only exception data with CREATED event are processed, since the PASSED and
	 * HANDLED should be connected as children to the CREATED one.
	 *
	 * @param entityManager
	 *            {@link EntityManager} needed for DB persistence.
	 * @param invData
	 *            Invocation data to be processed.
	 * @param topInvocationParent
	 *            Top invocation object.
	 */
	private void processExceptionSensorData(EntityManager entityManager, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		if (CollectionUtils.isNotEmpty(invData.getExceptionSensorDataObjects())) {
			for (ExceptionSensorData exceptionData : invData.getExceptionSensorDataObjects()) {
				if (exceptionData.getExceptionEvent() == ExceptionEvent.CREATED) {
					// only if created exception is in invocation set to the parent
					topInvocationParent.setNestedExceptions(Boolean.TRUE);

					// we need to directly call Exception message processor, cause it can not be
					// chained
					exceptionMessageCmrProcessor.process(exceptionData, entityManager);
					exceptionData.addInvocationParentId(topInvocationParent.getId());
					passToChainedProcessors(exceptionData, entityManager);
				}
			}
		}
	}

}
