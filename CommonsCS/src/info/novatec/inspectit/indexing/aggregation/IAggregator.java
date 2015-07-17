package info.novatec.inspectit.indexing.aggregation;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IAggregatedData;

/**
 * Interface that defines the operations needed to do a aggregation on the objects. This interface
 * can be used with queries to provide simpler aggregation possibilities.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of data that is aggregated.
 */
public interface IAggregator<E extends DefaultData> {

	/**
	 * Performs the aggregation. The aggregation should be done in the aggregatedObject, and
	 * objectToAdd should not be changed.
	 * 
	 * @param aggregatedObject
	 *            Object to hold aggregated values.
	 * @param objectToAdd
	 *            Object which values are added to the other object.
	 */
	void aggregate(IAggregatedData<E> aggregatedObject, E objectToAdd);

	/**
	 * Provides cloned object if the {@link #isCloning()} returns true.
	 * 
	 * @param object
	 *            Object to be cloned.
	 * @return Provides cloned object if the {@link #isCloning()} returns true.
	 */
	IAggregatedData<E> getClone(E object);

	/**
	 * Returns aggregation key.
	 * 
	 * @param object
	 *            Object to get key for.
	 * @return Aggregation key.
	 */
	Object getAggregationKey(E object);

}