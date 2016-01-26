package rocks.inspectit.shared.cs.ci.sensor.method;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;

/**
 * Interface for the method sensor configurations.
 *
 * @author Ivan Senic
 *
 */
public interface IMethodSensorConfig extends ISensorConfig {

	/**
	 * Returns sensor name.
	 *
	 * @return Returns sensor name.
	 */
	String getName();

	/**
	 * Returns priority of the sensor.
	 *
	 * @return Returns priority of the sensor.
	 */
	PriorityEnum getPriority();

	/**
	 * Defines if sensor is "advanced". Sensor should be marked as advanced if the end-users would
	 * normally not make any assignments with it (thus it's not a standard sensor). Advanced sensors
	 * are by default hidden in the CI user interface and can be selected only if advanced sensors
	 * option is checked.
	 *
	 * @return Returns <code>true</code> if sensor is "advanced", false otherwise.
	 */
	boolean isAdvanced();

}
