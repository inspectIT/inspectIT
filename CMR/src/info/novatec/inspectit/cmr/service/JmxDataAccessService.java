package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for retrieving {@link JmxSensorValueData} objects from the CMR.
 * 
 * @author Alfred Krauss
 * 
 */
@Service
public class JmxDataAccessService implements IJmxDataAccessService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The default DAO.
	 */
	@Autowired
	private DefaultDataDao defaultDataDao;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData jmxSensorValueData) {
		List<JmxSensorValueData> result = defaultDataDao.getJmxDataOverview(jmxSensorValueData, null, null);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData jmxSensorValueData, Date fromDate, Date toDate) {
		List<JmxSensorValueData> result = defaultDataDao.getJmxDataOverview(jmxSensorValueData, fromDate, toDate);
		return result;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		log.info("|-Jmx Sensor Data Access Service active...");
	}

}
