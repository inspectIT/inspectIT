package rocks.inspectit.server.instrumentation.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.job.AbstractConfigurationChangeJob;
import rocks.inspectit.server.instrumentation.config.job.ProfileUpdateJob;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Listener for the {@link ProfileUpdateEvent}s.
 *
 * @author Ivan Senic
 * @author Marius Oehler
 *
 */
@Component
public class ProfileEventListener extends AbstractConfigurationChangeListener<ProfileUpdateEvent> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(ProfileUpdateEvent event) {
		// if the profile is not active before and after update do nothing
		if (!event.isProfileActive() && !event.isProfileDeactivated()) {
			return;
		}

		List<AbstractConfigurationChangeJob> jobs = new ArrayList<>();

		// look all agent cache entries if profile is contained in environment
		Map<Long, AgentCacheEntry> agentCacheMap = nextGenInstrumentationManager.getAgentCacheMap();
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
			ProfileUpdateJob profileUpdateJob = createJob(ProfileUpdateJob.class);
			profileUpdateJob.setProfileUpdateEvent(event);
			profileUpdateJob.setAgentCacheEntry(agentCacheEntry);

			jobs.add(profileUpdateJob);
		}

		if (CollectionUtils.isNotEmpty(jobs)) {
			executeJobs(jobs);
		}
	}
}