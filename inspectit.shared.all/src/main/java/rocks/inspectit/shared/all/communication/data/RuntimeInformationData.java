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
	 * The uptime of the virtual machine in milliseconds.
	 */
	private long uptime = 0;

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
	 * Gets {@link #uptime}.
	 *
	 * @return {@link #uptime}
	 */
	public long getUptime() {
		return uptime;
	}

	/**
	 * Sets {@link #uptime}.
	 *
	 * @param uptime
	 *            New value for {@link #uptime}
	 */
	public void setUptime(long uptime) {
		this.uptime = uptime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + (int) (uptime ^ (uptime >>> 32));
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
		if (uptime != other.uptime) {
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
		size += objectSizes.getPrimitiveTypesSize(0, 0, 0, 0, 1, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
