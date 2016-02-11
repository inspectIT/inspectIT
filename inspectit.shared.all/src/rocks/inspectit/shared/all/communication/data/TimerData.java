package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * The timer data class stores information about the execution time of a java method.
 * 
 * Notes
 * <ul>
 * <li>This class is used for multiple purposes over time. Be aware that although this class
 * provides exclusive timings these are only available if the timer is included in an invocation
 * sequence and is calculated on the CMR NOT the agent.</li>
 * <li>In order to check if certain information is available use the checking methods
 * <code>isCpuMetricAvailable</code> and <code>isExclusiveMetricAvailable</code></li>
 * <li>To change a minimum of maximum (normal time, exclusive, cpu) use the calculate methods as
 * these deal with the internals in a correct way (we must initialize some fields seemingly in a
 * strange way but we want to improve performance and size)</li>
 * </ul>
 * 
 * @author Patrice Bouillet
 * @author Stefan Siegl
 * 
 */
@Entity
@Table(indexes = { @Index(name = "time_stamp_idx", columnList = "timeStamp") })
public class TimerData extends InvocationAwareData {

	/**
	 * Generated serial UID.
	 */
	private static final long serialVersionUID = 8992128958802371539L;

	/**
	 * The minimum value.
	 */
	private double min = -1;

	/**
	 * The maximum value.
	 */
	private double max = -1;

	/**
	 * The count.
	 */
	private long count = 0;

	/**
	 * The complete duration.
	 */
	private double duration = 0;

	/**
	 * The variance (optional parameter).
	 */
	private double variance;

	/**
	 * The cpu minimum value.
	 */
	private double cpuMin = -1;

	/**
	 * The cpu maximum value.
	 */
	private double cpuMax = -1;

	/**
	 * The cpu complete duration.
	 */
	private double cpuDuration = 0;

	/**
	 * Exclusive count. Needed because this count can be less than the total count.
	 */
	private long exclusiveCount = 0;

	/**
	 * Exclusive duration.
	 */
	private double exclusiveDuration;

	/**
	 * Exclusive max duration.
	 */
	private double exclusiveMax = -1;

	/**
	 * Exclusive min duration.
	 */
	private double exclusiveMin = -1;

	/**
	 * Defines if the data should be saved to database and available for charting.
	 */
	private boolean charting;

	/**
	 * Default no-args constructor.
	 */
	public TimerData() {
	}

	/**
	 * Creates a new instance of the <code>Timerdata</code>.
	 * 
	 * @param timeStamp
	 *            the timestamp.
	 * @param platformIdent
	 *            the platform identifier.
	 * @param sensorTypeIdent
	 *            the sensor type identifier.
	 * @param methodIdent
	 *            the method identifier.
	 */
	public TimerData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * Creates a new instance of the <code>Timerdata</code>.
	 * 
	 * @param timeStamp
	 *            the timestamp.
	 * @param platformIdent
	 *            the platform identifier.
	 * @param sensorTypeIdent
	 *            the sensor type identifier.
	 * @param methodIdent
	 *            the method identifier.
	 * @param parameterContentData
	 *            The information of captured parameters.
	 */
	public TimerData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List<ParameterContentData> parameterContentData) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);
	}

	/**
	 * <b> CAREFUL! min is initialized to -1 due to data transfer sizes! </b>
	 * 
	 * @return the min time.
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Sets the minimum and deals with the -1 initialization!.
	 * 
	 * @param min
	 *            the minimum value to be set to if it is smaller than the minimum.
	 */
	public void calculateMin(double min) {
		if (this.min == -1) {
			this.min = min;
		} else {
			this.min = Math.min(this.min, min);
		}
	}

	/**
	 * Gets {@link #max}.
	 * 
	 * @return {@link #max}
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Sets the maximum if the given value is bigger than the current value.
	 * 
	 * @param max
	 *            the maximum to be set
	 */
	public void calculateMax(double max) {
		this.max = Math.max(this.max, max);
	}

	/**
	 * Gets {@link #count}.
	 * 
	 * @return {@link #count}
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Sets {@link #count}.
	 * 
	 * @param count
	 *            New value for {@link #count}
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * increases the count by 1.
	 */
	public void increaseCount() {
		this.count++;
	}

	/**
	 * Gets {@link #duration}.
	 * 
	 * @return {@link #duration}
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Sets {@link #duration}.
	 * 
	 * @param duration
	 *            New value for {@link #duration}
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * adds the given time to the duration.
	 * 
	 * @param duration
	 *            the duration to add.
	 */
	public void addDuration(double duration) {
		this.duration += duration;
	}

	/**
	 * Returns average time.
	 * 
	 * @return Returns average time.
	 */
	public double getAverage() {
		return duration / count;
	}

	/**
	 * Gets {@link #variance}.
	 * 
	 * @return {@link #variance}
	 */
	public double getVariance() {
		return variance;
	}

	/**
	 * Sets {@link #variance}.
	 * 
	 * @param variance
	 *            New value for {@link #variance}
	 */
	public void setVariance(double variance) {
		this.variance = variance;
	}

	/**
	 * Checks if this data object contains captured parameters.
	 * 
	 * @return if this data object contains captured parameters.
	 */
	public boolean providesCapturedParameters() {
		return null != getParameterContentData() && !getParameterContentData().isEmpty();
	}

	/**
	 * Sets the minimum and deals with the -1 initialization!.
	 * 
	 * @param min
	 *            the minimum value to be set to if it is smaller than the minimum.
	 */
	public void calculateCpuMin(double min) {
		if (cpuMin == -1) {
			cpuMin = min;
		} else {
			cpuMin = Math.min(cpuMin, min);
		}
	}

	/**
	 * Sets the maximum if the given value is bigger than the current value.
	 * 
	 * @param time
	 *            the maximum to be set
	 */
	public void calculateCpuMax(double time) {
		cpuMax = Math.max(cpuMax, time);
	}

	/**
	 * <b> Notice: ensure to check using the <code> isCpuMetricDataAvailable() </code> if cpu metric
	 * data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return the cpuMin
	 */
	public double getCpuMin() {
		return cpuMin;
	}

	/**
	 * <b> Notice: ensure to check using the <code> isCpuMetricDataAvailable() </code> if cpu metric
	 * data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return the cpuMax
	 */
	public double getCpuMax() {
		return cpuMax;
	}

	/**
	 * <b> Notice: ensure to check using the <code> isCpuMetricDataAvailable() </code> if cpu metric
	 * data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return the cpuDuration
	 */
	public double getCpuDuration() {
		return cpuDuration;
	}

	/**
	 * @param cpuDuration
	 *            the cpuDuration to set
	 */
	public void setCpuDuration(double cpuDuration) {
		this.cpuDuration = cpuDuration;
	}

	/**
	 * @param cpuDuration
	 *            the cpuDuration to add
	 */
	public void addCpuDuration(double cpuDuration) {
		this.cpuDuration += cpuDuration;
	}

	/**
	 * <b> Notice: ensure to check using the <code> isCpuMetricDataAvailable() </code> if cpu metric
	 * data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return the cpuAverage
	 */
	public double getCpuAverage() {
		return cpuDuration / count;
	}

	/**
	 * <b> Notice: ensure to check using the <code> isExclusiveMetricDataAvailable() </code> if cpu
	 * metric data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return exclusive count
	 */
	public long getExclusiveCount() {
		return exclusiveCount;
	}

	/**
	 * Sets {@link #exclusiveCount}.
	 * 
	 * @param exclusiveCount
	 *            New value for {@link #exclusiveCount}
	 */
	public void setExclusiveCount(long exclusiveCount) {
		this.exclusiveCount = exclusiveCount;
	}

	/**
	 * increases the exclusive count by 1.
	 */
	public void increaseExclusiveCount() {
		this.exclusiveCount++;
	}

	/**
	 * <b> Notice: ensure to check using the <code> isExclusiveMetricDataAvailable() </code> if cpu
	 * metric data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return duration
	 */
	public double getExclusiveDuration() {
		return exclusiveDuration;
	}

	/**
	 * Sets {@link #exclusiveDuration}.
	 * 
	 * @param exclusiveDuration
	 *            New value for {@link #exclusiveDuration}
	 */
	public void setExclusiveDuration(double exclusiveDuration) {
		this.exclusiveDuration = exclusiveDuration;
	}

	/**
	 * adds the given time to the exclusive duration.
	 * 
	 * @param exclusiveDuration
	 *            the duration to add.
	 */
	public void addExclusiveDuration(double exclusiveDuration) {
		this.exclusiveDuration += exclusiveDuration;
	}

	/**
	 * <b> Notice: ensure to check using the <code> isExclusiveMetricDataAvailable() </code> if cpu
	 * metric data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return exlusive max
	 */
	public double getExclusiveMax() {
		return exclusiveMax;
	}

	/**
	 * Sets the maximum if the given value is bigger than the current value.
	 * 
	 * @param max
	 *            the maximum to be set
	 */
	public void calculateExclusiveMax(double max) {
		exclusiveMax = Math.max(exclusiveMax, max);
	}

	/**
	 * <b> Notice: ensure to check using the <code> isExclusiveMetricDataAvailable() </code> if cpu
	 * metric data is in fact available, otherwise you might get strange results. </b>
	 * 
	 * @return the exclusive minimum time.
	 */
	public double getExclusiveMin() {
		return exclusiveMin;
	}

	/**
	 * Sets the minimum and deals with the -1 initialization!.
	 * 
	 * @param min
	 *            the minimum value to be set to if it is smaller than the minimum.
	 */
	public void calculateExclusiveMin(double min) {
		if (exclusiveMin == -1) {
			exclusiveMin = min;
		} else {
			exclusiveMin = Math.min(exclusiveMin, min);
		}
	}

	/**
	 * Returns the average exclusive time calculated as exclusive duration % count.
	 * 
	 * @return Average exclusive time.
	 */
	public double getExclusiveAverage() {
		return exclusiveDuration / exclusiveCount;
	}

	// Private setters for hibernate. Users should use the calculate methods.

	@SuppressWarnings("unused")
	private void setMin(double min) {
		this.min = min;
	}

	@SuppressWarnings("unused")
	private void setMax(double max) {
		this.max = max;
	}

	@SuppressWarnings("unused")
	private void setCpuMin(double cpuMin) {
		this.cpuMin = cpuMin;
	}

	@SuppressWarnings("unused")
	private void setCpuMax(double cpuMax) {
		this.cpuMax = cpuMax;
	}

	@SuppressWarnings("unused")
	private void setExclusiveMax(double exclusiveMax) {
		this.exclusiveMax = exclusiveMax;
	}

	@SuppressWarnings("unused")
	private void setExclusiveMin(double exclusiveMin) {
		this.exclusiveMin = exclusiveMin;
	}

	/**
	 * Gets {@link #charting}.
	 * 
	 * @return {@link #charting}
	 */
	public boolean isCharting() {
		return charting;
	}

	/**
	 * Sets {@link #charting}.
	 * 
	 * @param charting
	 *            New value for {@link #charting}
	 */
	public void setCharting(boolean charting) {
		this.charting = charting;
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData finalizeData() {
		// no need
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(0, 1, 0, 0, 2, 10);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public double getInvocationAffiliationPercentage() {
		return (double) getObjectsInInvocationsCount() / count;
	}

	/**
	 * Whether or not this timer data contains cpu related metrics.
	 * 
	 * @return Whether or not this timer data contains cpu related metrics.
	 */
	public boolean isCpuMetricDataAvailable() {
		// cpu duration cannot be used as comparison, because:
		// in the timer hook cpu time is calculated using an JVM JMX bean that only has a resolution
		// of 10ms (or more depending on the system). Thus even though CPU times could be
		// calculated, the time might be 0. So in this case 0 will be added to the duration. But
		// still, yes, we do have cpu metric data available, but it is 0.
		return cpuMin != -1;
	}

	/**
	 * Whether or not this timer data contains exclusive time metrics.
	 * 
	 * @return Whether or not this timer data contains exclusive time metrics.
	 */
	public boolean isExclusiveTimeDataAvailable() {
		return exclusiveMin != -1;
	}

	/**
	 * Whether or not this timer data contains time metrics.
	 * 
	 * @return Whether or not this timer data contains time metrics.
	 */
	public boolean isTimeDataAvailable() {
		return min != -1;
	}

	/**
	 * Aggregates the values given in the supplied timer data parameter to the objects data.
	 * 
	 * @param timerData
	 *            Data to be aggregated into current object.
	 */
	public void aggregateTimerData(TimerData timerData) {
		super.aggregateInvocationAwareData(timerData);
		this.setCount(this.getCount() + timerData.getCount());
		this.setDuration(this.getDuration() + timerData.getDuration());
		this.calculateMax(timerData.getMax());
		this.calculateMin(timerData.getMin());

		if (timerData.isCpuMetricDataAvailable()) {
			this.setCpuDuration(this.getCpuDuration() + timerData.getCpuDuration());
			this.calculateCpuMax(timerData.getCpuMax());
			this.calculateCpuMin(timerData.getCpuMin());
		}
		if (timerData.isExclusiveTimeDataAvailable()) {
			this.addExclusiveDuration(timerData.getExclusiveDuration());
			this.setExclusiveCount(this.getExclusiveCount() + timerData.getExclusiveCount());
			this.calculateExclusiveMax(timerData.getExclusiveMax());
			this.calculateExclusiveMin(timerData.getExclusiveMin());
		}
		this.charting = this.charting | timerData.isCharting();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (count ^ (count >>> 32));
		long temp;
		temp = Double.doubleToLongBits(cpuDuration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cpuMax);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cpuMin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(duration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (exclusiveCount ^ (exclusiveCount >>> 32));
		temp = Double.doubleToLongBits(exclusiveDuration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(exclusiveMax);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(exclusiveMin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(variance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (charting ? 1231 : 1237);
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
		TimerData other = (TimerData) obj;
		if (count != other.count) {
			return false;
		}
		if (Double.doubleToLongBits(cpuDuration) != Double.doubleToLongBits(other.cpuDuration)) {
			return false;
		}
		if (Double.doubleToLongBits(cpuMax) != Double.doubleToLongBits(other.cpuMax)) {
			return false;
		}
		if (Double.doubleToLongBits(cpuMin) != Double.doubleToLongBits(other.cpuMin)) {
			return false;
		}
		if (Double.doubleToLongBits(duration) != Double.doubleToLongBits(other.duration)) {
			return false;
		}
		if (exclusiveCount != other.exclusiveCount) {
			return false;
		}
		if (Double.doubleToLongBits(exclusiveDuration) != Double.doubleToLongBits(other.exclusiveDuration)) {
			return false;
		}
		if (Double.doubleToLongBits(exclusiveMax) != Double.doubleToLongBits(other.exclusiveMax)) {
			return false;
		}
		if (Double.doubleToLongBits(exclusiveMin) != Double.doubleToLongBits(other.exclusiveMin)) {
			return false;
		}
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max)) {
			return false;
		}
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min)) {
			return false;
		}
		if (Double.doubleToLongBits(variance) != Double.doubleToLongBits(other.variance)) {
			return false;
		}
		if (charting != other.charting) {
			return false;
		}
		return true;
	}

}
