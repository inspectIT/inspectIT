package rocks.inspectit.agent.java.config.impl;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;

/**
 * Class needed to temporarily save information about a MBean.
 *
 * @author Alfred Krauss
 *
 */
public class UnregisteredJmxConfig {

	/**
	 * String passed on from the ConfigurationReader containing the ObjectName defined in the
	 * config-file.
	 */
	private String passedObjectNameExpression = "";

	/**
	 * String passed on from the ConfigurationReader containing the AttributeName defined in the
	 * config-file.
	 */
	private String passedAttributeNameExpression = "";

	/**
	 * The type-config of the jmx-sensor.
	 */
	private final JmxSensorTypeConfig jmxSensorTypeConfig;

	/**
	 * Constructor.
	 *
	 * @param jmxSensorTypeConfig
	 *            JmxSensorTypeConfig for this JmxConfig.
	 * @param objectNameExpression
	 *            ObjectName passed on from the ConfigurationReader.
	 * @param attributeNameExpression
	 *            AttributeName passed on from the ConfigurationReader.
	 */
	public UnregisteredJmxConfig(JmxSensorTypeConfig jmxSensorTypeConfig, String objectNameExpression, String attributeNameExpression) {
		this.jmxSensorTypeConfig = jmxSensorTypeConfig;
		this.passedObjectNameExpression = objectNameExpression;
		this.passedAttributeNameExpression = attributeNameExpression;
	}

	/**
	 * Gets {@link #passedObjectNameExpression}.
	 *
	 * @return {@link #passedObjectNameExpression}
	 */
	public String getPassedObjectNameExpression() {
		return passedObjectNameExpression;
	}

	/**
	 * Gets {@link #passedAttributeNameExpression}.
	 *
	 * @return {@link #passedAttributeNameExpression}
	 */
	public String getPassedAttributeNameExpression() {
		return passedAttributeNameExpression;
	}

	/**
	 * Gets {@link #jmxSensorTypeConfig}.
	 *
	 * @return {@link #jmxSensorTypeConfig}
	 */
	public JmxSensorTypeConfig getJmxSensorTypeConfig() {
		return jmxSensorTypeConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jmxSensorTypeConfig == null) ? 0 : jmxSensorTypeConfig.hashCode());
		result = prime * result + ((passedAttributeNameExpression == null) ? 0 : passedAttributeNameExpression.hashCode());
		result = prime * result + ((passedObjectNameExpression == null) ? 0 : passedObjectNameExpression.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		UnregisteredJmxConfig other = (UnregisteredJmxConfig) obj;
		if (jmxSensorTypeConfig == null) {
			if (other.jmxSensorTypeConfig != null) {
				return false;
			}
		} else if (!jmxSensorTypeConfig.equals(other.jmxSensorTypeConfig)) {
			return false;
		}

		if (!StringUtils.equals(passedAttributeNameExpression, other.getPassedAttributeNameExpression())) {
			return false;
		}
		if (!StringUtils.equals(passedObjectNameExpression, other.getPassedObjectNameExpression())) {
			return false;
		}

		return true;
	}

}
