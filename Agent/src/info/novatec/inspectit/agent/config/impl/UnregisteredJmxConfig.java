package info.novatec.inspectit.agent.config.impl;


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
	private JmxSensorTypeConfig jmxSensorTypeConfig;

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

}
