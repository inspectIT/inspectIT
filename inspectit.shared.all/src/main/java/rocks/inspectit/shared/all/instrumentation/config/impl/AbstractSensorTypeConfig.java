package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract sensor type configuration class which is used by the {@link MethodSensorTypeConfig} and
 * the {@link PlatformSensorTypeConfig}.
 *
 * @author Patrice Bouillet
 * @see MethodSensorTypeConfig
 * @see PlatformSensorTypeConfig
 *
 */
public abstract class AbstractSensorTypeConfig {

	/**
	 * The hash value of this sensor type.
	 */
	private long id = -1;

	/**
	 * The name of the class.
	 */
	private String className;

	/**
	 * Some additional parameters.
	 */
	private Map<String, Object> parameters = new HashMap<String, Object>();

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set the id of this sensor type.
	 *
	 * @param id
	 *            The id to set.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the class name of the sensor type as fully qualified.
	 *
	 * @return The class name.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * The class name has to be stored as fully qualified, example: <code>java.lang.String</code>.
	 *
	 * @param className
	 *            The class name.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Returns a {@link Map} of optional parameters. Is never null, but the size of the map could be
	 * 0.
	 *
	 * @return A map of parameters.
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * The {@link Map} of parameters stores additional information about the sensor type. Key and
	 * value should be both Strings.
	 *
	 * @param parameters
	 *            The parameters.
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

}
