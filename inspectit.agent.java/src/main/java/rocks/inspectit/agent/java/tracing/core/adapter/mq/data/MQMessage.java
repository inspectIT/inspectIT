package rocks.inspectit.agent.java.tracing.core.adapter.mq.data;

import io.opentracing.propagation.TextMap;

/**
 * The interface to represent the MQ Message. Should be able to provide the message id and
 * destination, as well as implement the {@link TextMap} in order to propagate/get the tracing
 * information and baggage with/from the message.
 *
 * @author Ivan Senic
 *
 */
public interface MQMessage extends TextMap {

	/**
	 * Returns the message ID.
	 *
	 * @return Message ID.
	 */
	String getId();

	/**
	 * Returns the message destination. Destination could be a topic or a queue.
	 *
	 * @return Message destination
	 */
	String getDestination();

}
