package rocks.inspectit.shared.cs.ci.sensor;

import java.util.Map;

/**
 * Sensor configuration.
 *
 * @author Ivan Senic
 *
 */
public interface ISensorConfig {

	/**
	 * Returns sensor name.
	 *
	 * @return Returns sensor name.
	 */
	String getName();

	/**
	 * Returns the class name of the sensor type as fully qualified.
	 *
	 * @return Returns the class name of the sensor type as fully qualified.
	 */
	String getClassName();

	/**
	 * The {@link Map} of parameters stores additional information about the sensor type.
	 *
	 * @return The {@link Map} of parameters stores additional information about the sensor type.
	 */
	Map<String, Object> getParameters();
}
