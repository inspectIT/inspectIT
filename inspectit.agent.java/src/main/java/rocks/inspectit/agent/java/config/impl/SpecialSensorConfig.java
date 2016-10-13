package rocks.inspectit.agent.java.config.impl;

import rocks.inspectit.agent.java.sensor.method.IMethodSensor;

/**
 * Special sensor config that relates to exactly one sensor holding the
 * {@link rocks.inspectit.agent.java.hooking.ISpecialHook}.
 *
 * @author Ivan Senic
 *
 */
public class SpecialSensorConfig extends AbstractSensorConfig {

	/**
	 * Sensor holding the {@link rocks.inspectit.agent.java.hooking.ISpecialHook}.
	 */
	private IMethodSensor sensor;

	/**
	 * Gets {@link #sensor}.
	 *
	 * @return {@link #sensor}
	 */
	public IMethodSensor getSensor() {
		return this.sensor;
	}

	/**
	 * Sets {@link #sensor}.
	 *
	 * @param sensor
	 *            New value for {@link #sensor}
	 */
	public void setSensor(IMethodSensor sensor) {
		this.sensor = sensor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.sensor == null) ? 0 : this.sensor.hashCode());
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
		SpecialSensorConfig other = (SpecialSensorConfig) obj;
		if (this.sensor == null) {
			if (other.sensor != null) {
				return false;
			}
		} else if (!this.sensor.equals(other.sensor)) {
			return false;
		}
		return true;
	}

}
