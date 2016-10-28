package rocks.inspectit.server.event;

import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

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
	 * Deleted agent.
	 */
	private final PlatformIdent platformIdent;

	/**
	 * Default constructor for the event.
	 *
	 * @param source
	 *            event source
	 * @param platformIdent
	 *            deleted agent, must not be <code>null</code>
	 */
	public AgentRegisteredEvent(Object source, PlatformIdent platformIdent) {
		super(source);

		if (null == platformIdent) {
			throw new IllegalArgumentException("Agent can not be null.");
		}

		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #platformIdent}.
	 *
	 * @return {@link #platformIdent}
	 */
	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

}
