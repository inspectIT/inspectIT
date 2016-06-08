package rocks.inspectit.agent.java.sensor.method.remote.inserter.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sensor.method.remote.RemoteConstants;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteDefaultInserterHook;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.agent.java.util.ReflectionCache;
import rocks.inspectit.shared.all.communication.data.RemoteMQCallData;

/**
 * The hook implements the {@link RemoteDefaultInserterHook} class for Message Queue. It puts the
 * InspectIT header as additional header/attribute to the Message. The hook invokes the methode
 * {@link #METHOD_NAME} to add the header attribute.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteMQInserterHook extends RemoteDefaultInserterHook<RemoteMQCallData> {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteMQInserterHook.class);

	/**
	 * MQ specific method name to check if inspectIT header is already in place.
	 */
	private static final String METHOD_NAME_GET_OBJECT_PROPERTY = "getObjectProperty";

	/**
	 * MQ specific method name to add inspectIT header.
	 */
	private static final String METHOD_NAME = "setObjectProperty";

	/**
	 * MQ specific method name to get JMS Message ID.
	 */
	private static final String METHOD_NAME_GET_JMS_MESSAGE_ID = "getJMSMessageID";

	/**
	 * MQ specific method name to get JMS Destination.
	 */
	private static final String METHOD_NAME_GET_JMS_MESSAGE_DESTINATION = "getJMSDestination";

	/**
	 * MQ specific method name to get Topic Name.
	 */
	private static final String METHOD_NAME_GET_TOPIC_NAME = "getTopicName";

	/**
	 * MQ specific method name to get Queue Name.
	 */
	private static final String METHOD_NAME_GET_QUEUE_NAME = "getQueueName";

	/**
	 * MQ specific method signature to add InspechtITHeader.
	 */
	private static final Class<?>[] METHOD_PARAMETER = new Class<?>[] { String.class, Object.class };;;

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	public RemoteMQInserterHook(IPlatformManager platformManager, RemoteIdentificationManager remoteIdentificationManager) {
		super(platformManager, remoteIdentificationManager);
	}

	@Override
	protected boolean needToInsertInspectItHeader(Object object, Object[] parameters) {
		int parameterIndex = this.getIndexForMessage(parameters.length);

		if (parameterIndex >= 0) {
			Object message = parameters[parameterIndex];

			try {
				String inspectITHeader = (String) cache.invokeMethod(message.getClass(), METHOD_NAME_GET_OBJECT_PROPERTY, METHOD_PARAMETER_ONE_STRING_FIELD, message,
						new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER }, null);

				return inspectITHeader == null;

			} catch (Exception e) {
				LOG.warn("Check of InspectITHeader was not possible.", e);
				return true;
			}
		}

		return false;
	}

	@Override
	protected void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters) {
		long identification = remoteIdentificationManager.getNextIdentification();
		int parameterIndex = this.getIndexForMessage(parameters.length);

		String inspectItHeader = getInspectItHeader(identification);

		try {
			Object message = parameters[parameterIndex];
			cache.invokeMethod(message.getClass(), METHOD_NAME, METHOD_PARAMETER, message, new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER, inspectItHeader }, null);

			RemoteMQCallData remoteCallData = new RemoteMQCallData();
			remoteCallData.setIdentification(identification);
			remoteCallData.setRemotePlatformIdent(0);
			this.threadRemoteCallData.set(remoteCallData);

			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectITHeader inserted: " + inspectItHeader);
			}
		} catch (Exception e) {
			LOG.warn("Insertion of InspectITHeader was not possible. No Header Extention.", e);
		}

	}

	/**
	 *
	 * @param numberParameters
	 *            Number of Method Parameter.
	 * @return Index of parameter.
	 */
	private int getIndexForMessage(int numberParameters) {
		if (numberParameters == 1 || numberParameters == 4) {
			return 0;
		} else if (numberParameters == 2 || numberParameters == 5) {
			return 1;
		}

		LOG.warn("New send(*) Message in javax.jms.QueueSender is not supported.");

		return -1;
	}

	@Override
	protected void addRemoteSpecificData(Object object, Object[] parameters, Object result) {
		RemoteMQCallData data = this.threadRemoteCallData.get();
		Object message = parameters[this.getIndexForMessage(parameters.length)];
		data.setMessageDestination(this.readMessageDestination(message));
		data.setMessageId(this.readMessageID(message));
	}

	/**
	 * Read the JMS Message ID from the JMS Message.
	 *
	 * @param message
	 *            The JMS Message.
	 * @return JMS Message ID
	 */
	private String readMessageID(Object message) {
		String messageID = (String) cache.invokeMethod(message.getClass(), METHOD_NAME_GET_JMS_MESSAGE_ID, METHOD_PARAMETER_EMPTY, message, new Object[] {}, null);
		return messageID;
	}

	/**
	 * Read the JMS Message Destination. Destination could be a Topic or a Queue.
	 *
	 * @param message
	 *            The JMS Message.
	 * @return JMS Destination Name.
	 */
	private String readMessageDestination(Object message) {
		Object destination = cache.invokeMethod(message.getClass(), METHOD_NAME_GET_JMS_MESSAGE_DESTINATION, METHOD_PARAMETER_EMPTY, message, new Object[] {}, null);

		// destination can be a topic or a queue
		// try queue first
		Object nameObject = cache.invokeMethod(destination.getClass(), METHOD_NAME_GET_QUEUE_NAME, METHOD_PARAMETER_EMPTY, destination, new Object[] {}, null);
		// if null, try topic
		if (nameObject == null) {
			nameObject = cache.invokeMethod(destination.getClass(), METHOD_NAME_GET_TOPIC_NAME, METHOD_PARAMETER_EMPTY, destination, new Object[] {}, null);
		}

		if (nameObject != null) {
			return (String) nameObject;
		}
		return null;
	}

}
