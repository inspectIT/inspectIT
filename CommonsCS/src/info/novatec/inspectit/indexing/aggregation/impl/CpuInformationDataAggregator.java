package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * {@link IAggregator} for the {@link CpuInformationData}.
 * 
 * @author Ivan Senic
 * 
 */
public class CpuInformationDataAggregator implements IAggregator<CpuInformationData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3054915347660161402L;

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<CpuInformationData> aggregatedObject, CpuInformationData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public CpuInformationData getClone(CpuInformationData cpuInformationData) {
		CpuInformationData clone = new CpuInformationData();
		clone.setPlatformIdent(cpuInformationData.getPlatformIdent());
		clone.setSensorTypeIdent(cpuInformationData.getSensorTypeIdent());
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
	public Object getAggregationKey(CpuInformationData object) {
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
