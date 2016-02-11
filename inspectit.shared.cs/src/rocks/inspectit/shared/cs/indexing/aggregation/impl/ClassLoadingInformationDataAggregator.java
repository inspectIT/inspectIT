package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * {@link IAggregator} for the {@link ClassLoadingInformationData}.
 * 
 * @author Ivan Senic
 * 
 */
public class ClassLoadingInformationDataAggregator implements IAggregator<ClassLoadingInformationData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6012530906635644981L;

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<ClassLoadingInformationData> aggregatedObject, ClassLoadingInformationData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassLoadingInformationData getClone(ClassLoadingInformationData classLoadingInformationData) {
		ClassLoadingInformationData clone = new ClassLoadingInformationData();
		clone.setPlatformIdent(classLoadingInformationData.getPlatformIdent());
		clone.setSensorTypeIdent(classLoadingInformationData.getSensorTypeIdent());
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
	public Object getAggregationKey(ClassLoadingInformationData object) {
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
