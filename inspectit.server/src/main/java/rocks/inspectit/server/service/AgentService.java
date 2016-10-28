package rocks.inspectit.server.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.instrumentation.NextGenInstrumentationManager;
import rocks.inspectit.server.messaging.AgentInstrumentationMessageGate;
import rocks.inspectit.server.messaging.AgentMessageProvider;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.shared.all.cmr.service.IAgentService;
import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.spring.logger.Log;

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
	 * {@link NextGenInstrumentationManager}.
	 */
	@Autowired
	NextGenInstrumentationManager nextGenInstrumentationManager;

	/**
	 * The {@link AgentInstrumentationMessageGate}.
	 */
	@Autowired
	AgentInstrumentationMessageGate messageGate;

	/**
	 * The {@link AgentMessageProvider}.
	 */
	@Autowired
	AgentMessageProvider messageProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public AgentConfig register(List<String> definedIPs, String agentName, String version) throws BusinessException {
		AgentConfig agentConfig = nextGenInstrumentationManager.register(definedIPs, agentName, version);

		// reset instrumentation status
		messageGate.clear(agentConfig.getPlatformId());
		messageProvider.clear(agentConfig.getPlatformId());

		return agentConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public void unregister(long platformIdent) throws BusinessException {
		nextGenInstrumentationManager.unregister(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public InstrumentationDefinition analyze(long platformIdent, String hash, Type sentType) throws BusinessException {
		return nextGenInstrumentationManager.analyze(platformIdent, hash, sentType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public void instrumentationApplied(long platformId, Map<Long, long[]> methodToSensorMap) {
		nextGenInstrumentationManager.instrumentationApplied(platformId, methodToSensorMap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public Collection<JmxAttributeDescriptor> analyzeJmxAttributes(long platformIdent, Collection<JmxAttributeDescriptor> attributeDescriptors) throws BusinessException {
		return nextGenInstrumentationManager.analyzeJmxAttributes(platformIdent, attributeDescriptors);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<IAgentMessage<?>> fetchAgentMessages(long platformId) {
		return messageProvider.fetchMessages(platformId);
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
