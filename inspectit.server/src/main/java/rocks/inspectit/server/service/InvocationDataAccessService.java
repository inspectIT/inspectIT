package rocks.inspectit.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.alerting.BusinessTransactionsAlertRegistry;
import rocks.inspectit.server.dao.InvocationDataDao;
import rocks.inspectit.server.influx.dao.IInfluxDBDao;
import rocks.inspectit.server.influx.util.QueryResultWrapper;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;

/**
 * @author Patrice Bouillet
 *
 */
@Service
public class InvocationDataAccessService implements IInvocationDataAccessService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The invocation DAO.
	 */
	@Autowired
	private InvocationDataDao invocationDataDao;

	/**
	 * The cached data service for {@link ResultComparator} bounding.
	 */
	@Autowired
	private ICachedDataService cachedDataService;

	/**
	 * The alert registry for business transaction alerts.
	 */
	@Autowired
	private BusinessTransactionsAlertRegistry alertRegistry;

	/**
	 * DAO for the influxDB data.
	 */
	@Autowired
	private IInfluxDBDao influxDBDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, limit, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate, ResultComparator<InvocationSequenceData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, limit, fromDate, toDate, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate,
			ResultComparator<InvocationSequenceData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, invocationIdCollection, limit, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(Long platformId, int limit, Date startDate, Date endDate, Long minId, ResultComparator<InvocationSequenceData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, startDate, endDate, minId, limit, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		InvocationSequenceData result = invocationDataDao.getInvocationSequenceDetail(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, String alertId, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		if (!influxDBDao.isOnline()) {
			log.warn("Was not able to retrieve invocation sequences for the given alert. InfluxDB DAO is not connected to the influxDB instance!");
			return Collections.emptyList();
		}

		String influxDbQuery = alertRegistry.createInfluxDBQueryForAlert(alertId, platformId);
		if (null == influxDbQuery) {
			log.warn("Was not able to retrieve invocation sequences for the given alert. The alert id '" + alertId + "' is unknown or has been evicted!");
			return Collections.emptyList();
		}

		QueryResult queryResult = influxDBDao.query(influxDbQuery);
		QueryResultWrapper resultWrapper = new QueryResultWrapper(queryResult);

		List<Long> invocationSequenceIds = new ArrayList<>();
		for (int i = 0; i < resultWrapper.getRowCount(); i++) {
			long id = (long) resultWrapper.get(i, 1);
			invocationSequenceIds.add(id);
		}

		return getInvocationSequenceOverview(platformId, invocationSequenceIds, limit, resultComparator);
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
			log.info("|-Invocation Data Access Service active...");
		}
	}
}