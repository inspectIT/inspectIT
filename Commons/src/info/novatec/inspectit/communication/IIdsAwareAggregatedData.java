package info.novatec.inspectit.communication;

import java.util.Collection;

/**
 * Extension of the {@link IAggregatedData} interface for classes that keep information on object
 * IDs that were aggregated.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of data.
 */
public interface IIdsAwareAggregatedData<E extends DefaultData> extends IAggregatedData<E> {

	/**
	 * Returns the collection that contains the IDs of the originally aggregated data.
	 * 
	 * @return Returns the collection that contains the IDs of the originally aggregated data.
	 */
	Collection<Long> getAggregatedIds();

	/**
	 * Clears the aggregated IDs that were collected during the aggregation.
	 */
	void clearAggregatedIds();
}
