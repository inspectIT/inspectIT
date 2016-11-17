package rocks.inspectit.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.alerting.AlertRegistry;
import rocks.inspectit.server.alerting.util.AlertingUtils;
import rocks.inspectit.server.dao.InvocationDataDao;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.server.influx.util.InfluxQueryFactory;
import rocks.inspectit.server.influx.util.QueryResultWrapper;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AlertErrorCodeEnum;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

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
	private AlertRegistry alertRegistry;

	/**
	 * DAO for the influxDB data.
	 */
	@Autowired
	private InfluxDBDao influxDBDao;

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
	public List<InvocationSequenceData> getInvocationSequenceOverview(Long platformId, int limit, Date startDate, Date endDate, Long minId, int businessTrxId, int applicationId, String alertId, // NOCHK
			ResultComparator<InvocationSequenceData> resultComparator) throws BusinessException {
		List<Long> invocationSequenceIds = null;

		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}

		if (StringUtils.isNotEmpty(alertId)) {
			invocationSequenceIds = getInvocationSequenceIds(alertId);
		}

		return invocationDataDao.getInvocationSequenceOverview(platformId, startDate, endDate, minId, limit, businessTrxId, applicationId, invocationSequenceIds, resultComparator);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(String alertId, int limit, ResultComparator<InvocationSequenceData> resultComparator) throws BusinessException {
		List<Long> invocationSequenceIds = getInvocationSequenceIds(alertId);

		return getInvocationSequenceOverview(0, invocationSequenceIds, limit, resultComparator);
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

	/**
	 * Returns a list of {@link Long} invocation sequences id's belonging to an alert defined by the
	 * passed alert id.
	 *
	 * @param alertId
	 *            The ID of the alert the invocation sequences belong to.
	 * @return Returns the list of IDs of invocation sequences.
	 * @throws BusinessException
	 *             If data cannot be retrieved.
	 */
	private List<Long> getInvocationSequenceIds(String alertId) throws BusinessException {
		if (!influxDBDao.isConnected()) {
			throw new BusinessException("Retrieving invocation sequences for alert with id '" + alertId + "'", AlertErrorCodeEnum.DATABASE_OFFLINE);
		}
		Alert alert = alertRegistry.getAlert(alertId);
		if (null == alert) {
			throw new BusinessException("Retrieving invocation sequences for alert with id '" + alertId + "'", AlertErrorCodeEnum.UNKNOWN_ALERT_ID);
		}
		if (!AlertingUtils.isBusinessTransactionAlert(alert.getAlertingDefinition())) {
			throw new BusinessException("The given alert '" + alertId + "' is not related to a buisness transaction.", AlertErrorCodeEnum.NO_BTX_ALERT);
		}

		String influxDbQuery = InfluxQueryFactory.buildTraceIdForAlertQuery(alert);
		QueryResult queryResult = influxDBDao.query(influxDbQuery);
		QueryResultWrapper resultWrapper = new QueryResultWrapper(queryResult);

		List<Long> invocationSequenceIds = new ArrayList<>();
		for (int i = 0; i < resultWrapper.getRowCount(); i++) {
			Double id = resultWrapper.getDouble(i, 1);
			invocationSequenceIds.add(id.longValue());
		}

		return invocationSequenceIds;
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
	public Collection<InvocationSequenceData> getInvocationSequenceDetail(long traceId) {
		return invocationDataDao.getInvocationSequenceDetail(traceId);
	}

}