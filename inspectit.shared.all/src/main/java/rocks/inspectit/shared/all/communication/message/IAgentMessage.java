package rocks.inspectit.shared.all.communication.message;

/**
 * Interface for all {@link IAgentMessage}s. This messages can be provided by the CMR for fetching
 * by the agent.
 *
 * @author Marius Oehler
 *
 * @param <T>
 *            Type of the message content.
 */
public interface IAgentMessage<T> {

	/**
	 * Returns the message's content.
	 *
	 * @return instance of {@link T}.
	 */
	T getMessageContent();
}
