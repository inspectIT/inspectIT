package rocks.inspectit.shared.all.communication.data;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.IAggregatedData;

/**
 * Aggregated {@link InvocationSequenceData} object.
 *
 * @author Alexander Wert
 *
 */
public class AggregatedInvocationSequenceData extends InvocationSequenceData implements IAggregatedData<InvocationSequenceData> {

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
		TimerData timerData;
		if (InvocationSequenceDataHelper.hasTimerData(this)) {
			timerData = getTimerData();
		} else if (InvocationSequenceDataHelper.hasSQLData(this)) {
			timerData = getSqlStatementData();
		} else {
			throw new IllegalArgumentException("No timer data available!");
		}
		setDuration(timerData.getDuration());
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
		AggregatedInvocationSequenceData other = (AggregatedInvocationSequenceData) obj;
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
