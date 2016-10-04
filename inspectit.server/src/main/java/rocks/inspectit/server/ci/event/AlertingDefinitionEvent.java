/**
 *
 */
package rocks.inspectit.server.ci.event;

import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Base class for all events related to {@link AlertingDefinition}.
 * 
 * @author Marius Oehler
 *
 */
public abstract class AlertingDefinitionEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4223794223438737561L;

	/**
	 * Constructor.
	 *
	 * @param source
	 *            the component that published the event (never {@code null})
	 */
	public AlertingDefinitionEvent(Object source) {
		super(source);
	}

}
