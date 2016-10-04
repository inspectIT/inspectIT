package rocks.inspectit.shared.cs.cmr.service;

import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;

/**
 * Service to query details from the CMR's InfluxDB.
 *
 * @author Marius Oehler
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IInfluxDBService {

	/**
	 * Get the measurements of the current database.
	 *
	 * @return a list containing the existing measurements
	 */
	List<String> getMeasurements();

	/**
	 * Returns all existing tags in the specified measurement.
	 *
	 * @param measurement
	 *            the measurement
	 * @return a list containing tag keys
	 */
	List<String> getTags(String measurement);

	/**
	 * Returns all existing values of the specified tag key in the specified measurement.
	 *
	 * @param measurement
	 *            the measurement
	 * @param tagKey
	 *            the tag key
	 * @return list of the values of the tag
	 */
	List<String> getTagValues(String measurement, String tagKey);

	/**
	 * Returns all existing fields in the specified measurement.
	 *
	 * @param measurement
	 *            the measurement
	 * @return a list containing fields
	 */
	List<String> getFields(String measurement);
}
