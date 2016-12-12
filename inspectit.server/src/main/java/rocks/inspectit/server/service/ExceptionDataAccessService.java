package rocks.inspectit.server.service;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.dao.ExceptionSensorDataDao;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.AggregatedExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IExceptionDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;

/**
 * Service class for retrieving {@link ExceptionSensorData} objects from the CMR.
 *
 * @author Eduard Tudenhoefner
 *
 */
@Service
public class ExceptionDataAccessService implements IExceptionDataAccessService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The exception sensor DAO.
	 */
	@Autowired
	private ExceptionSensorDataDao exceptionSensorDataDao;

	/**
	 * {@link CachedDataService}.
	 */
	@Autowired
	private ICachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, ResultComparator<ExceptionSensorData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate, ResultComparator<ExceptionSensorData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit, fromDate, toDate, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, ResultComparator<ExceptionSensorData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate, ResultComparator<ExceptionSensorData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, fromDate, toDate, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getExceptionTree(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		List<AggregatedExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		List<AggregatedExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getStackTraceMessagesForThrowableType(template);
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
		if (log.isInfoEnabled()) {
			log.info("|-Exception Sensor Data Access Service active...");
		}
	}
}
