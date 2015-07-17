package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	 * {@inheritDoc}
	 */
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
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate, ResultComparator<InvocationSequenceData> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate, resultComparator);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
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
	@MethodLog
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		InvocationSequenceData result = invocationDataDao.getInvocationSequenceDetail(template);
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
			log.info("|-Invocation Data Access Service active...");
		}
	}

}