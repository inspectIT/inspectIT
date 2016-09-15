package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;

/**
 * Point builder for the business transactions. Information is derived from the
 * {@link InvocationSequenceData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class BusinessTransactionPointBuilder extends DefaultDataPointBuilder<InvocationSequenceData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<InvocationSequenceData> getDataClass() {
		return InvocationSequenceData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.BusinessTransaction.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTags(InvocationSequenceData data, Builder builder) {
		super.addTags(data, builder);

		String businessTxName = BusinessTransactionDefinition.UNKNOWN_BUSINESS_TX;
		String applicationName = ApplicationDefinition.UNKNOWN_APP;

		BusinessTransactionData businessTx = cachedDataService.getBusinessTransactionForId(data.getApplicationId(), data.getBusinessTransactionId());
		if (null != businessTx) {
			businessTxName = businessTx.getName();
			applicationName = businessTx.getApplication().getName();
		}

		builder.tag(Series.BusinessTransaction.TAG_APPLICATION_NAME, applicationName);
		builder.tag(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, businessTxName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(InvocationSequenceData data, Builder builder) {
		// fields
		builder.addField(Series.BusinessTransaction.FIELD_DURATION, data.getDuration());
		builder.addField(Series.BusinessTransaction.FIELD_TRACE_ID, data.getId());
	}

}
