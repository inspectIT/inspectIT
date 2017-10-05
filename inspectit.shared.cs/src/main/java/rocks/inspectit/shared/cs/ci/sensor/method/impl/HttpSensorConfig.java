package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.cs.ci.sensor.StringConstraintSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;

/**
 * HTTP sensor configuration.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "http-sensor-config")
public class HttpSensorConfig extends StringConstraintSensorConfig implements IMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "HTTP Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.http.HttpSensor";

	/**
	 * Session capture option.
	 */
	@XmlAttribute(name = "sessionCapture")
	private Boolean sessionCapture = Boolean.FALSE;

	/**
	 * Whether attributes should be captured.
	 */
	@XmlAttribute(name = "attributesCapture")
	private Boolean attributesCapture = Boolean.FALSE;

	/**
	 * Whether parameters should be captured.
	 */
	@XmlAttribute(name = "parametersCapture")
	private Boolean parametersCapture = Boolean.FALSE;

	/**
	 * No-args constructor.
	 */
	public HttpSensorConfig() {
		super(500);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return SENSOR_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PriorityEnum getPriority() {
		return PriorityEnum.MAX;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> parameters = super.getParameters();

		if (sessionCapture) {
			parameters.put("sessioncapture", "true");
		}
		if (attributesCapture) {
			parameters.put("attributescapture", "true");
		}
		if (parametersCapture) {
			parameters.put("parameterscapture", "true");
		}

		return parameters;
	}

	/**
	 * Gets {@link #sessionCapture}.
	 *
	 * @return {@link #sessionCapture}
	 */
	public boolean isSessionCapture() {
		return sessionCapture.booleanValue();
	}

	/**
	 * Sets {@link #sessionCapture}.
	 *
	 * @param sessionCapture
	 *            New value for {@link #sessionCapture}
	 */
	public void setSessionCapture(boolean sessionCapture) {
		this.sessionCapture = Boolean.valueOf(sessionCapture);
	}

	/**
	 * Gets {@link #attributesCapture}.
	 *
	 * @return {@link #attributesCapture}
	 */
	public Boolean isAttributesCapture() {
		return this.attributesCapture;
	}

	/**
	 * Sets {@link #attributesCapture}.
	 *
	 * @param attributesCapture
	 *            New value for {@link #attributesCapture}
	 */
	public void setAttributesCapture(Boolean attributesCapture) {
		this.attributesCapture = attributesCapture;
	}

	/**
	 * Gets {@link #parametersCapture}.
	 *
	 * @return {@link #parametersCapture}
	 */
	public Boolean isParametersCapture() {
		return this.parametersCapture;
	}

	/**
	 * Sets {@link #parametersCapture}.
	 *
	 * @param parametersCapture
	 *            New value for {@link #parametersCapture}
	 */
	public void setParametersCapture(Boolean parametersCapture) {
		this.parametersCapture = parametersCapture;
	}

}
