package info.novatec.inspectit.agent.sensor;

import java.util.Map;

/**
 * This interface is used by all sensor which are collecting any kind of data. The
 * {@link #init(Map)} method is used to initialize the sensor with some additional preferences if
 * available.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface ISensor {

	/**
	 * Initialize the sensor.
	 * 
	 * @param parameter
	 *            Some additional parameters.
	 */
	void init(Map<String, Object> parameter);

}
