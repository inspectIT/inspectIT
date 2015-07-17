package info.novatec.inspectit.cmr.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Method Sensor Type Ident class is used to store the sensor types which are used for methods
 * and basically called when the respective method is called.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodSensorTypeIdent extends SensorTypeIdent {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8933452676894686230L;

	/**
	 * The one-to-many association to the {@link MethodIdentToSensorType} objects.
	 */
	private Set<MethodIdentToSensorType> methodIdentToSensorTypes = new HashSet<MethodIdentToSensorType>(0);

	/**
	 * Settings of the sensor on the agent.
	 */
	private Map<String, Object> settings;

	/**
	 * Gets {@link #methodIdentToSensorTypes}.
	 * 
	 * @return {@link #methodIdentToSensorTypes}
	 */
	public Set<MethodIdentToSensorType> getMethodIdentToSensorTypes() {
		return methodIdentToSensorTypes;
	}

	/**
	 * Sets {@link #methodIdentToSensorTypes}.
	 * 
	 * @param methodIdentToSensorTypes
	 *            New value for {@link #methodIdentToSensorTypes}
	 */
	public void setMethodIdentToSensorTypes(Set<MethodIdentToSensorType> methodIdentToSensorTypes) {
		this.methodIdentToSensorTypes = methodIdentToSensorTypes;
	}

	/**
	 * Gets {@link #settings}.
	 * 
	 * @return {@link #settings}
	 */
	public Map<String, Object> getSettings() {
		return settings;
	}

	/**
	 * Sets {@link #settings}.
	 * 
	 * @param settings
	 *            New value for {@link #settings}
	 */
	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}

}
