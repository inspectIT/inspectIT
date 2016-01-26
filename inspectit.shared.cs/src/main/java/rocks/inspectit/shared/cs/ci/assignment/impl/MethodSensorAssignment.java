package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;

/**
 * Class for method sensor assignment.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ TimerMethodSensorAssignment.class })
@XmlRootElement(name = "method-sensor-assignment")
public class MethodSensorAssignment extends AbstractClassSensorAssignment<IMethodSensorConfig> {

	/**
	 * Sensor config class.
	 */
	@XmlAttribute(name = "sensor-config-class", required = true)
	private Class<? extends IMethodSensorConfig> sensorConfig;

	/**
	 * Method name/pattern.
	 */
	@XmlAttribute(name = "method-name")
	private String methodName;

	/**
	 * Parameters.
	 */
	@XmlAttribute(name = "parameters")
	private List<String> parameters;

	/**
	 * If is constructor.
	 */
	@XmlAttribute(name = "constructor")
	private boolean constructor;

	/**
	 * If public methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "public-mod")
	private boolean publicModifier = true;

	/**
	 * If protected methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "protected-mod")
	private boolean protectedModifier = true;

	/**
	 * If private methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "private-mod")
	private boolean privateModifier = true;

	/**
	 * If default methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "default-mod")
	private boolean defaultModifier = true;

	/**
	 * No-args constructor.
	 */
	public MethodSensorAssignment() {
	}

	/**
	 * Default constructor.
	 *
	 * @param sensorConfig
	 *            Method sensor config class begin assigned.
	 */
	public MethodSensorAssignment(Class<? extends IMethodSensorConfig> sensorConfig) {
		this.sensorConfig = sensorConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends IMethodSensorConfig> getSensorConfigClass() {
		return sensorConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getSettings() {
		return Collections.emptyMap();
	}

	/**
	 * Gets {@link #methodName}.
	 *
	 * @return {@link #methodName}
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Sets {@link #methodName}.
	 *
	 * @param methodName
	 *            New value for {@link #methodName}
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Gets {@link #parameters}.
	 *
	 * @return {@link #parameters}
	 */
	public List<String> getParameters() {
		return parameters;
	}

	/**
	 * Sets {@link #parameters}.
	 *
	 * @param parameters
	 *            New value for {@link #parameters}
	 */
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets {@link #constructor}.
	 *
	 * @return {@link #constructor}
	 */
	public boolean isConstructor() {
		return constructor;
	}

	/**
	 * Sets {@link #constructor}.
	 *
	 * @param constructor
	 *            New value for {@link #constructor}
	 */
	public void setConstructor(boolean constructor) {
		this.constructor = constructor;
	}

	/**
	 * Gets {@link #publicModifier}.
	 *
	 * @return {@link #publicModifier}
	 */
	public boolean isPublicModifier() {
		return publicModifier;
	}

	/**
	 * Sets {@link #publicModifier}.
	 *
	 * @param publicModifier
	 *            New value for {@link #publicModifier}
	 */
	public void setPublicModifier(boolean publicModifier) {
		this.publicModifier = publicModifier;
	}

	/**
	 * Gets {@link #protectedModifier}.
	 *
	 * @return {@link #protectedModifier}
	 */
	public boolean isProtectedModifier() {
		return protectedModifier;
	}

	/**
	 * Sets {@link #protectedModifier}.
	 *
	 * @param protectedModifier
	 *            New value for {@link #protectedModifier}
	 */
	public void setProtectedModifier(boolean protectedModifier) {
		this.protectedModifier = protectedModifier;
	}

	/**
	 * Gets {@link #privateModifier}.
	 *
	 * @return {@link #privateModifier}
	 */
	public boolean isPrivateModifier() {
		return privateModifier;
	}

	/**
	 * Sets {@link #privateModifier}.
	 *
	 * @param privateModifier
	 *            New value for {@link #privateModifier}
	 */
	public void setPrivateModifier(boolean privateModifier) {
		this.privateModifier = privateModifier;
	}

	/**
	 * Gets {@link #defaultModifier}.
	 *
	 * @return {@link #defaultModifier}
	 */
	public boolean isDefaultModifier() {
		return defaultModifier;
	}

	/**
	 * Sets {@link #defaultModifier}.
	 *
	 * @param defaultModifier
	 *            New value for {@link #defaultModifier}
	 */
	public void setDefaultModifier(boolean defaultModifier) {
		this.defaultModifier = defaultModifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (constructor ? 1231 : 1237);
		result = prime * result + (defaultModifier ? 1231 : 1237);
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + (privateModifier ? 1231 : 1237);
		result = prime * result + (protectedModifier ? 1231 : 1237);
		result = prime * result + (publicModifier ? 1231 : 1237);
		result = prime * result + ((sensorConfig == null) ? 0 : sensorConfig.getName().hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MethodSensorAssignment other = (MethodSensorAssignment) obj;
		if (constructor != other.constructor) {
			return false;
		}
		if (defaultModifier != other.defaultModifier) {
			return false;
		}
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (privateModifier != other.privateModifier) {
			return false;
		}
		if (protectedModifier != other.protectedModifier) {
			return false;
		}
		if (publicModifier != other.publicModifier) {
			return false;
		}
		if (sensorConfig.getName() == null) {
			if (other.sensorConfig.getName() != null) {
				return false;
			}
		} else if (!sensorConfig.getName().equals(other.sensorConfig.getName())) {
			return false;
		}
		return true;
	}

}
