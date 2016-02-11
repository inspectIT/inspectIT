package rocks.inspectit.shared.cs.indexing.aggregation.impl;

import java.io.Serializable;

import rocks.inspectit.shared.all.communication.IAggregatedData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;

/**
 * {@link IAggregator} for {@link MemoryInformationData}.
 * 
 * @author Ivan Senic
 * 
 */
public class MemoryInformationDataAggregator implements IAggregator<MemoryInformationData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 1258455695092762583L;

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<MemoryInformationData> aggregatedObject, MemoryInformationData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public MemoryInformationData getClone(MemoryInformationData memoryInformationData) {
		MemoryInformationData clone = new MemoryInformationData();
		clone.setPlatformIdent(memoryInformationData.getPlatformIdent());
		clone.setSensorTypeIdent(memoryInformationData.getSensorTypeIdent());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCloning() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(MemoryInformationData object) {
		return object.getPlatformIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// we must make constant hashCode because of the caching
		result = prime * result + this.getClass().getName().hashCode();
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
		return true;
	}
}
