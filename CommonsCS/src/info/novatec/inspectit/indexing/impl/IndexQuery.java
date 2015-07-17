package info.novatec.inspectit.indexing.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.restriction.IIndexQueryRestriction;
import info.novatec.inspectit.indexing.restriction.IIndexQueryRestrictionProcessor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * {@link IndexQuery} represent an object that is used in querying the tree structure of the buffer.
 * 
 * @author Ivan Senic
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class IndexQuery implements IIndexQuery {

	/**
	 * Processor that checks if the given restrictions that are set in the query are fulfilled for
	 * any object.
	 */
	@Autowired
	IIndexQueryRestrictionProcessor restrictionProcessor;

	/**
	 * Minimum id that returned objects should have.
	 */
	private long minId;

	/**
	 * Platform id.
	 */
	private long platformIdent;

	/**
	 * Sensor type id.
	 */
	private long sensorTypeIdent;

	/**
	 * Method id.
	 */
	private long methodIdent;

	/**
	 * Object class type.
	 */
	private List<Class<?>> objectClasses;

	/**
	 * From date.
	 */
	private Timestamp fromDate;

	/**
	 * Till date.
	 */
	private Timestamp toDate;

	/**
	 * List of restrictions for this query.
	 */
	private List<IIndexQueryRestriction> indexingRestrictionList = new ArrayList<IIndexQueryRestriction>();

	/**
	 * {@inheritDoc}
	 */
	public long getMinId() {
		return minId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMinId(long minId) {
		this.minId = minId;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPlatformIdent(long platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSensorTypeIdent() {
		return sensorTypeIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSensorTypeIdent(long sensorTypeIdent) {
		this.sensorTypeIdent = sensorTypeIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getMethodIdent() {
		return methodIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Class<?>> getObjectClasses() {
		return objectClasses;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setObjectClasses(List objectClasses) {
		this.objectClasses = objectClasses;
	}

	/**
	 * {@inheritDoc}
	 */
	public Timestamp getFromDate() {
		return fromDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFromDate(Timestamp fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Timestamp getToDate() {
		return toDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setToDate(Timestamp toDate) {
		this.toDate = toDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addIndexingRestriction(IIndexQueryRestriction indexingRestriction) {
		indexingRestrictionList.add(indexingRestriction);
	}

	/**
	 * @return the indexingRestrictionList
	 */
	protected List<IIndexQueryRestriction> getIndexingRestrictionList() {
		return indexingRestrictionList;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isIntervalSet() {
		if (null != fromDate) {
			if (null == toDate) {
				return true;
			} else {
				return fromDate.before(toDate);
			}
		} else if (null != toDate) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInInterval(Timestamp timestamp) {
		if (isIntervalSet()) {
			if (null == timestamp) {
				return false;
			} else {
				if (fromDate != null && fromDate.compareTo(timestamp) > 0) {
					return false;
				}
				if (toDate != null && toDate.compareTo(timestamp) < 0) {
					return false;
				}
				return true;
			}
		} else {
			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean areAllRestrictionsFulfilled(DefaultData defaultData) {
		return restrictionProcessor.areAllRestrictionsFulfilled(defaultData, indexingRestrictionList);
	}

	/**
	 * @param restrictionProcessor
	 *            the restrictionProcessor to set
	 */
	public void setRestrictionProcessor(IIndexQueryRestrictionProcessor restrictionProcessor) {
		this.restrictionProcessor = restrictionProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fromDate == null) ? 0 : fromDate.hashCode());
		result = prime * result + ((CollectionUtils.isEmpty(indexingRestrictionList)) ? 0 : indexingRestrictionList.hashCode());
		result = prime * result + (int) (methodIdent ^ (methodIdent >>> 32));
		result = prime * result + (int) (minId ^ (minId >>> 32));
		result = prime * result + (int) (platformIdent ^ (platformIdent >>> 32));
		result = prime * result + (int) (sensorTypeIdent ^ (sensorTypeIdent >>> 32));
		result = prime * result + ((toDate == null) ? 0 : toDate.hashCode());

		// manually create the hash code for object class, cause we can not depend on Class.hashCode
		if (CollectionUtils.isNotEmpty(objectClasses)) {
			for (Class<?> clazz : objectClasses) {
				result = prime * result + clazz.getName().hashCode();
			}
		}

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
		IndexQuery other = (IndexQuery) obj;
		if (fromDate == null) {
			if (other.fromDate != null) {
				return false;
			}
		} else if (!fromDate.equals(other.fromDate)) {
			return false;
		}
		if (indexingRestrictionList == null) {
			if (other.indexingRestrictionList != null) {
				return false;
			}
		} else if (!indexingRestrictionList.equals(other.indexingRestrictionList)) {
			return false;
		}
		if (methodIdent != other.methodIdent) {
			return false;
		}
		if (minId != other.minId) {
			return false;
		}
		if (objectClasses == null) {
			if (other.objectClasses != null) {
				return false;
			}
		} else if (!objectClasses.equals(other.objectClasses)) {
			return false;
		}
		if (platformIdent != other.platformIdent) {
			return false;
		}
		if (sensorTypeIdent != other.sensorTypeIdent) {
			return false;
		}
		if (toDate == null) {
			if (other.toDate != null) {
				return false;
			}
		} else if (!toDate.equals(other.toDate)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("minId", minId);
		toStringBuilder.append("platformIdent", platformIdent);
		toStringBuilder.append("sensorTypeIdent", sensorTypeIdent);
		toStringBuilder.append("methodIdent", methodIdent);
		toStringBuilder.append("objectClasses", objectClasses);
		toStringBuilder.append("fromDate", fromDate);
		toStringBuilder.append("toDate", toDate);
		toStringBuilder.append("indexingRestrictionList", indexingRestrictionList);
		return toStringBuilder.toString();
	}
}
