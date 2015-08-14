package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.JmxSensorValueData;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author Alfred Krauss
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IJmxDataAccessService {

	/**
	 * Returns a list of captured jmx data for a given template. In the template, only the platform
	 * id is extracted.
	 * 
	 * @param jmxSensorValueData
	 *            The template containing the platform id.
	 * @return The list of jmx value data.
	 */
	List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData jmxSensorValueData);

	/**
	 * Returns a list of captured jmx data for a given template in a time frame. In the template,
	 * only the platform id is extracted. Only the latest data of a sensor will be returned.
	 * 
	 * @param jmxSensorValueData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of jmx value data.
	 */
	List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData jmxSensorValueData, Date fromDate, Date toDate);

	/**
	 * Returns a list of captured JMX data for a given template in a time frame.
	 * 
	 * @param template
	 *            The template.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of JMX value data.
	 */
	List<JmxSensorValueData> getJmxData(JmxSensorValueData template, Date fromDate, Date toDate);
}