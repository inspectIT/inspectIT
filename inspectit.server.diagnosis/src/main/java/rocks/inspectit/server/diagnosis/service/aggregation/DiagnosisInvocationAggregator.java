package rocks.inspectit.server.diagnosis.service.aggregation;

import rocks.inspectit.shared.all.communication.IAggregatedData;
import rocks.inspectit.shared.all.communication.data.AggregatedHttpTimerData;
import rocks.inspectit.shared.all.communication.data.AggregatedSqlStatementData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;

/**
 * Aggregation for {@link InvocationSequenceData}.
 *
 * Note: When {@link InvocationSequenceData} elements are aggregated, then the invocation structure
 * is ignored for aggregation.
 *
 * @author Alexander Wert, Christian Voegele
 *
 */
public class DiagnosisInvocationAggregator implements IAggregator<InvocationSequenceData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aggregate(IAggregatedData<InvocationSequenceData> aggregatedObject, InvocationSequenceData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAggregatedData<InvocationSequenceData> getClone(InvocationSequenceData invocationSequenceData) {
		AggregatedDiagnosisInvocationData clone = new AggregatedDiagnosisInvocationData();
		clone.setMethodIdent(invocationSequenceData.getMethodIdent());

		if (InvocationSequenceDataHelper.hasHttpTimerData(invocationSequenceData)) {
			HttpTimerData httpTimerData = (HttpTimerData) invocationSequenceData.getTimerData();
			HttpTimerData httpTimerDataClone = new AggregatedHttpTimerData();
			httpTimerDataClone.setHttpInfo(httpTimerData.getHttpInfo());
			httpTimerDataClone.setMethodIdent(httpTimerData.getMethodIdent());
			clone.setTimerData(httpTimerDataClone);
		} else if (InvocationSequenceDataHelper.hasTimerData(invocationSequenceData)) {
			TimerData timerData = invocationSequenceData.getTimerData();
			TimerData timerDataClone = new AggregatedTimerData();
			timerDataClone.setMethodIdent(timerData.getMethodIdent());
			clone.setTimerData(timerDataClone);
		} else if (InvocationSequenceDataHelper.hasSQLData(invocationSequenceData)) {
			SqlStatementData sqlStatementData = invocationSequenceData.getSqlStatementData();
			SqlStatementData sqlStatementDataClone = new AggregatedSqlStatementData();
			sqlStatementDataClone.setSql(sqlStatementData.getSql());
			sqlStatementDataClone.setMethodIdent(sqlStatementData.getMethodIdent());
			clone.setSqlStatementData(sqlStatementDataClone);
		} else {
			throw new IllegalArgumentException("No timer data available!");
		}
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAggregationKey(InvocationSequenceData invocationSequenceData) {
		if (InvocationSequenceDataHelper.hasSQLData(invocationSequenceData)) {
			return new Pair<Long, String>(invocationSequenceData.getMethodIdent(), invocationSequenceData.getSqlStatementData().getSql());
		} else if (InvocationSequenceDataHelper.hasHttpTimerData(invocationSequenceData)) {
			return new Pair<Long, String>(invocationSequenceData.getMethodIdent(), ((HttpTimerData) invocationSequenceData.getTimerData()).getHttpInfo().getUri());
		} else {
			return invocationSequenceData.getMethodIdent();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// we must make constant hashCode because of the caching
		result = (prime * result) + this.getClass().getName().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

}