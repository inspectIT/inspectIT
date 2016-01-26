package rocks.inspectit.server.instrumentation.config.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.EnvironmentUpdateEvent;
import rocks.inspectit.shared.cs.ci.factory.SpecialMethodSensorAssignmentFactory;

/**
 * Job for an environment update.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class EnvironmentUpdateJob extends AbstractConfigurationChangeJob {

	/**
	 * SpecialMethodSensorAssignmentFactory for resolving functional assignment updates.
	 */
	@Autowired
	private SpecialMethodSensorAssignmentFactory functionalAssignmentFactory;

	/**
	 * {@link EnvironmentUpdateEvent}.
	 */
	private EnvironmentUpdateEvent environmentUpdateEvent;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// always update with new Environment
		getConfigurationHolder().update(environmentUpdateEvent.getAfter(), getAgentId());

		// then process removed and added assignments
		super.processRemovedAssignments(environmentUpdateEvent.getRemovedSensorAssignments(functionalAssignmentFactory));
		super.processAddedAssignments(environmentUpdateEvent.getAddedSensorAssignments(functionalAssignmentFactory));
	}

	/**
	 * Sets {@link #environmentUpdateEvent}.
	 *
	 * @param environmentUpdateEvent
	 *            New value for {@link #environmentUpdateEvent}
	 */
	public void setEnvironmentUpdateEvent(EnvironmentUpdateEvent environmentUpdateEvent) {
		this.environmentUpdateEvent = environmentUpdateEvent;
	}

}
