package rocks.inspectit.agent.java.tracing.core.adapter.mq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.mq.data.MQMessage;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ReferenceType;

/**
 * The {@link ServerRequestAdapter} and {@link ClientRequestAdapter} for the MQ.
 *
 * @author Ivan Senic
 *
 */
public class MQRequestAdapter implements ServerRequestAdapter, ClientRequestAdapter {

	/**
	 * Message.
	 */
	private MQMessage message;

	/**
	 * Default constructor.
	 *
	 * @param message
	 *            Message
	 */
	public MQRequestAdapter(MQMessage message) {
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return PropagationType.JMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceType getReferenceType() {
		return ReferenceType.FOLLOW_FROM;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Tag, String> getTags() {
		String messageId = message.getId();
		String messageDestination = message.getDestination();

		if ((null == messageId) && (null == messageDestination)) {
			return Collections.emptyMap();
		}

		Map<Tag, String> tags = new HashMap<Tag, String>(2, 1f);
		if (null != messageId) {
			tags.put(Tag.Jms.MESSAGE_ID, messageId);
		}
		if (null != messageDestination) {
			tags.put(Tag.Jms.MESSAGE_DESTINATION, messageDestination);
		}
		return tags;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaggageInjectAdapter getBaggageInjectAdapter() {
		return message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaggageExtractAdapter getBaggageExtractAdapter() {
		return message;
	}

}
