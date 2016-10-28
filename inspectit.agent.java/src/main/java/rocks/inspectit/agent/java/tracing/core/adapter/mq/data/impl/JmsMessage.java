package rocks.inspectit.agent.java.tracing.core.adapter.mq.data.impl;

import rocks.inspectit.agent.java.tracing.core.adapter.mq.data.MQMessage;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The implementation of the {@link MQMessage} that works with {@link javax.jms.Message}.
 *
 * @author Ivan Senic
 *
 */
public class JmsMessage implements MQMessage {

	/**
	 * FQN of the javax.jms.Message.
	 */
	public static final String JAVAX_JMS_MESSAGE = "javax.jms.Message";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * JMS message, instance of javax.jms.Message.
	 */
	private final Object message;

	/**
	 * @param message
	 *            JMS message, instance of javax.jms.Message.
	 * @param cache
	 *            reflection cache to use
	 */
	public JmsMessage(Object message, ReflectionCache cache) {
		this.cache = cache;
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putBaggageItem(String key, String value) {
		cache.invokeMethod(message.getClass(), "setStringProperty", new Class<?>[] { String.class, String.class }, message, new Object[] { key, value }, null, JAVAX_JMS_MESSAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBaggageItem(String key) {
		return (String) cache.invokeMethod(message.getClass(), "getStringProperty", new Class<?>[] { String.class }, message, new Object[] { key }, null, JAVAX_JMS_MESSAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return (String) cache.invokeMethod(message.getClass(), "getJMSMessageID", new Class<?>[] {}, message, new Object[] {}, null, JAVAX_JMS_MESSAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDestination() {
		Object destination = cache.invokeMethod(message.getClass(), "getJMSDestination", new Class<?>[] {}, message, new Object[] {}, null, JAVAX_JMS_MESSAGE);
		if (null != destination) {
			// we know here it can be queue or topic, not sure how to handle this
			// for now we just use toString() method
			return destination.toString();
		} else {
			return null;
		}

	}

}
