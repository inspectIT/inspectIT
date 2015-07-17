package info.novatec.inspectit.rcp.editor.preferences.control.samplingrate;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.util.Date;
import java.util.List;

/**
 * The interface for the sampling rate modes.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface ISamplingRateMode {

	/**
	 * Adjusts the sampling rate with the given sampling rate mode and returns a {@link List} with
	 * the aggregated {@link DefaultData} objects.
	 * 
	 * @param <E>
	 *            Type of element.
	 * @param defaultDataList
	 *            The {@link List} with {@link DefaultData} objects.
	 * @param from
	 *            The start time.
	 * @param to
	 *            The end time.
	 * @param samplingRate
	 *            The sampling rate.
	 * @param aggregator
	 *            {@link IAggregator} to be used.
	 * @return A {@link List} with the aggregated data.
	 */
	<E extends DefaultData> List<E> adjustSamplingRate(List<E> defaultDataList, Date from, Date to, int samplingRate, IAggregator<E> aggregator);
}
