package rocks.inspectit.shared.all.communication.data;

import java.sql.Timestamp;

import javax.persistence.Entity;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.SystemSensorData;

/**
 * This class provide dynamic informations about the runtime system of the virtual machine.
 *
 * @author Eduard Tudenhoefner
 *
 */
@Entity
public class RuntimeInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -6969524429547729867L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The total uptime of the virtual machine in milliseconds.
	 */
	private long totalUptime = 0;

	/**
	 * Default no-args constructor.
	 */
	public RuntimeInformationData() {
	}

	/**
	 * The constructor which needs three parameters.
	 *
	 * @param timeStamp
	 *            The Timestamp.
	 * @param platformIdent
	 *            The PlatformIdent.
	 * @param sensorTypeIdent
	 *            The SensorTypeIdent.
	 */
	public RuntimeInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);
	}

	/**
	 * Gets {@link #count}.
	 *
	 * @return {@link #count}
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets {@link #count}.
	 *
	 * @param count
	 *            New value for {@link #count}
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * increases the count by 1.
	 */
	public void incrementCount() {
		this.count++;
	}

	/**
	 * increases the uptime by the given value.
	 *
	 * @param uptime
	 *            the value to add to the uptime.
	 */
	public void addUptime(long uptime) {
		this.totalUptime += uptime;
	}

	/**
	 * Gets {@link #totalUptime}.
	 *
	 * @return {@link #totalUptime}
	 */
	public long getTotalUptime() {
		return totalUptime;
	}

	/**
	 * Sets {@link #totalUptime}.
	 *
	 * @param totalUptime
	 *            New value for {@link #totalUptime}
	 */
	public void setTotalUptime(long totalUptime) {
		this.totalUptime = totalUptime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + count;
		result = (prime * result) + (int) (totalUptime ^ (totalUptime >>> 32));
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
		RuntimeInformationData other = (RuntimeInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (totalUptime != other.totalUptime) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 1, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
