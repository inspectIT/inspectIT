package rocks.inspectit.shared.cs.ci.assignment.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.jaxb.DefaultValue.BooleanFalse;
import rocks.inspectit.shared.cs.jaxb.DefaultValue.BooleanTrue;

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
	@XmlJavaTypeAdapter(BooleanFalse.class)
	private Boolean constructor = Boolean.FALSE;

	/**
	 * If public methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "public-mod")
	@XmlJavaTypeAdapter(BooleanTrue.class)
	private Boolean publicModifier = Boolean.TRUE;

	/**
	 * If protected methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "protected-mod")
	@XmlJavaTypeAdapter(BooleanTrue.class)
	private Boolean protectedModifier = Boolean.TRUE;

	/**
	 * If private methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "private-mod")
	@XmlJavaTypeAdapter(BooleanTrue.class)
	private Boolean privateModifier = Boolean.TRUE;

	/**
	 * If default methods should be included. By default <code>true</code>.
	 */
	@XmlAttribute(name = "default-mod")
	@XmlJavaTypeAdapter(BooleanTrue.class)
	private Boolean defaultModifier = Boolean.TRUE;

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
		return constructor.booleanValue();
	}

	/**
	 * Sets {@link #constructor}.
	 *
	 * @param constructor
	 *            New value for {@link #constructor}
	 */
	public void setConstructor(boolean constructor) {
		this.constructor = Boolean.valueOf(constructor);
	}

	/**
	 * Gets {@link #publicModifier}.
	 *
	 * @return {@link #publicModifier}
	 */
	public boolean isPublicModifier() {
		return publicModifier.booleanValue();
	}

	/**
	 * Sets {@link #publicModifier}.
	 *
	 * @param publicModifier
	 *            New value for {@link #publicModifier}
	 */
	public void setPublicModifier(boolean publicModifier) {
		this.publicModifier = Boolean.valueOf(publicModifier);
	}

	/**
	 * Gets {@link #protectedModifier}.
	 *
	 * @return {@link #protectedModifier}
	 */
	public boolean isProtectedModifier() {
		return protectedModifier.booleanValue();
	}

	/**
	 * Sets {@link #protectedModifier}.
	 *
	 * @param protectedModifier
	 *            New value for {@link #protectedModifier}
	 */
	public void setProtectedModifier(boolean protectedModifier) {
		this.protectedModifier = Boolean.valueOf(protectedModifier);
	}

	/**
	 * Gets {@link #privateModifier}.
	 *
	 * @return {@link #privateModifier}
	 */
	public boolean isPrivateModifier() {
		return privateModifier.booleanValue();
	}

	/**
	 * Sets {@link #privateModifier}.
	 *
	 * @param privateModifier
	 *            New value for {@link #privateModifier}
	 */
	public void setPrivateModifier(boolean privateModifier) {
		this.privateModifier = Boolean.valueOf(privateModifier);
	}

	/**
	 * Gets {@link #defaultModifier}.
	 *
	 * @return {@link #defaultModifier}
	 */
	public boolean isDefaultModifier() {
		return defaultModifier.booleanValue();
	}

	/**
	 * Sets {@link #defaultModifier}.
	 *
	 * @param defaultModifier
	 *            New value for {@link #defaultModifier}
	 */
	public void setDefaultModifier(boolean defaultModifier) {
		this.defaultModifier = Boolean.valueOf(defaultModifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.constructor == null) ? 0 : this.constructor.hashCode());
		result = (prime * result) + ((this.defaultModifier == null) ? 0 : this.defaultModifier.hashCode());
		result = (prime * result) + ((this.methodName == null) ? 0 : this.methodName.hashCode());
		result = (prime * result) + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		result = (prime * result) + ((this.privateModifier == null) ? 0 : this.privateModifier.hashCode());
		result = (prime * result) + ((this.protectedModifier == null) ? 0 : this.protectedModifier.hashCode());
		result = (prime * result) + ((this.publicModifier == null) ? 0 : this.publicModifier.hashCode());
		result = (prime * result) + ((this.sensorConfig == null) ? 0 : this.sensorConfig.getName().hashCode());
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
		if (this.constructor == null) {
			if (other.constructor != null) {
				return false;
			}
		} else if (!this.constructor.equals(other.constructor)) {
			return false;
		}
		if (this.defaultModifier == null) {
			if (other.defaultModifier != null) {
				return false;
			}
		} else if (!this.defaultModifier.equals(other.defaultModifier)) {
			return false;
		}
		if (this.methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!this.methodName.equals(other.methodName)) {
			return false;
		}
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		if (this.privateModifier == null) {
			if (other.privateModifier != null) {
				return false;
			}
		} else if (!this.privateModifier.equals(other.privateModifier)) {
			return false;
		}
		if (this.protectedModifier == null) {
			if (other.protectedModifier != null) {
				return false;
			}
		} else if (!this.protectedModifier.equals(other.protectedModifier)) {
			return false;
		}
		if (this.publicModifier == null) {
			if (other.publicModifier != null) {
				return false;
			}
		} else if (!this.publicModifier.equals(other.publicModifier)) {
			return false;
		}
		if (this.sensorConfig == null) {
			if (other.sensorConfig != null) {
				return false;
			}
		} else if (!this.sensorConfig.equals(other.sensorConfig)) {
			return false;
		}
		return true;
	}

}
