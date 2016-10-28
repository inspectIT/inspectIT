package rocks.inspectit.server.ci.event;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;

/**
 * Class representing the event when the instrumentation of classes have been changed.
 *
 * @author Marius Oehler
 *
 */
public class ClassInstrumentationChangedEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4071717195293420513L;

	/**
	 * Collection of changed instrumentationDefinitions.
	 */
	private final Collection<InstrumentationDefinition> instrumentationDefinitions;

	/**
	 * The id of the agent this event belongs to.
	 */
	private final long agentId;

	/**
	 * Default constructor for the event.
	 *
	 * @param source
	 *            event source
	 * @param agentId
	 *            id of the agent this event belongs to
	 * @param instrumentationDefinitions
	 *            updated instrumentationDefinitions
	 *
	 */
	public ClassInstrumentationChangedEvent(Object source, long agentId, Collection<InstrumentationDefinition> instrumentationDefinitions) {
		super(source);

		if (CollectionUtils.isEmpty(instrumentationDefinitions)) {
			throw new IllegalArgumentException("Given collection of instrumentation definitions may not be null or empty.");
		}

		this.agentId = agentId;
		this.instrumentationDefinitions = instrumentationDefinitions;
	}

	/**
	 * Gets {@link #agentId}.
	 *
	 * @return {@link #agentId}
	 */
	public long getAgentId() {
		return this.agentId;
	}

	/**
	 * Gets {@link #instrumentationDefinitions}.
	 *
	 * @return {@link #instrumentationDefinitions}
	 */
	public Collection<InstrumentationDefinition> getInstrumentationDefinitions() {
		return instrumentationDefinitions;
	}
}
