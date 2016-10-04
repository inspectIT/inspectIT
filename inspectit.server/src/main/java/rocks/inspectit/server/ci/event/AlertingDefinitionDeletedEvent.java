package rocks.inspectit.server.ci.event;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Event that signals that an {@link AlertingDefinition} has been deleted via CI.
 *
 * @author Marius Oehler
 *
 */
public class AlertingDefinitionDeletedEvent extends AlertingDefinitionEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5333703504708056275L;

	/**
	 * Removed alerting definition.
	 */
	private final AlertingDefinition removedAlertingDefinition;

	/**
	 * @param source
	 *            the component that published the event (never {@code null})
	 * @param deletedAlertingDefinition
	 *            the {@link AlertingDefinition} which has been deleted
	 */
	public AlertingDefinitionDeletedEvent(Object source, AlertingDefinition deletedAlertingDefinition) {
		super(source);
		this.removedAlertingDefinition = deletedAlertingDefinition;
	}

	/**
	 * Gets {@link #removedAlertingDefinition}.
	 *
	 * @return {@link #removedAlertingDefinition}
	 */
	public AlertingDefinition getRemovedAlertingDefinition() {
		return removedAlertingDefinition;
	}

}
