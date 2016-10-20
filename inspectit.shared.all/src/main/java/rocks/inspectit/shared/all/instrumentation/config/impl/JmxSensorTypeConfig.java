package rocks.inspectit.shared.all.instrumentation.config.impl;

/**
 * Container for the values of a sensor type configuration. Stores all the values defined in a
 * configuration for later access.
 *
 * @author Alfred Krauss
 */
public class JmxSensorTypeConfig extends AbstractSensorTypeConfig {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getId() + " :: class: " + getClassName();
	}

}
