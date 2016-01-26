package rocks.inspectit.shared.all.instrumentation.config.impl;

import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import rocks.inspectit.shared.all.instrumentation.asm.ConstructorInstrumenter;
import rocks.inspectit.shared.all.instrumentation.asm.MethodInstrumenter;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;

/**
 * Registered sensor config used with the server-side instrumentation.
 *
 * @author Ivan Senic
 *
 */
public class SensorInstrumentationPoint implements IMethodInstrumentationPoint {

	/**
	 * The method id.
	 */
	private long id;

	/**
	 * List of sensor ids to run on the method.
	 */
	private long[] sensorIds = new long[0];

	/**
	 * Backing array for figuring the priority order of the sensor ids. Bytes are enough to store
	 * values till 128 so we are safe here.
	 *
	 * @see #addSensorId(long, PriorityEnum)
	 */
	private transient byte[] sensorPriorities = new byte[0];

	/**
	 * If it's constructor to be instrumented.
	 */
	private boolean constructor;

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
	 * {@inheritDoc}
	 */
	public MethodVisitor getMethodVisitor(MethodVisitor superMethodVisitor, int access, String name, String desc, boolean enhancedExceptionSensor) {
		if (constructor) {
			return new ConstructorInstrumenter(superMethodVisitor, access, name, desc, id, enhancedExceptionSensor);
		} else {
			return new MethodInstrumenter(superMethodVisitor, access, name, desc, id, enhancedExceptionSensor);
		}
	}

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
	 * {@inheritDoc}
	 */
	public long[] getSensorIds() {
		return sensorIds;
	}

	/**
	 * Adds sensor Id if one does not exists already and properly sorts the id in the
	 * {@link #sensorIds} array based on the priority.
	 *
	 * @param sensorId
	 *            id to add
	 * @param priorityEnum
	 *            {@link PriorityEnum} of the sensor.
	 * @return true if sensor id has been added, false otherwise
	 */
	public boolean addSensorId(long sensorId, PriorityEnum priorityEnum) {
		// don't add existing ones
		if (containsSensorId(sensorId)) {
			return false;
		}

		// check insert index by priority
		// we want sensor with highest priority to be first, thus we need to negate the ordinal
		// add in addition -1 to avoid having negative zero
		byte priority = (byte) (-1 - priorityEnum.ordinal());
		int index = Math.abs(Arrays.binarySearch(sensorPriorities, priority) + 1);

		// update both arrays
		int length = sensorIds.length;

		long[] updateIds = new long[length + 1];
		System.arraycopy(sensorIds, 0, updateIds, 0, index);
		System.arraycopy(sensorIds, index, updateIds, index + 1, length - index);
		updateIds[index] = sensorId;

		byte[] updatePriority = new byte[length + 1];
		System.arraycopy(sensorPriorities, 0, updatePriority, 0, index);
		System.arraycopy(sensorPriorities, index, updatePriority, index + 1, length - index);
		updatePriority[index] = priority;

		sensorIds = updateIds;
		sensorPriorities = updatePriority;

		return true;
	}

	/**
	 * If sensor if is contained in this {@link SensorInstrumentationPoint}.
	 *
	 * @param sensorId
	 *            sensor id to check
	 * @return <code>true</code> if given sensor id is contained in the
	 *         {@link SensorInstrumentationPoint}
	 */
	public boolean containsSensorId(long sensorId) {
		return ArrayUtils.contains(sensorIds, sensorId);
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

}
