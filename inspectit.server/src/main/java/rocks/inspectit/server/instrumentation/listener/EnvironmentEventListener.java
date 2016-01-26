package rocks.inspectit.server.instrumentation.listener;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.EnvironmentUpdateEvent;
import rocks.inspectit.server.instrumentation.NextGenInstrumentationManager;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.job.EnvironmentUpdateJob;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Listener for the {@link EnvironmentUpdateEvent}s.
 *
 * @author Ivan Senic
 *
 */
@Component
public class EnvironmentEventListener implements ApplicationListener<EnvironmentUpdateEvent> {

	/**
	 * NextGenInstrumentationManager need.
	 */
	@Autowired
	private NextGenInstrumentationManager nextGenInstrumentationManager;

	/**
	 * Factory for creating new {@link EnvironmentUpdateJob}.
	 */
	@Autowired
	ObjectFactory<EnvironmentUpdateJob> environmentUpdateJobFactory;

	/**
	 * Executor for dealing with configuration updates.
	 */
	@Autowired
	@Qualifier("agentServiceExecutorService")
	ExecutorService executor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(EnvironmentUpdateEvent event) {
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
			EnvironmentUpdateJob environmentUpdateJob = environmentUpdateJobFactory.getObject();
			environmentUpdateJob.setEnvironmentUpdateEvent(event);
			environmentUpdateJob.setAgentCacheEntry(agentCacheEntry);

			executor.execute(environmentUpdateJob);
		}
	}
}