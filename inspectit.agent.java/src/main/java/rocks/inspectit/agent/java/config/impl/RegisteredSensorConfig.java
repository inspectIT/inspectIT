package rocks.inspectit.agent.java.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;

/**
 * Registered sensor config used with the server-side instrumentation.
 *
 * @author Ivan Senic
 *
 */
public class RegisteredSensorConfig extends AbstractSensorConfig {

	/**
	 * If the invocation should be started.
	 */
	private boolean startsInvocation;

	/**
	 * Additional settings are stored in this map.
	 */
	private Map<String, Object> settings;

	/**
	 * If <code>propertyAccess</code> is set to true, then this list contains at least one element.
	 * The contents is of type {@link PropertyPathStart}.
	 */
	private List<PropertyPathStart> propertyAccessorList;

	/**
	 * Method sensor list.
	 */
	private final List<IMethodSensor> methodSensors = new ArrayList<IMethodSensor>(1);

	/**
	 * Method sensor list reverse.
	 */
	private final List<IMethodSensor> methodSensorsReverse = new ArrayList<IMethodSensor>(1);

	/**
	 * {@inheritDoc}
	 */
	public boolean isStartsInvocation() {
		return startsInvocation;
	}

	/**
	 * Sets {@link #startsInvocation}.
	 *
	 * @param startsInvocation
	 *            New value for {@link #startsInvocation}
	 */
	public void setStartsInvocation(boolean startsInvocation) {
		this.startsInvocation = startsInvocation;
	}

	/**
	 * {@inheritDoc}
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

	/**
	 * Adds all given settings to the settings map.
	 *
	 * @param settings
	 *            Map of settings to add.
	 */
	public void addSettings(Map<String, Object> settings) {
		if (null == this.settings) {
			this.settings = new HashMap<String, Object>(settings.size());
		}
		this.settings.putAll(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PropertyPathStart> getPropertyAccessorList() {
		return propertyAccessorList;
	}

	/**
	 * Sets {@link #propertyAccessorList}.
	 *
	 * @param propertyAccessorList
	 *            New value for {@link #propertyAccessorList}
	 */
	public void setPropertyAccessorList(List<PropertyPathStart> propertyAccessorList) {
		this.propertyAccessorList = propertyAccessorList;
	}

	/**
	 * Adds one {@link PropertyPathStart} to the list of the property acc list.
	 *
	 * @param propertyPathStart
	 *            {@link PropertyPathStart} to add.
	 */
	public void addPropertyAccessor(PropertyPathStart propertyPathStart) {
		if (null == this.propertyAccessorList) {
			this.propertyAccessorList = new ArrayList<PropertyPathStart>(1);
		}
		this.propertyAccessorList.add(propertyPathStart);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPropertyAccess() {
		return CollectionUtils.isNotEmpty(propertyAccessorList);
	}

	/**
	 * Gets {@link #methodSensors}.
	 *
	 * @return {@link #methodSensors}
	 */
	public List<IMethodSensor> getMethodSensors() {
		return methodSensors;
	}

	/**
	 * Gets {@link #methodSensorsReverse}.
	 *
	 * @return {@link #methodSensorsReverse}
	 */
	public List<IMethodSensor> getMethodSensorsReverse() {
		return methodSensorsReverse;
	}

	/**
	 * Adds the {@link IMethodSensor} as last to the {@link #methodSensors} and as first to the
	 * {@link #methodSensorsReverse}.
	 *
	 * @param methodSensor
	 *            {@link IMethodSensor} to add.
	 */
	public void addMethodSensor(IMethodSensor methodSensor) {
		methodSensors.add(methodSensor);
		methodSensorsReverse.add(0, methodSensor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.methodSensors == null) ? 0 : this.methodSensors.hashCode());
		result = (prime * result) + ((this.methodSensorsReverse == null) ? 0 : this.methodSensorsReverse.hashCode());
		result = (prime * result) + ((this.propertyAccessorList == null) ? 0 : this.propertyAccessorList.hashCode());
		result = (prime * result) + ((this.settings == null) ? 0 : this.settings.hashCode());
		result = (prime * result) + (this.startsInvocation ? 1231 : 1237);
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
		RegisteredSensorConfig other = (RegisteredSensorConfig) obj;
		if (this.methodSensors == null) {
			if (other.methodSensors != null) {
				return false;
			}
		} else if (!this.methodSensors.equals(other.methodSensors)) {
			return false;
		}
		if (this.methodSensorsReverse == null) {
			if (other.methodSensorsReverse != null) {
				return false;
			}
		} else if (!this.methodSensorsReverse.equals(other.methodSensorsReverse)) {
			return false;
		}
		if (this.propertyAccessorList == null) {
			if (other.propertyAccessorList != null) {
				return false;
			}
		} else if (!this.propertyAccessorList.equals(other.propertyAccessorList)) {
			return false;
		}
		if (this.settings == null) {
			if (other.settings != null) {
				return false;
			}
		} else if (!this.settings.equals(other.settings)) {
			return false;
		}
		if (this.startsInvocation != other.startsInvocation) {
			return false;
		}
		return true;
	}

}
