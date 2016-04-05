/**
 *
 */
package info.novatec.inspectit.cmr.instrumentation.listener;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.cmr.ci.event.AgentMappingsUpdateEvent;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.instrumentation.NextGenInstrumentationManager;
import info.novatec.inspectit.cmr.instrumentation.config.AgentCacheEntry;
import info.novatec.inspectit.cmr.instrumentation.config.ConfigurationHolder;
import info.novatec.inspectit.cmr.instrumentation.config.ConfigurationResolver;
import info.novatec.inspectit.cmr.instrumentation.config.job.EnvironmentMappingUpdateJob;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.exception.BusinessException;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

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
	private NextGenInstrumentationManager serverSideInstrumentationManager;

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
		Map<Long, AgentCacheEntry> agentCacheMap = serverSideInstrumentationManager.getAgentCacheMap();
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