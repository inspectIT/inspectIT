package rocks.inspectit.server.service;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.messaging.AgentInstrumentationMessageGate;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IAgentInstrumentationService;

/**
 * Service for management of the agents instrumentation.
 *
 * @author Marius Oehler
 *
 */
@Service
public class AgentInstrumentationService implements IAgentInstrumentationService {

	/**
	 * Logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * The {@link AgentInstrumentationMessageGate}.
	 */
	@Autowired
	AgentInstrumentationMessageGate messageGate;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateInstrumentation(Collection<Long> updatePlatformIds) {
		if (CollectionUtils.isEmpty(updatePlatformIds)) {
			throw new IllegalArgumentException("The collection of platform idents may not be null or empty.");
		}
		if (log.isInfoEnabled()) {
			log.info("Instrumentation updated messages flushed for the agent(s) with ID(s): {}", Arrays.toString(updatePlatformIds.toArray()));
		}

		for (long platformId : updatePlatformIds) {
			messageGate.flush(platformId);
		}
	}

}
