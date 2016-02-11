package rocks.inspectit.shared.cs.indexing.aggregation.impl;

import java.io.Serializable;

import rocks.inspectit.shared.all.communication.IAggregatedData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;

/**
 * {@link IAggregator} for {@link TimerData}.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataAggregator implements IAggregator<TimerData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8176969641431206899L;

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<TimerData> aggregatedObject, TimerData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public IAggregatedData<TimerData> getClone(TimerData timerData) {
		AggregatedTimerData clone = new AggregatedTimerData();
		clone.setPlatformIdent(timerData.getPlatformIdent());
		clone.setSensorTypeIdent(timerData.getSensorTypeIdent());
		clone.setMethodIdent(timerData.getMethodIdent());
		clone.setCharting(timerData.isCharting());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(TimerData object) {
		return object.getMethodIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// we must make constant hashCode because of the caching
		result = prime * result + this.getClass().getName().hashCode();
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
