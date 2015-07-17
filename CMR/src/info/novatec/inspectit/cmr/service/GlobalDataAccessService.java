package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;
import info.novatec.inspectit.spring.logger.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Patrice Bouillet
 * 
 */
@Service
public class GlobalDataAccessService implements IGlobalDataAccessService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The platform ident DAO.
	 */
	@Autowired
	PlatformIdentDao platformIdentDao;

	/**
	 * The default data DAO.
	 */
	@Autowired
	DefaultDataDao defaultDataDao;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider agentStatusProvider;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<PlatformIdent, AgentStatusData> getAgentsOverview() {
		List<PlatformIdent> agents = platformIdentDao.findAll();
		Map<Long, AgentStatusData> agentStatusMap = agentStatusProvider.getAgentStatusDataMap();

		Map<PlatformIdent, AgentStatusData> resultMap = new HashMap<PlatformIdent, AgentStatusData>();
		for (PlatformIdent platformIdent : agents) {
			resultMap.put(platformIdent, agentStatusMap.get(platformIdent.getId()));
		}
		return resultMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public PlatformIdent getCompleteAgent(long id) throws ServiceException {
		PlatformIdent platformIdent = platformIdentDao.findInitialized(id);
		if (null != platformIdent) {
			return platformIdent;
		} else {
			throw new ServiceException("Agent with given ID=" + id + " is not existing.");
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteAgent(long platformId) throws ServiceException {
		PlatformIdent platformIdent = platformIdentDao.load(platformId);
		if (null != platformIdent) {
			AgentStatusData agentStatusData = agentStatusProvider.getAgentStatusDataMap().get(platformIdent.getId());

			// delete is allowed only if agent is disconnected or was never connected
			if (null != agentStatusData && agentStatusData.getAgentConnection() == AgentConnection.CONNECTED) {
				throw new ServiceException("The Agent '" + platformIdent.getAgentName() + "' can not be deleted because it's still connected.");
			}

			platformIdentDao.delete(platformIdent);
			defaultDataDao.deleteAll(platformIdent.getId());
			agentStatusProvider.registerDeleted(platformId);

			log.info("The Agent '" + platformIdent.getAgentName() + "' with the ID " + platformIdent.getId() + " was successfully deleted from the CMR.");
		} else {
			throw new ServiceException("The Agent with the ID=" + platformId + " does not exists on the CMR.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		List<DefaultData> result = defaultDataDao.findByExampleWithLastInterval(template, timeInterval);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public DefaultData getLastDataObject(DefaultData template) {
		DefaultData result = defaultDataDao.findByExampleLastData(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<? extends DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<DefaultData> result = defaultDataDao.findByExampleFromToDate(template, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<? extends DefaultData> getTemplatesDataObjectsFromToDate(Collection<DefaultData> templates, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<DefaultData> result = new ArrayList<>();
		for (DefaultData template : templates) {
			result.addAll(this.getDataObjectsFromToDate(template, fromDate, toDate));
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<DefaultData> getDataObjectsSinceId(DefaultData template) {
		List<DefaultData> result = defaultDataDao.findByExampleSinceId(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		List<DefaultData> result = defaultDataDao.findByExampleSinceIdIgnoreMethodId(template);
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
			log.info("|-Global Data Access Service active...");
		}
	}

}
