package rocks.inspectit.shared.cs.cmr.service;

import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * Service for providing general timer data objects.
 * 
 * @author Ivan Senic
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ITimerDataAccessService {

	/**
	 * Returns a list of the timer data for a given template. In this template, only the platform id
	 * is extracted.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @return The list of the timer data object.
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
