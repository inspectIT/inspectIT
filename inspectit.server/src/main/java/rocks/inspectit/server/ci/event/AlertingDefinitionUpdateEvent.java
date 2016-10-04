package rocks.inspectit.server.ci.event;

import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Event that signals that an {@link AlertingDefinition} has been updated via CI.
 *
 * @author Marius Oehler
 *
 */
public class AlertingDefinitionUpdateEvent extends ApplicationEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = -5594020575958880826L;

	/**
	 * Updated {@link AlertingDefinition}.
	 */
	private final transient AlertingDefinition updateAlertingDefinition;

	/**
	 * @param source
	 *            the component that published the event (never {@code null})
	 * @param updateAlertingDefinition
	 *            the {@link AlertingDefinition} which has been updated
	 */
	public AlertingDefinitionUpdateEvent(Object source, AlertingDefinition updateAlertingDefinition) {
		super(source);
		this.updateAlertingDefinition = updateAlertingDefinition;
	}

	/**
	 * Gets {@link #updateAlertingDefinition}.
	 *
	 * @return {@link #updateAlertingDefinition}
	 */
	public AlertingDefinition getUpdateAlertingDefinition() {
		return updateAlertingDefinition;
	}
}
