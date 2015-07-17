package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.HttpTimerData;

import java.util.Date;
import java.util.List;

/**
 * Provides Services to access <code>HttpTimerData</code> information.
 * 
 * @author Stefan Siegl
 */
public interface HttpTimerDataDao {

	/**
	 * Returns a list of the aggregated timer data for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param httpData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            use different request method information for building categorization pairs?
	 * @return The list of the aggregated timer data object.
	 */
	List<HttpTimerData> getAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod);

	/**
	 * Returns a list of the aggregated timer data for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param httpData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            use different request method information for building categorization pairs?
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the aggregated timer data object.
	 */
	List<HttpTimerData> getAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate);

	/**
	 * Returns a list of the tagged timer data (aggregated by the value of the inspectit header) for
	 * a given template. In this template, only the platform id is extracted.
	 * 
	 * @param httpData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            use different request method information for building categorization pairs?
	 * @return The list of the aggregated timer data object.
	 */
	List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod);

	/**
	 * Returns a list of the tagged timer data (aggregated by the value of the inspectit header) for
	 * a given template. In this template, only the platform id is extracted.
	 * 
	 * @param httpData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            use different request method information for building categorization pairs?
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the aggregated timer data object.
	 */
	List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate);
}
