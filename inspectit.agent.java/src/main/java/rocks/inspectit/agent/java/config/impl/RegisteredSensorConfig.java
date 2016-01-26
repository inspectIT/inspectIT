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
public class RegisteredSensorConfig {

	/**
	 * The method id.
	 */
	private long id;

	/**
	 * The name of the target class.
	 */
	private String targetClassFqn;

	/**
	 * The name of the target method.
	 */
	private String targetMethodName;

	/**
	 * The return type of the method.
	 */
	private String returnType;

	/**
	 * The parameter types (as the fully qualified name) of the method.
	 */
	private List<String> parameterTypes;

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
	public long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(long id) {
		this.id = id;
	}


	/**
	 * Gets {@link #targetClassFqn}.
	 *
	 * @return {@link #targetClassFqn}
	 */
	public String getTargetClassFqn() {
		return targetClassFqn;
	}

	/**
	 * Sets {@link #targetClassFqn}.
	 *
	 * @param targetClassFqn
	 *            New value for {@link #targetClassFqn}
	 */
	public void setTargetClassFqn(String targetClassFqn) {
		this.targetClassFqn = targetClassFqn;
	}

	/**
	 * Gets {@link #targetMethodName}.
	 *
	 * @return {@link #targetMethodName}
	 */
	public String getTargetMethodName() {
		return targetMethodName;
	}

	/**
	 * Sets {@link #targetMethodName}.
	 *
	 * @param targetMethodName
	 *            New value for {@link #targetMethodName}
	 */
	public void setTargetMethodName(String targetMethodName) {
		this.targetMethodName = targetMethodName;
	}

	/**
	 * Gets {@link #returnType}.
	 *
	 * @return {@link #returnType}
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * Sets {@link #returnType}.
	 *
	 * @param returnType
	 *            New value for {@link #returnType}
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Gets {@link #parameterTypes}.
	 *
	 * @return {@link #parameterTypes}
	 */
	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Sets {@link #parameterTypes}.
	 *
	 * @param parameterTypes
	 *            New value for {@link #parameterTypes}
	 */
	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

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
}
