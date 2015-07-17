package info.novatec.inspectit.rcp.editor.preferences.control.samplingrate;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * The enumeration for sampling rate modes.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public enum SamplingRateMode implements ISamplingRateMode {
	/**
	 * The identifier of the sampling rate modes.
	 */
	TIMEFRAME_DIVIDER {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public <E extends DefaultData> List<E> adjustSamplingRate(List<E> defaultDataList, Date from, Date to, int samplingRate, IAggregator<E> aggregator) {
			long timeframe = 0;

			if (samplingRate > 0 && defaultDataList != null) {
				timeframe = (to.getTime() - from.getTime()) / samplingRate;
			} else {
				return defaultDataList;
			}

			List<E> resultList = new ArrayList<E>();

			// define the start and end position of the first time frame
			long timeframeStartTime = Long.MAX_VALUE;
			long timeframeEndTime = 0;
			int fromIndex = 0;
			int toIndex = -1;

			// find the starting value
			for (int i = 0; i < defaultDataList.size(); i++) {
				Date dataDate = defaultDataList.get(i).getTimeStamp();

				// find first data object which lies in the specified time range
				if ((dataDate.getTime() == from.getTime() || dataDate.after(from)) && dataDate.before(to)) {
					fromIndex = i;
					timeframeStartTime = dataDate.getTime() - timeframe / 2;
					timeframeEndTime = dataDate.getTime() + timeframe / 2;

					if (i - 1 >= 0) {
						// we add a data object so that the drawn graph does not start with the
						// first drawn object, but the line will go out of the graph.
						if (i - 1 > 0) {
							// this data object is not the first of the list, thus we add the very
							// first data object to the result list because of the auto range of
							// jfreechart. Otherwise the graph would not scale correctly.
							resultList.add(defaultDataList.get(0));
						}
						resultList.add(defaultDataList.get(i - 1));
					}

					break;
				}
			}

			AggregationPerformer<E> aggregationPerformer = new AggregationPerformer<E>(aggregator);
			// iterate over time frames
			while (timeframeStartTime < to.getTime() + timeframe) {
				long averageTime = (timeframeStartTime + timeframeEndTime) / 2;

				for (int i = fromIndex; i < defaultDataList.size(); i++) {
					long dataTime = defaultDataList.get(i).getTimeStamp().getTime();

					if (dataTime > timeframeEndTime) {
						// if the actual data object is not anymore in the actual time frame, then
						// the last data object was
						toIndex = i - 1;
						break;
					} else if (i + 1 == defaultDataList.size()) {
						// if end of list is reached then toIndex is the end of the list
						toIndex = i;
					}
				}

				// aggregate data objects only when toIndex changed
				if (toIndex >= 0 && fromIndex <= toIndex) {
					// aggregate data and set the average time stamp
					aggregationPerformer.reset();
					// aggregation performer does not include toIndex to the aggregation
					aggregationPerformer.processList(defaultDataList, fromIndex, toIndex + 1);
					List<E> aggregatedData = aggregationPerformer.getResultList();
					if (CollectionUtils.isNotEmpty(aggregatedData)) {
						E data = aggregatedData.get(0);
						data.setTimeStamp(new Timestamp(averageTime));
						resultList.add(data);
					}

					// set the fromIndex on the actual data object
					fromIndex = toIndex + 1;
				}

				// adjust timeframe
				timeframeStartTime = timeframeEndTime;
				timeframeEndTime += timeframe;
				// reset the toIndex
				toIndex = -1;
			}

			if (0 != fromIndex) {
				// we try to append an object at the right non-visible part of the graph so that a
				// line is drawn.
				if (defaultDataList.size() > fromIndex) {
					resultList.add(defaultDataList.get(fromIndex));
				}
				if (defaultDataList.size() > fromIndex + 1) {
					// there are some objects untouched, thus we need to add the very last data
					// object to the result list for the auto scaling of jfreechart to work.
					resultList.add(defaultDataList.get(defaultDataList.size() - 1));
				}
			}

			return resultList;
		}
	};

}
