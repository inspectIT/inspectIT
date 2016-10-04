package rocks.inspectit.server.ci.event;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Event that signals that an {@link AlertingDefinition} has been created via CI.
 *
 * @author Marius Oehler
 *
 */
public class AlertingDefinitionCreatedEvent extends AlertingDefinitionEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5044832128387415161L;

	/**
	 * Created {@link AlertingDefinition}.
	 */
	private final AlertingDefinition createdAlertingDefinition;

	/**
	 * @param source
	 *            the component that published the event (never {@code null})
	 * @param createdAlertingDefinition
	 *            the {@link AlertingDefinition} which has been created
	 */
	public AlertingDefinitionCreatedEvent(Object source, AlertingDefinition createdAlertingDefinition) {
		super(source);
		this.createdAlertingDefinition = createdAlertingDefinition;
	}

	/**
	 * Gets {@link #createdAlertingDefinition}.
	 *
	 * @return {@link #createdAlertingDefinition}
	 */
	public AlertingDefinition getCreatedAlertingDefinition() {
		return createdAlertingDefinition;
	}
}
