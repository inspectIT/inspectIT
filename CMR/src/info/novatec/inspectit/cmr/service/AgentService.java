package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.instrumentation.ServerSideInstrumentationManager;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationResult;
import info.novatec.inspectit.spring.logger.Log;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for agent communication with the CMR.
 *
 * @author Ivan Senic
 *
 */
@Service
public class AgentService implements IAgentService {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ServerSideInstrumentationManager}.
	 */
	@Autowired
	ServerSideInstrumentationManager serverSideInstrumentationManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public AgentConfiguration register(List<String> definedIPs, String agentName, String version) throws BusinessException {
		return serverSideInstrumentationManager.register(definedIPs, agentName, version);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public void unregister(List<String> definedIPs, String agentName) throws BusinessException {
		serverSideInstrumentationManager.unregister(definedIPs, agentName);
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public InstrumentationResult analyzeAndInstrument(long platformIdent, String hash, Type sentType) throws BusinessException {
		return serverSideInstrumentationManager.analyzeAndInstrument(platformIdent, hash, sentType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void instrumentationApplied(Map<Long, long[]> methodToSensorMap) {
		serverSideInstrumentationManager.instrumentationApplied(methodToSensorMap);
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
			log.info("|-AgentService active...");
		}
	}

}
