package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.TimerData;

import java.util.Date;
import java.util.List;

/**
 * The DAO for timer data objects.
 * 
 * @author Ivan Senic
 * 
 */
public interface TimerDataDao {

	/**
	 * Returns a list of the aggregated timer data for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @return The list of the aggregated timer data object.
	 */
	List<TimerData> getAggregatedTimerData(TimerData timerData);

	/**
	 * Returns a list of the timer data for a given template for a time frame. In this template,
	 * only the platform id is extracted.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the timer data object.
	 */
	List<TimerData> getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate);
}
