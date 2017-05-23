package rocks.inspectit.server.influx.builder;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.HttpTimerDataHelper;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;

/**
 * Point builder for the business transactions. Information is derived from the
 * {@link InvocationSequenceData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class BusinessTransactionPointBuilder extends SinglePointBuilder<InvocationSequenceData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Class<InvocationSequenceData>> getDataClasses() {
		return Collections.singleton(InvocationSequenceData.class);
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
		builder.addField(Series.BusinessTransaction.FIELD_DURATION, data.getDuration());
		builder.addField(Series.BusinessTransaction.FIELD_TRACE_ID, data.getId());
		if (InvocationSequenceDataHelper.hasHttpTimerData(data) && HttpTimerDataHelper.hasResponseCode((HttpTimerData) data.getTimerData())) {
			builder.addField(Series.BusinessTransaction.FIELD_HTTP_RESPONSE_CODE, ((HttpTimerData) data.getTimerData()).getHttpResponseStatus());
		}
	}

}
