package rocks.inspectit.shared.all.indexing;

import java.sql.Timestamp;
import java.util.List;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.indexing.restriction.IIndexQueryRestriction;

/**
 * Interface for query that queries the indexing tree.
 *
 * @author Ivan Senic
 *
 */
public interface IIndexQuery {

	/**
	 * Minimum object id to be returned by the query. Query should return only objects that have
	 * bigger id than this one.
	 *
	 * @return minimum id
	 */
	long getMinId();

	/**
	 * Set minimum id for objects to be returned by the query.
	 *
	 * @param minId
	 *            minimum id or 0 for returning all objects
	 */
	void setMinId(long minId);

	/**
	 * Platform id that objects returned by query should have.
	 *
	 * @return platform id
	 */
	long getPlatformIdent();

	/**
	 * Set platform id for objects to be returned by the query.
	 *
	 * @param platformIdent
	 *            platform id or 0 for not including this property in the query
	 */
	void setPlatformIdent(long platformIdent);

	/**
	 * Sensor type id that objects returned by query should have.
	 *
	 * @return sensor type id
	 */
	long getSensorTypeIdent();

	/**
	 * Set sensor type id for objects to be returned by the query.
	 *
	 * @param sensorTypeIdent
	 *            sensor type id or 0 for not including this property in the query
	 */
	void setSensorTypeIdent(long sensorTypeIdent);

	/**
	 * Method id that objects returned by query should have.
	 *
	 * @return method id
	 */
	long getMethodIdent();

	/**
	 * Set method id for objects to be returned by the query.
	 *
	 * @param methodIdent
	 *            method id or 0 for not including this property in the query
	 */
	void setMethodIdent(long methodIdent);

	/**
	 * Classes of the objects returned by query. Query will only return objects that are of this
	 * classes, thus no objects that are of a class that is extending one of the classes.
	 *
	 * @return class
	 */
	List<Class<?>> getObjectClasses();

	/**
	 * Set the classes for objects to be returned by query. Only objects that are instances of
	 * supplied classes will be returned by query.
	 *
	 * @param objectClasses
	 *            class or null for not including this property in the query
	 */
	void setObjectClasses(List<Class<?>> objectClasses);

	/**
	 * Time stamp that represents date after which objects returned by query are created.
	 *
	 * @return timestamp
	 */
	Timestamp getFromDate();

	/**
	 * Sets that time stamp that represents date, so that all objects returned by query are created
	 * after this date.
	 *
	 * @param fromDate
	 *            starting date
	 */
	void setFromDate(Timestamp fromDate);

	/**
	 * Time stamp that represents date before which objects returned by query are created.
	 *
	 * @return timestamp
	 */
	Timestamp getToDate();

	/**
	 * Sets that time stamp that represents date, so that all objects returned by query are created
	 * before this date.
	 *
	 * @param toDate
	 *            end date
	 */
	void setToDate(Timestamp toDate);

	/**
	 * Adds one indexing restriction to the query.
	 *
	 * @param indexingRestriction
	 *            Indexing restriction.
	 */
	void addIndexingRestriction(IIndexQueryRestriction indexingRestriction);

	/**
	 * Returns if the searching interval is set for current {@link IIndexQuery} object. The method
	 * will return true only when both {@link #getFromDate} and {@link #getToDate} time stamps are
	 * not null, and when {@link #getToDate} is after {@link #getFromDate}.
	 *
	 * @return if the searching interval is set for current {@link IIndexQuery} object.
	 */
	boolean isIntervalSet();

	/**
	 * Returns if the given time stamp is belonging to the interval set in the {@link IIndexQuery}
	 * object. This method will return true only when interval is set for current object (see
	 * {@link #isInIntervalSet()}) and given time stamp object is in interval ({@link #getFromDate}
	 * >= timestamp >= {@link #getToDate}).
	 *
	 * @param timestamp
	 *            time stamp to be checked
	 * @return if the given time stamp is belonging to the interval set in the {@link IIndexQuery}
	 *         object.
	 */
	boolean isInInterval(Timestamp timestamp);

	/**
	 * Checks if the all restrictions in the query are fulfilled for given {@link DefaultData}
	 * object.
	 *
	 * @param defaultData
	 *            Object to be checked
	 * @return true if all restrictions are fulfilled, otherwise no
	 */
	boolean areAllRestrictionsFulfilled(DefaultData defaultData);

}
