package rocks.inspectit.server.service;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IJmxDataAccessService;

/**
 * Service class for retrieving {@link JmxSensorValueData} objects from the CMR.
 *
 * @author Alfred Krauss
 *
 */
@Service
@Transactional
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
	@Override
	@MethodLog
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData jmxSensorValueData) {
		List<JmxSensorValueData> result = defaultDataDao.getJmxDataOverview(jmxSensorValueData, null, null);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
