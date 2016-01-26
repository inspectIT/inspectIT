/**
 *
 */
package info.novatec.inspectit.cmr.instrumentation.listener;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.cmr.ci.event.ProfileUpdateEvent;
import info.novatec.inspectit.cmr.instrumentation.ServerSideInstrumentationManager;
import info.novatec.inspectit.cmr.instrumentation.config.AgentCacheEntry;
import info.novatec.inspectit.cmr.instrumentation.config.ConfigurationHolder;
import info.novatec.inspectit.cmr.instrumentation.config.job.ProfileUpdateJob;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener for the {@link ProfileUpdateEvent}s.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ProfileEventListener implements ApplicationListener<ProfileUpdateEvent> {

	/**
	 * ServerSideInstrumentationManager needed for cache map.
	 */
	@Autowired
	private ServerSideInstrumentationManager serverSideInstrumentationManager;

	/**
	 * Factory for creating new {@link ProfileUpdatedJob}.
	 */
	@Autowired
	ObjectFactory<ProfileUpdateJob> profileUpdateJobFactory;

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
	public void onApplicationEvent(ProfileUpdateEvent event) {
		// if the profile is not active before and after update do nothing
		if (!event.isProfileActive() && !event.isProfileDeactivated()) {
			return;
		}

		// look all agent cache entries if profile is contained in environment
		Map<Long, AgentCacheEntry> agentCacheMap = serverSideInstrumentationManager.getAgentCacheMap();
		for (AgentCacheEntry agentCacheEntry : agentCacheMap.values()) {
			ConfigurationHolder configurationHolder = agentCacheEntry.getConfigurationHolder();
			// if not initialized then ignore
			if (!configurationHolder.isInitialized()) {
				continue;
			}

			// if environment does not contain such profile ignore
			Environment environment = configurationHolder.getEnvironment();
			if (CollectionUtils.isEmpty(environment.getProfileIds()) || !environment.getProfileIds().contains(event.getProfileId())) {
				continue;
			}

			// create and fire job
			ProfileUpdateJob profileUpdateJob = profileUpdateJobFactory.getObject();
			profileUpdateJob.setProfileUpdateEvent(event);
			profileUpdateJob.setAgentCacheEntry(agentCacheEntry);

			executor.execute(profileUpdateJob);
		}
	}
}