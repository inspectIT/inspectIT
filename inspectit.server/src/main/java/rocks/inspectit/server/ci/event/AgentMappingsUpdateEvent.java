package rocks.inspectit.server.ci.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event that signals that the configuration interface mappings have been updated.
 *
 * @author Ivan Senic
 *
 */
public class AgentMappingsUpdateEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2427305108647290514L;

	/**
	 * Default constructor.
	 *
	 * @param source
	 *            the component that published the event (never {@code null})
	 */
	public AgentMappingsUpdateEvent(Object source) {
		super(source);
	}

}
