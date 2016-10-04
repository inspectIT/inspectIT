package rocks.inspectit.server.ci.event;

import java.util.List;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Event that signals that the existing {@link AlertingDefinition}s have been loaded by the CI.
 *
 * @author Marius Oehler
 *
 */

public class AlertingDefinitionLoadedEvent extends AlertingDefinitionEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -9146704118986474777L;

	/**
	 * List of loaded {@link AlertingDefinition}s.
	 */
	private final List<AlertingDefinition> loadedAlertingDefinitions;

	/**
	 * @param source
	 *            the component that published the event (never {@code null})
	 * @param loadedAlertingDefinitions
	 *            list of loaded {@link AlertingDefinition}s
	 */
	public AlertingDefinitionLoadedEvent(Object source, List<AlertingDefinition> loadedAlertingDefinitions) {
		super(source);
		this.loadedAlertingDefinitions = loadedAlertingDefinitions;
	}

	/**
	 * Gets {@link #loadedAlertingDefinitions}.
	 *
	 * @return {@link #loadedAlertingDefinitions}
	 */
	public List<AlertingDefinition> getLoadedAlertingDefinitions() {
		return loadedAlertingDefinitions;
	}

}
