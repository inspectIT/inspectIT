package rocks.inspectit.server.instrumentation.config.job;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.ProfileUpdateEvent;

/**
 * Profile update job that runs for profile update against one environment/class cache.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ProfileUpdateJob extends AbstractConfigurationChangeJob {

	/**
	 * Update event that defines changed properties.
	 */
	private ProfileUpdateEvent profileUpdateEvent;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// always update configuration
		getConfigurationHolder().update(getEnvironment(), getAgentId());

		// first process all removed and added assignments
		super.processRemovedAssignments(profileUpdateEvent.getRemovedSensorAssignments());
		super.processAddedAssignments(profileUpdateEvent.getAddedSensorAssignments());
	}

	/**
	 * Sets {@link #profileUpdateEvent}.
	 *
	 * @param profileUpdateEvent
	 *            New value for {@link #profileUpdateEvent}
	 */
	public void setProfileUpdateEvent(ProfileUpdateEvent profileUpdateEvent) {
		this.profileUpdateEvent = profileUpdateEvent;
	}

}
