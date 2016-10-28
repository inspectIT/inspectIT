package rocks.inspectit.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event for signaling that an agent has been registered.
 *
 * @author Marius Oehler
 *
 */
public class AgentRegisteredEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -1865902802663328684L;

	/**
	 * Id of the registered agent.
	 */
	private final long platformId;

	/**
	 * Default constructor for the event.
	 *
	 * @param source
	 *            event source
	 * @param platformId
	 *            id of registered agent, must be greater than 0
	 */
	public AgentRegisteredEvent(Object source, long platformId) {
		super(source);

		if (platformId <= 0) {
			throw new IllegalArgumentException("Agent ID has to be greater than 0.");
		}

		this.platformId = platformId;
	}

	/**
	 * Gets {@link #platformId}.
	 *
	 * @return {@link #platformId}
	 */
	public long getPlatformId() {
		return this.platformId;
	}
}
