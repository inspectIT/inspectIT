package info.novatec.inspectit.communication.data;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Sub-class of TimerData that has better performance when aggregating values from other TimerData
 * objects. This class is only meant to be used for purpose of aggregation of objects that will be
 * persisted in the database.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
@Table
public class DatabaseAggregatedTimerData extends TimerData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 3139731190115609664L;

	/**
	 * Default no-args constructor.
	 */
	public DatabaseAggregatedTimerData() {
		super();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param timestamp
	 *            the timestamp.
	 * @param platformIdent
	 *            the platform identifier.
	 * @param sensorTypeIdent
	 *            the sensor type identifier.
	 * @param methodIdent
	 *            the method identifier.
	 */
	public DatabaseAggregatedTimerData(Timestamp timestamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timestamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method does not aggregate the {@link InvocationAwareData} because it is not needed in
	 * the database.
	 */
	public void aggregateTimerData(TimerData timerData) {
		this.setCount(this.getCount() + timerData.getCount());
		this.setDuration(this.getDuration() + timerData.getDuration());
		this.calculateMax(timerData.getMax());
		this.calculateMin(timerData.getMin());

		if (timerData.isCpuMetricDataAvailable()) {
			this.setCpuDuration(this.getCpuDuration() + timerData.getCpuDuration());
			this.calculateCpuMin(timerData.getCpuMin());
			this.calculateCpuMax(timerData.getCpuMax());
		}

		if (timerData.isExclusiveTimeDataAvailable()) {
			this.addExclusiveDuration(timerData.getExclusiveDuration());
			this.setExclusiveCount(this.getExclusiveCount() + timerData.getExclusiveCount());
			this.calculateExclusiveMin(timerData.getExclusiveMin());
			this.calculateExclusiveMax(timerData.getExclusiveMax());
		}
	}
}
