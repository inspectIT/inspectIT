package rocks.inspectit.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event for signaling that agent has been deleted.
 *
 * @author Ivan Senic
 *
 */
public class AgentDeletedEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 1365478176949875717L;

	/**
	 * Id of deleted agent.
	 */
	private final long platformId;

	/**
	 * Default constructor for the event.
	 *
	 * @param source
	 *            event source
	 * @param platformId
	 *            id of deleted agent, must be greater than 0
	 */
	public AgentDeletedEvent(Object source, long platformId) {
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
