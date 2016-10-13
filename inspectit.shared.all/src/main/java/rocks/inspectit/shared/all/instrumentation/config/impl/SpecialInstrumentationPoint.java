package rocks.inspectit.shared.all.instrumentation.config.impl;

import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;

/**
 * Special instrumentation point. These points are not directly defined by user, but are specific
 * points that we add based on the specific settings or our implementation.
 *
 * @author Ivan Senic
 *
 */
public class SpecialInstrumentationPoint implements IMethodInstrumentationPoint {

	/**
	 * The method id.
	 */
	private long id;

	/**
	 * The sensor id.
	 */
	private long sensorId;

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #sensorId}.
	 * 
	 * @return {@link #sensorId}
	 */
	public long getSensorId() {
		return this.sensorId;
	}

	/**
	 * Sets {@link #sensorId}.
	 * 
	 * @param sensorId
	 *            New value for {@link #sensorId}
	 */
	public void setSensorId(long sensorId) {
		this.sensorId = sensorId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (this.id ^ (this.id >>> 32));
		result = (prime * result) + (int) (this.sensorId ^ (this.sensorId >>> 32));
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
		SpecialInstrumentationPoint other = (SpecialInstrumentationPoint) obj;
		if (this.id != other.id) {
			return false;
		}
		if (this.sensorId != other.sensorId) {
			return false;
		}
		return true;
	}

}
