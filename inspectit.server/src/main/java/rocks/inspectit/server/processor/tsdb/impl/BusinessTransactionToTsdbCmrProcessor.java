package rocks.inspectit.server.processor.tsdb.impl;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.IInfluxDBService;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * This processor writes business transaction timings to a timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_BUSINESS_TRANSACTIONS = "businessTransactions";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * Application name tag.
	 */
	private static final String TAG_APPLICATION_NAME = "applicationName";

	/**
	 * Business transaction name tag.
	 */
	private static final String TAG_BUSINESS_TRANSACTION_NAME = "businessTxName";

	/**
	 * Duration field.
	 */
	private static final String FIELD_DURATION = "duration";

	/**
	 * {@link IInfluxDBService} used to write data to an influx database.
	 */
	@Autowired
	IInfluxDBService influxDbService;

	/**
	 * {@link ICachedDataService} used to retrieve the business context.
	 */
	@Autowired
	ICachedDataService cachedDataService;

	/**
	 * {@link IGlobalDataAccessService} used to retrieve the agent information.
	 */
	@Autowired
	IGlobalDataAccessService globalDataAccessService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData invocationSequence = (InvocationSequenceData) defaultData;

		String businessTxName = BusinessTransactionDefinition.UNKNOWN_BUSINESS_TX;
		String applicationName = ApplicationDefinition.UNKNOWN_APP;
		BusinessTransactionData businessTx = cachedDataService.getBusinessTransactionForId(invocationSequence.getApplicationId(), invocationSequence.getBusinessTransactionId());
		if (null != businessTx) {
			businessTxName = businessTx.getName();
			applicationName = businessTx.getApplication().getName();
		}
		String agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(defaultData.getPlatformIdent());
			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}

		// measurement
		Builder builder = Point.measurement(MEASUREMENT_BUSINESS_TRANSACTIONS);
		builder.time(invocationSequence.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(invocationSequence.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);
		builder.tag(TAG_APPLICATION_NAME, applicationName);
		builder.tag(TAG_BUSINESS_TRANSACTION_NAME, businessTxName);

		// fields
		builder.addField(FIELD_DURATION, invocationSequence.getDuration());

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof InvocationSequenceData);
	}

}
