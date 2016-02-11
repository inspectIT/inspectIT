package rocks.inspectit.agent.java.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import rocks.inspectit.agent.java.config.impl.PropertyAccessor.PropertyPathStart;

/**
 * The abstract sensor configuration which is used by the registered and unregistered sensor
 * configuration classes.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractSensorConfig {

	/**
	 * The name of the target class.
	 */
	private String targetPackageName;

	/**
	 * The name of the target class.
	 */
	private String targetClassName;

	/**
	 * The name of the target method.
	 */
	private String targetMethodName;

	/**
	 * The parameter types (as the fully qualified name) of the method.
	 */
	private List<String> parameterTypes = new ArrayList<String>();

	/**
	 * Additional settings are stored in this map.
	 */
	private Map<String, Object> settings = new HashMap<String, Object>();

	/**
	 * Defines if this sensor configuration contains one or many definitions for a property access
	 * (class field / method parameter) to save.
	 */
	private boolean propertyAccess = false;

	/**
	 * If <code>propertyAccess</code> is set to true, then this list contains at least one element.
	 * The contents is of type {@link PropertyPathStart}.
	 */
	private List<PropertyPathStart> propertyAccessorList = new CopyOnWriteArrayList<PropertyPathStart>();

	/**
	 * If this config defines a constructor.
	 */
	private boolean constructor = false;

	/**
	 * Returns a map of the defined settings.
	 * 
	 * @return The map of settings.
	 */
	public Map<String, Object> getSettings() {
		return settings;
	}

	/**
	 * The map of settings. Both, key and value, should be standard strings.
	 * 
	 * @param settings
	 *            The map of settings.
	 */
	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}

	/**
	 * Returns the package name.
	 * 
	 * @return The package name.
	 */
	public String getTargetPackageName() {
		return targetPackageName;
	}

	/**
	 * Sets the package name.
	 * 
	 * @param targetPackageName
	 *            The package name to set.
	 */
	public void setTargetPackageName(String targetPackageName) {
		this.targetPackageName = targetPackageName;
	}

	/**
	 * Returns the class name as a name only string.
	 * 
	 * @return The class name. Example: String
	 */
	public String getTargetClassName() {
		return targetClassName;
	}

	/**
	 * Returns the fully qualified class name.
	 * 
	 * @return FQN of a class. Example: java.lang.String
	 */
	public String getQualifiedTargetClassName() {
		return targetPackageName + '.' + targetClassName;
	}

	/**
	 * Sets the class name. Has to be a fully qualified class name, example:
	 * <code>java.lang.String</code>
	 * 
	 * @param targetClassName
	 *            The target class name to set.
	 */
	public void setTargetClassName(String targetClassName) {
		this.targetClassName = targetClassName;
	}

	/**
	 * Returns the method name without the signature.
	 * 
	 * @return The method name.
	 */
	public String getTargetMethodName() {
		return targetMethodName;
	}

	/**
	 * Sets the method name. Has to be without the signature. So a defined method in the config file
	 * as <code>test(java.lang.String)</code> has to be extracted as just <code>test</code>.
	 * 
	 * @param targetMethodName
	 *            The method name.
	 */
	public void setTargetMethodName(String targetMethodName) {
		this.targetMethodName = targetMethodName;
	}

	/**
	 * The parameter types or the signature of the method. Returns a {@link List} of {@link String}
	 * instances containing the fully qualified name of the classes.
	 * 
	 * @return The {@link List} of parameter types.
	 */
	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Sets the parameter types. The {@link List} contains just of {@link String} instances.
	 * 
	 * @param parameterTypes
	 *            The {@link List} of parameter types.
	 */
	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	/**
	 * If this configuration defines a property access.
	 * 
	 * @return Returns <code>true</code> if a property access is defines.
	 */
	public boolean isPropertyAccess() {
		return propertyAccess;
	}

	/**
	 * Sets if this sensor configuration defines a property access.
	 * 
	 * @param propertyAccess
	 *            If this sensor configuration defines a property access.
	 */
	public void setPropertyAccess(boolean propertyAccess) {
		this.propertyAccess = propertyAccess;
	}

	/**
	 * Returns the {@link List} containing {@link PropertyPathStart} objects. Only contains
	 * something if {@link #isPropertyAccess()} returns <code>true</code>.
	 * 
	 * @return The {@link List} of {@link PropertyPathStart} objects.
	 */
	public List<PropertyPathStart> getPropertyAccessorList() {
		return propertyAccessorList;
	}

	/**
	 * If this sensor config defines a constructor.
	 * 
	 * @param isConstructor
	 *            the isConstructor to set
	 */
	public void setConstructor(boolean isConstructor) {
		this.constructor = isConstructor;
	}

	/**
	 * Is this sensor config defining an constructor?
	 * 
	 * @return the isConstructor
	 */
	public boolean isConstructor() {
		return constructor;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return targetClassName + "#" + targetMethodName + parameterTypes;
	}

}
