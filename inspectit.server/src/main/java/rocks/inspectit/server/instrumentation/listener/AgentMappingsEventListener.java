package rocks.inspectit.server.instrumentation.listener;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.AgentMappingsUpdateEvent;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.instrumentation.NextGenInstrumentationManager;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.server.instrumentation.config.job.EnvironmentMappingUpdateJob;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Listener for the {@link AgentMappingsUpdateEvent}s.
 *
 * @author Ivan Senic
 *
 */
@Component
public class AgentMappingsEventListener implements ApplicationListener<AgentMappingsUpdateEvent> {

	/**
	 * NextGenInstrumentationManager needed.
	 */
	@Autowired
	private NextGenInstrumentationManager nextGenInstrumentationManager;

	/**
	 * Factory for creating new {@link EnvironmentMappingUpdateJob}.
	 */
	@Autowired
	private ObjectFactory<EnvironmentMappingUpdateJob> mappingUpdateJobFactory;

	/**
	 * Configuration resolver.
	 */
	@Autowired
	private ConfigurationResolver configurationResolver;

	/**
	 * Platform ident dao for resolving agents by ids.
	 */
	@Autowired
	private PlatformIdentDao platformIdentDao;

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
	public void onApplicationEvent(AgentMappingsUpdateEvent event) {
		Map<Long, AgentCacheEntry> agentCacheMap = nextGenInstrumentationManager.getAgentCacheMap();
		// iterate all caches
		for (AgentCacheEntry agentCacheEntry : agentCacheMap.values()) {
			ConfigurationHolder configurationHolder = agentCacheEntry.getConfigurationHolder();
			Environment cachedEnvironment = configurationHolder.getEnvironment();
			PlatformIdent platformIdent = platformIdentDao.load(agentCacheEntry.getId());
			try {
				// see what 's the new environment for the agent
				Environment environment = configurationResolver.getEnvironmentForAgent(platformIdent.getDefinedIPs(), platformIdent.getAgentName());

				// only if we have new environment fire job
				if (!ObjectUtils.equals(cachedEnvironment.getId(), environment.getId())) {
					EnvironmentMappingUpdateJob mappingUpdateJob = mappingUpdateJobFactory.getObject();
					mappingUpdateJob.setEnvironment(environment);
					mappingUpdateJob.setAgentCacheEntry(agentCacheEntry);

					executor.execute(mappingUpdateJob);
				}
			} catch (BusinessException e) {
				// if we have exception by resolving new environment run job with no new
				// environment
				EnvironmentMappingUpdateJob mappingUpdateJob = mappingUpdateJobFactory.getObject();
				mappingUpdateJob.setAgentCacheEntry(agentCacheEntry);

				executor.execute(mappingUpdateJob);
			}
		}
	}

}