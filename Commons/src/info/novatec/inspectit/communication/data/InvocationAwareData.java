package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.MethodSensorData;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * This is an abstract class for all object that can be found in invocations and should be aware of
 * it.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
public abstract class InvocationAwareData extends MethodSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 1321146768671989693L;

	/**
	 * Map<Long, MutableInt> that contains the ID of invocation as a key and numbers of object
	 * appearances in this invocation.
	 */
	@Transient
	private Map<Long, MutableInt> invocationsParentsIdMap;

	/**
	 * Default no-args constructor.
	 */
	public InvocationAwareData() {
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param timeStamp
	 *            the timestamp.
	 * @param platformIdent
	 *            the platform identifier.
	 * @param sensorTypeIdent
	 *            the sensor type identifier.
	 * @param methodIdent
	 *            the method identifier.
	 */
	public InvocationAwareData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param timeStamp
	 *            the timestamp.
	 * @param platformIdent
	 *            the platform identifier.
	 * @param sensorTypeIdent
	 *            the sensor type identifier.
	 * @param methodIdent
	 *            the method identifier.
	 * @param parameterContentData
	 *            the parameter contents.
	 */
	public InvocationAwareData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List<ParameterContentData> parameterContentData) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);
	}

	/**
	 * Adds one invocation sequence data ID to the set of invocation IDs where this object is found.
	 * 
	 * @param id
	 *            Invocation id.
	 */
	public void addInvocationParentId(Long id) {
		if (null != id) {
			if (null == invocationsParentsIdMap) {
				invocationsParentsIdMap = new HashMap<Long, MutableInt>();
			}
			MutableInt count = (MutableInt) invocationsParentsIdMap.get(id);
			if (null != count) {
				count.increase();
			} else {
				invocationsParentsIdMap.put(id, new MutableInt(1));
			}
		}
	}

	/**
	 * Returns set of invocation parents IDS.
	 * 
	 * @return Returns set of invocation parents IDS.
	 */
	public Set<Long> getInvocationParentsIdSet() {
		if (null != invocationsParentsIdMap) {
			return invocationsParentsIdMap.keySet();
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Gets {@link #invocationsParentsIdMap}.
	 * 
	 * @return {@link #invocationsParentsIdMap}
	 */
	public Map<Long, MutableInt> getInvocationsParentsIdMap() {
		return invocationsParentsIdMap;
	}

	/**
	 * Sets {@link #invocationsParentsIdMap}.
	 * 
	 * @param invocationsParentsIdMap
	 *            New value for {@link #invocationsParentsIdMap}
	 */
	public void setInvocationsParentsIdMap(Map<Long, MutableInt> invocationsParentsIdMap) {
		this.invocationsParentsIdMap = invocationsParentsIdMap;
	}

	/**
	 * Returns how much objects are contained in the invocation parents.
	 * 
	 * @return Returns how much objects are contained in the invocation parents.
	 */
	public int getObjectsInInvocationsCount() {
		int count = 0;
		if (null != invocationsParentsIdMap) {
			for (MutableInt parentId : invocationsParentsIdMap.values()) {
				count += parentId.getValue();
			}
		}
		return count;
	}

	/**
	 * Aggregates the data correlated to the invocation parents. Note that this method has to be
	 * called from the subclasses when they implement any kind of aggregation.
	 * 
	 * @param invocationAwareData
	 *            Data to aggregate to current object.
	 */
	public void aggregateInvocationAwareData(InvocationAwareData invocationAwareData) {
		if (null != invocationAwareData.getInvocationsParentsIdMap()) {
			if (null == invocationsParentsIdMap) {
				invocationsParentsIdMap = new HashMap<Long, MutableInt>();
			}
			for (Map.Entry<Long, MutableInt> entry : invocationAwareData.getInvocationsParentsIdMap().entrySet()) {
				MutableInt count = invocationsParentsIdMap.get(entry.getKey());
				if (null != count) {
					count.add(entry.getValue().getValue());
				} else {
					invocationsParentsIdMap.put(entry.getKey(), new MutableInt(entry.getValue().getValue()));
				}
			}
		}
	}

	/**
	 * Returns the percentage of objects that are found in invocations as double.
	 * 
	 * @return Double ranging from 0 to 1.
	 */
	public abstract double getInvocationAffiliationPercentage();

	/**
	 * {@inheritDoc}
	 */
	public boolean isOnlyFoundInInvocations() {
		return getInvocationAffiliationPercentage() == 1d;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isOnlyFoundOutsideInvocations() {
		return getInvocationAffiliationPercentage() == 0d;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((invocationsParentsIdMap == null) ? 0 : invocationsParentsIdMap.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
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
		InvocationAwareData other = (InvocationAwareData) obj;
		if (invocationsParentsIdMap == null) {
			if (other.invocationsParentsIdMap != null) {
				return false;
			}
		} else if (!invocationsParentsIdMap.equals(other.invocationsParentsIdMap)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		if (null != invocationsParentsIdMap) {
			size += objectSizes.getSizeOfHashMap(invocationsParentsIdMap.size());
			size += invocationsParentsIdMap.size() * objectSizes.getSizeOfLongObject();
			long sizeOfMutableInt = objectSizes.alignTo8Bytes(objectSizes.getSizeOfObjectHeader() + objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 0, 0));
			size += invocationsParentsIdMap.size() * sizeOfMutableInt;
		}
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * Simple mutable integer class for internal purposes.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class MutableInt implements Serializable {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -2367937702260302863L;

		/**
		 * Value.
		 */
		private int value;

		/**
		 * No-arg constructor for serialization.
		 */
		public MutableInt() {
		}

		/**
		 * Constructor that sets initial value.
		 * 
		 * @param value
		 *            Initial value.
		 */
		public MutableInt(int value) {
			this.value = value;
		}

		/**
		 * @return the value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Increases the value.
		 */
		public void increase() {
			value++;
		}

		/**
		 * Adds delta to the value.
		 * 
		 * @param delta
		 *            Delta.
		 */
		public void add(int delta) {
			value += delta;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
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
			MutableInt other = (MutableInt) obj;
			if (value != other.value) {
				return false;
			}
			return true;
		}

	}
}
