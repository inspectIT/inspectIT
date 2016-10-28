package rocks.inspectit.server.instrumentation.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.EnvironmentUpdateEvent;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.job.AbstractConfigurationChangeJob;
import rocks.inspectit.server.instrumentation.config.job.EnvironmentUpdateJob;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Listener for the {@link EnvironmentUpdateEvent}s.
 *
 * @author Ivan Senic
 * @author Marius Oehler
 *
 */
@Component
public class EnvironmentEventListener extends AbstractConfigurationChangeListener<EnvironmentUpdateEvent> {

	/**
	 * The used {@link ObjectFactory}.
	 */
	@Autowired
	private ObjectFactory<EnvironmentUpdateJob> objectFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(EnvironmentUpdateEvent event) {
		List<AbstractConfigurationChangeJob> jobs = new ArrayList<>();

		Map<Long, AgentCacheEntry> agentCacheMap = nextGenInstrumentationManager.getAgentCacheMap();
		for (AgentCacheEntry agentCacheEntry : agentCacheMap.values()) {
			ConfigurationHolder configurationHolder = agentCacheEntry.getConfigurationHolder();
			// if not initialized then ignore
			if (!configurationHolder.isInitialized()) {
				continue;
			}

			// if environments ids not match ignore
			Environment environment = configurationHolder.getEnvironment();
			if (!Objects.equals(environment.getId(), event.getEnvironmentId())) {
				continue;
			}

			// create and fire job
			EnvironmentUpdateJob environmentUpdateJob = objectFactory.getObject();
			environmentUpdateJob.setEnvironmentUpdateEvent(event);
			environmentUpdateJob.setAgentCacheEntry(agentCacheEntry);

			jobs.add(environmentUpdateJob);
		}

		if (CollectionUtils.isNotEmpty(jobs)) {
			executeJobs(jobs);
		}
	}
}