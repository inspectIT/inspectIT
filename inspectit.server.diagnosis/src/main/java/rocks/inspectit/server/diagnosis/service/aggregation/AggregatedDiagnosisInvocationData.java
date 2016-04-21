package rocks.inspectit.server.diagnosis.service.aggregation;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.IAggregatedData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;

/**
 * Aggregated {@link InvocationSequenceData} object.
 *
 * @author Alexander Wert, Christian Voegele
 *
 */
public class AggregatedDiagnosisInvocationData extends InvocationSequenceData implements IAggregatedData<InvocationSequenceData> {

	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = 3389275017405936921L;

	/**
	 * List of {@link InvocationSequenceData} instances that are part of the aggregation.
	 */
	private final List<InvocationSequenceData> rawInvocationsSequenceElements = new ArrayList<InvocationSequenceData>(1);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aggregate(InvocationSequenceData data) {
		if (InvocationSequenceDataHelper.hasTimerData(this)) {
			this.getData().getTimerData().aggregateTimerData(data.getTimerData());
			this.getData().setDuration(this.getData().getTimerData().getDuration());
		} else if (InvocationSequenceDataHelper.hasSQLData(this)) {
			this.getData().getSqlStatementData().aggregateTimerData(data.getSqlStatementData());
			this.getData().setDuration(this.getData().getSqlStatementData().getDuration());
		} else {
			throw new IllegalArgumentException("No timer data available!");
		}
		rawInvocationsSequenceElements.add(data);
	}

	/**
	 * Gets {@link #rawInvocationsSequenceElements}.
	 *
	 * @return {@link #rawInvocationsSequenceElements}
	 */
	public List<InvocationSequenceData> getRawInvocationsSequenceElements() {
		return rawInvocationsSequenceElements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InvocationSequenceData getData() {
		return this;
	}

	/**
	 * Returns the number of {@link InvocationSequenceData} instances that are part of the
	 * aggregation.
	 *
	 * @return Returns the number of {@link InvocationSequenceData} instances that are part of the
	 *         aggregation.
	 */
	public int size() {
		return rawInvocationsSequenceElements.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.rawInvocationsSequenceElements == null) ? 0 : this.rawInvocationsSequenceElements.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AggregatedDiagnosisInvocationData other = (AggregatedDiagnosisInvocationData) obj;
		if (this.rawInvocationsSequenceElements == null) {
			if (other.rawInvocationsSequenceElements != null) {
				return false;
			}
		} else if (!this.rawInvocationsSequenceElements.equals(other.rawInvocationsSequenceElements)) {
			return false;
		}
		return true;
	}
}