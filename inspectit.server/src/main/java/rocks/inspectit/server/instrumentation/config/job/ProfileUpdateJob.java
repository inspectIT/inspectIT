package rocks.inspectit.server.instrumentation.config.job;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Profile update job that runs for profile update against one environment/class cache.
 *
 * @author Ivan Senic
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ProfileUpdateJob extends AbstractConfigurationChangeJob {

	/**
	 * Logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * Update event that defines changed properties.
	 */
	private ProfileUpdateEvent profileUpdateEvent;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<ImmutableType> execute() {
		// always update configuration
		getConfigurationHolder().update(getEnvironment(), getAgentId());

		Collection<ImmutableType> changedClassTypes = new HashSet<>();

		// first process all removed and added assignments
		changedClassTypes.addAll(super.processRemovedAssignments(profileUpdateEvent.getRemovedSensorAssignments()));
		changedClassTypes.addAll(super.processAddedAssignments(profileUpdateEvent.getAddedSensorAssignments()));

		return changedClassTypes;
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
