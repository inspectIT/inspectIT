package rocks.inspectit.server.instrumentation.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.AgentMappingsUpdateEvent;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.server.instrumentation.config.job.AbstractConfigurationChangeJob;
import rocks.inspectit.server.instrumentation.config.job.EnvironmentMappingUpdateJob;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Listener for the {@link AgentMappingsUpdateEvent}s.
 *
 * @author Ivan Senic
 * @author Marius Oehler
 *
 */
@Component
public class AgentMappingsEventListener extends AbstractConfigurationChangeListener<AgentMappingsUpdateEvent> {

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
	 * The used {@link ObjectFactory}.
	 */
	@Autowired
	private ObjectFactory<EnvironmentMappingUpdateJob> objectFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(AgentMappingsUpdateEvent event) {
		List<AbstractConfigurationChangeJob> jobs = new ArrayList<>();

		Map<Long, AgentCacheEntry> agentCacheMap = nextGenInstrumentationManager.getAgentCacheMap();
		// iterate all caches
		for (AgentCacheEntry agentCacheEntry : agentCacheMap.values()) {
			ConfigurationHolder configurationHolder = agentCacheEntry.getConfigurationHolder();
			Environment cachedEnvironment = configurationHolder.getEnvironment();
			PlatformIdent platformIdent = platformIdentDao.load(agentCacheEntry.getId());
			try {
				// see what 's the new environment for the agent
				Environment environment = configurationResolver.getEnvironmentForAgent(platformIdent.getDefinedIPs(), platformIdent.getAgentName());

				// fire job only if we have new environment or we were not bounded to any
				// environment
				if ((null == cachedEnvironment) || !ObjectUtils.equals(cachedEnvironment.getId(), environment.getId())) {
					EnvironmentMappingUpdateJob mappingUpdateJob = objectFactory.getObject();
					mappingUpdateJob.setEnvironment(environment);
					mappingUpdateJob.setAgentCacheEntry(agentCacheEntry);

					jobs.add(mappingUpdateJob);
				}
			} catch (BusinessException e) {
				// if we have exception by resolving new environment run job with no new
				// environment
				EnvironmentMappingUpdateJob mappingUpdateJob = objectFactory.getObject();
				mappingUpdateJob.setAgentCacheEntry(agentCacheEntry);

				jobs.add(mappingUpdateJob);
			}
		}

		if (CollectionUtils.isNotEmpty(jobs)) {
			executeJobs(jobs);
		}
	}

}