package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.MapUtils;

import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;

/**
 * Method sensor assignment for all the sensors (at the moment http and timer) that support charting
 * option.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "charting-method-sensor-assignment")
public class ChartingMethodSensorAssignment extends MethodSensorAssignment {

	/**
	 * If it is charting.
	 */
	@XmlAttribute(name = "charting")
	private Boolean charting = Boolean.FALSE;

	/**
	 * No arg-constructor.
	 */
	public ChartingMethodSensorAssignment() {
	}

	/**
	 * Default constructor.
	 *
	 * @param sensorConfig
	 *            Method sensor config class begin assigned.
	 */
	public ChartingMethodSensorAssignment(Class<? extends IMethodSensorConfig> sensorConfig) {
		super(sensorConfig);

		// currently only supported by Timer & Http sensor
		if (!(Objects.equals(TimerSensorConfig.class, sensorConfig) || Objects.equals(HttpSensorConfig.class, sensorConfig))) {
			throw new IllegalArgumentException("Charting method sensor assignment is only applicable to the Timer and HTTP sensor.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getSettings() {
		Map<String, Object> settings = super.getSettings();
		if (MapUtils.isEmpty(settings)) {
			settings = new HashMap<>();
		}

		// charting
		if (charting) {
			settings.put("charting", Boolean.TRUE);
		}

		return settings;
	}

	/**
	 * Gets {@link #charting}.
	 *
	 * @return {@link #charting}
	 */
	public boolean isCharting() {
		return charting.booleanValue();
	}

	/**
	 * Sets {@link #charting}.
	 *
	 * @param charting
	 *            New value for {@link #charting}
	 */
	public void setCharting(boolean charting) {
		this.charting = Boolean.valueOf(charting);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.charting == null) ? 0 : this.charting.hashCode());
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
		ChartingMethodSensorAssignment other = (ChartingMethodSensorAssignment) obj;
		if (this.charting == null) {
			if (other.charting != null) {
				return false;
			}
		} else if (!this.charting.equals(other.charting)) {
			return false;
		}
		return true;
	}

}
