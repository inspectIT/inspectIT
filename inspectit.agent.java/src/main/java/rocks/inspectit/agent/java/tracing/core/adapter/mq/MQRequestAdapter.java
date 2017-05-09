package rocks.inspectit.agent.java.tracing.core.adapter.mq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;
import rocks.inspectit.agent.java.tracing.core.adapter.mq.data.MQMessage;
import rocks.inspectit.agent.java.tracing.core.adapter.store.NoopSpanContextStore;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * The {@link ServerRequestAdapter} and {@link ClientRequestAdapter} for the MQ.
 *
 * @author Ivan Senic
 *
 */
public class MQRequestAdapter implements ServerRequestAdapter<TextMap>, ClientRequestAdapter<TextMap> {

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
	public boolean startClientSpan() {
		return true;
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
	public String getReferenceType() {
		return References.FOLLOWS_FROM;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTags() {
		String messageId = message.getId();
		String messageDestination = message.getDestination();

		if ((null == messageId) && (null == messageDestination)) {
			return Collections.emptyMap();
		}

		Map<String, String> tags = new HashMap<String, String>(2, 1f);
		if (null != messageId) {
			tags.put(ExtraTags.JMS_MESSAGE_ID, messageId);
		}
		if (null != messageDestination) {
			tags.put(ExtraTags.JMS_MESSAGE_DESTINATION, messageDestination);
		}
		return tags;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Format<TextMap> getFormat() {
		return Format.Builtin.TEXT_MAP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TextMap getCarrier() {
		return message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanContextStore getSpanContextStore() {
		return NoopSpanContextStore.INSTANCE;
	}
}
