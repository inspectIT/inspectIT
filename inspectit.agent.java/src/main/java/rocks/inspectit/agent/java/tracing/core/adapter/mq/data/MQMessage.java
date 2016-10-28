package rocks.inspectit.agent.java.tracing.core.adapter.mq.data;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;

/**
 * Our own interface to represent the Message Quege Message.
 *
 * @author Ivan Senic
 *
 */
public interface MQMessage extends BaggageInjectAdapter, BaggageExtractAdapter {

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
