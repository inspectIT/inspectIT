package rocks.inspectit.shared.all.instrumentation.config.impl;

import org.apache.commons.collections.MapUtils;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;

/**
 * Container for the values of a sensor type configuration. stores all the values defined in a
 * config file for later access.
 * 
 * @author Patrice Bouillet
 */
public class MethodSensorTypeConfig extends AbstractSensorTypeConfig {

	/**
	 * The name of the sensor type.
	 */
	private String name;

	/**
	 * The priority of this sensor type. The default is NORMAL.
	 */
	private PriorityEnum priority = PriorityEnum.NORMAL;

	/**
	 * Returns the unique name of the sensor type.
	 * 
	 * @return The name of the sensor type.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the unique name of the sensor type.
	 * 
	 * @param name
	 *            The sensor name.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Returns the priority of this sensor type. Important for time or memory sensors. Can return
	 * one of:<br>
	 * {@link PriorityEnum#MAX}<br>
	 * {@link PriorityEnum#HIGH}<br>
	 * {@link PriorityEnum#NORMAL}<br>
	 * {@link PriorityEnum#LOW}<br>
	 * {@link PriorityEnum#MIN}<br>
	 * 
	 * @return The priority of the sensor type.
	 */
	public PriorityEnum getPriority() {
		return priority;
	}

	/**
	 * Sets the priority of this sensor type.
	 * 
	 * @param priority
	 *            The priority.
	 */
	public void setPriority(PriorityEnum priority) {
		this.priority = priority;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getId() + " :: name: " + name + " (" + priority + ")";
	}

	/**
	 * Returns if jRebel property is activated on the sensor.
	 * 
	 * @return Returns if jRebel property is activated on the sensor.
	 */
	public boolean isJRebelActive() {
		if (MapUtils.isNotEmpty(getParameters())) {
			Object jRebelValue = getParameters().get("jRebel");
			if ("true".equals(jRebelValue)) {
				return true;
			}
		}
		return false;
	}

}
