package info.novatec.inspectit.agent.config.impl;

/**
 * Only a marker class currently to define the platform sensor type configurations. They have no
 * additional information as already available in the {@link AbstractSensorTypeConfig}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlatformSensorTypeConfig extends AbstractSensorTypeConfig {

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getId() + " :: class: " + getClassName();
	}

}
