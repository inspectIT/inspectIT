package rocks.inspectit.server.service;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.event.AgentDeletedEvent;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AgentManagementErrorCodeEnum;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * @author Patrice Bouillet
 *
 */
@Service
@Transactional
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
	 * Event publisher.
	 */
	@Autowired
	ApplicationEventPublisher eventPublisher;

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
	@MethodLog
	public PlatformIdent getCompleteAgent(long id) throws BusinessException {
		PlatformIdent platformIdent = platformIdentDao.findInitialized(id);
		if (null != platformIdent) {
			return platformIdent;
		} else {
			throw new BusinessException("Load the agent with ID=" + id + ".", AgentManagementErrorCodeEnum.AGENT_DOES_NOT_EXIST);
		}
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public void deleteAgent(long platformId) throws BusinessException {
		PlatformIdent platformIdent = platformIdentDao.load(platformId);
		if (null != platformIdent) {
			AgentStatusData agentStatusData = agentStatusProvider.getAgentStatusDataMap().get(platformIdent.getId());

			// delete is allowed only if agent is disconnected or was never connected
			if (null != agentStatusData && agentStatusData.getAgentConnection() == AgentConnection.CONNECTED) {
				throw new BusinessException("Delete the agent '" + platformIdent.getAgentName() + "'.", AgentManagementErrorCodeEnum.AGENT_CAN_NOT_BE_DELETED);
			}

			platformIdentDao.delete(platformIdent);
			defaultDataDao.deleteAll(platformIdent.getId());

			AgentDeletedEvent event = new AgentDeletedEvent(this, platformIdent);
			eventPublisher.publishEvent(event);

			log.info("The Agent '" + platformIdent.getAgentName() + "' with the ID " + platformIdent.getId() + " was successfully deleted from the CMR.");
		} else {
			throw new BusinessException("Delete the agent with the ID=" + platformId + ".", AgentManagementErrorCodeEnum.AGENT_DOES_NOT_EXIST);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		List<DefaultData> result = defaultDataDao.findByExampleWithLastInterval(template, timeInterval);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public DefaultData getLastDataObject(DefaultData template) {
		DefaultData result = defaultDataDao.findByExampleLastData(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
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
	@Override
	@MethodLog
	public List<DefaultData> getDataObjectsSinceId(DefaultData template) {
		List<DefaultData> result = defaultDataDao.findByExampleSinceId(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
