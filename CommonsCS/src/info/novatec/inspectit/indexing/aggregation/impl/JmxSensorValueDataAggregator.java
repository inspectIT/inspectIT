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

}
