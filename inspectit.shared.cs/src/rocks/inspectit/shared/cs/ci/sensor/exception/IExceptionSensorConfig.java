package info.novatec.inspectit.ci.sensor.exception;

import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;

/**
 * Interface for exception sensor configurations.
 * 
 * @author Ivan Senic
 * 
 */
public interface IExceptionSensorConfig extends IMethodSensorConfig {

	/**
	 * Defines if exception sensor is enhanced.
	 * 
	 * @return Returns <code>true</code> if exception sensor is in enhanced mode.
	 */
	boolean isEnhanced();
}
