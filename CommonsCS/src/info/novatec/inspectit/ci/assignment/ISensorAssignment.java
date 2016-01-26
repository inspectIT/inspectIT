package info.novatec.inspectit.ci.assignment;

import info.novatec.inspectit.ci.sensor.ISensorConfig;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplierProvider;

import java.util.Map;

/**
 * Base interface for all sensor assignments.
 * 
 * @author Ivan Senic
 * 
 * @param <T>Type of the sensor config that relates to the assignment.
 */
public interface ISensorAssignment<T extends ISensorConfig> extends IInstrumentationApplierProvider {

	/**
	 * Returns the class of the sensor config.
	 * 
	 * @return Returns the class of the sensor config.
	 */
	Class<? extends T> getSensorConfigClass();

	/**
	 * Return settings for the sensor assignment.
	 * 
	 * @return Return settings for the sensor assignment.
	 */
	Map<String, Object> getSettings();

}
