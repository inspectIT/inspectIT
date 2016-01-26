/**
 *
 */
package info.novatec.inspectit.cmr.ci.event;

import org.springframework.context.ApplicationEvent;

/**
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
	 *            he component that published the event (never {@code null})
	 */
	public AgentMappingsUpdateEvent(Object source) {
		super(source);
	}

}
