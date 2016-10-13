package rocks.inspectit.shared.cs.ci.sensor.method.special;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.cs.ci.sensor.method.AbstractMethodSensorConfig;

/**
 * Abstract config class for all special sensors.
 *
 * @author Ivan Senic
 *
 */
@XmlTransient
public abstract class AbstractSpecialMethodSensorConfig extends AbstractMethodSensorConfig {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PriorityEnum getPriority() {
		return PriorityEnum.NORMAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return true;
	}

}
