package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * This layer is used to access the exception sensor information.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface ExceptionSensorDataDao {

	/**
	 * Returns a list of {@link ExceptionSensorData} objects. This list can be used to get an
	 * overview over recorded Exceptions in a target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @param limit
	 *            The limit/size of the list.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * @return List of {@link ExceptionSensorData} objects to get an overview of recorded
	 *         Exceptions.
	 */
	List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Comparator<? super ExceptionSensorData> comparator);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects which are between the from and to
	 * {@link Date} objects. This list can be used to get an overview over recorded Exceptions in a
	 * target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @param limit
	 *            The limit/size of the list.
	 * @param fromDate
	 *            The start date.
	 * @param toDate
	 *            The end date.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * @return List of {@link ExceptionSensorData} objects to get an overview of recorded
	 *         Exceptions.
	 */
	List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate, Comparator<? super ExceptionSensorData> comparator);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects. This list can be used to get an
	 * overview over recorded Exceptions in a target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * @return List of {@link ExceptionSensorData} objects to get an overview of recorded
	 *         Exceptions.
	 */
	List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Comparator<? super ExceptionSensorData> comparator);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects which are between the from and to
	 * {@link Date} objects. This list can be used to get an overview over recorded Exceptions in a
	 * target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @param fromDate
	 *            The start date.
	 * @param toDate
	 *            The end date.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * @return List of {@link ExceptionSensorData} objects to get an overview of recorded
	 *         Exceptions.
	 */
	List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate, Comparator<? super ExceptionSensorData> comparator);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects containing all details of a specific
	 * Exception class.
	 * 
	 * @param template
	 *            The template data object.
	 * @return List of {@link ExceptionSensorData} objects containing all details of a specific
	 *         Exception class.
	 */
	List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template);

	/**
	 * Returns a list of {@link AggregatedExceptionSensorData} objects that is used to show an
	 * overview over Exceptions with specific information about the number of caused event types.
	 * 
	 * @param template
	 *            The template object to be used for the query.
	 * @return A list of {@link AggregatedExceptionSensorData} objects with additional information
	 *         about how often a specific eventType was caused.
	 */
	List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template);

	/**
	 * Returns a list of {@link AggregatedExceptionSensorData} objects that is used to show an
	 * overview over Exceptions with specific information about the number of caused event types.
	 * The returned list contains object that are between the from and to {@link Date} objects.
	 * 
	 * @param template
	 *            The template object to be used for the query.
	 * @param fromDate
	 *            The start date.
	 * @param toDate
	 *            The end date.
	 * @return A list of {@link AggregatedExceptionSensorData} objects with additional information
	 *         about how often a specific eventType was caused.
	 */
	List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate);

	/**
	 * Returns the exception sensor data list for all error and stack message combinations for the
	 * throwable type defined in the template.
	 * 
	 * @param template
	 *            template with throwable type set
	 * @return {@link ExceptionSensorData} list.
	 */
	List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template);
}
