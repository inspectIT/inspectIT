package rocks.inspectit.shared.cs.cmr.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;

/**
 * Service to access the HttpTimerData.
 *
 * @author Stefan Siegl
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IHttpTimerDataAccessService {

	/**
	 * Returns a list of the http timer data for a given template. In this template, only the
	 * platform id is extracted.
	 *
	 * @param timerData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 * @return The list of the timer data object.
	 */
	List<HttpTimerData> getAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod);

	/**
	 * Returns a list of the http timer data for a given template. In this template, only the
	 * platform id is extracted.
	 *
	 * @param timerData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the timer data object.
	 */
	List<HttpTimerData> getAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod, Date fromDate, Date toDate);

	/**
	 * Returns a list of http timer data that is aggregated the value of the given http request
	 * parameter. For this purpose the <code>uri</code> field of the http timer data is re-used to
	 * store this value.
	 *
	 * @param timerData
	 *            the template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 *
	 * @return The list of the timer data objects that are aggregated by the tagged value.
	 */
	List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod);

	/**
	 * Returns a list of http timer data that is aggregated the value of the given http request
	 * parameter. For this purpose the <code>uri</code> field of the http timer data is re-used to
	 * store this value.
	 *
	 * @param timerData
	 *            the template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 *
	 * @return The list of the timer data objects that are aggregated by the tagged value.
	 */
	List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod, Date fromDate, Date toDate);

	/**
	 * Returns the {@link HttpTimerData} list that can be used as the input for the plotting. From
	 * the template list the platform ident will be used as well as all URI and tagged values.
	 *
	 * @param templates
	 *            Templates.
	 * @param fromDate
	 *            From date.
	 * @param toDate
	 *            To date
	 * @param retrieveByTag
	 *            If tag values from the templates should be used when retrieving the data. If false
	 *            is passed, URi will be used from templates.
	 * @return List of {@link HttpTimerData}.
	 */
	List<HttpTimerData> getChartingHttpTimerDataFromDateToDate(Collection<HttpTimerData> templates, Date fromDate, Date toDate, boolean retrieveByTag);
}
