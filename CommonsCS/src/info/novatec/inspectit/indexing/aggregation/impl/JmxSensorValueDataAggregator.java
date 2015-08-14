package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * Aggregator for {@link JmxSensorValueData}.
 * 
 * @author Marius Oehler
 *
 */
public class JmxSensorValueDataAggregator implements IAggregator<JmxSensorValueData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4007439455022576127L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aggregate(IAggregatedData<JmxSensorValueData> aggregatedObject, JmxSensorValueData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JmxSensorValueData getClone(JmxSensorValueData object) {
		return new JmxSensorValueData(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAggregationKey(JmxSensorValueData object) {
		return object.getJmxSensorDefinitionDataIdentId();
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
