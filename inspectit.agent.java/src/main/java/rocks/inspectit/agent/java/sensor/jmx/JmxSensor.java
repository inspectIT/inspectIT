package rocks.inspectit.agent.java.sensor.jmx;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * The implementation of the JmxSensor.
 *
 * @author Alfred Krauss
 * @author Marius Oehler
 * @author Ivan Senic
 *
 */
public class JmxSensor implements IJmxSensor, InitializingBean, DisposableBean {

	/**
	 * Name of the MBeanServerDelegate to register as listener for the {@link NotificationListener}.
	 */
	private static final String MBEAN_SERVER_DELEGATE_NAME = "JMImplementation:type=MBeanServerDelegate";

	/**
	 * Defines the interval of the maximum call rate of the
	 * {@link JmxSensor#collectData(ICoreService, long)} method.
	 */
	private static final int DATA_COLLECT_INTERVAL = 5000;

	/**
	 * Notification filter that listeners only to the MBeanServerNotification events.
	 */
	private static final NotificationFilter NOTIFICATION_FILTER = new NotificationFilter() {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = 3300812282583960575L;

		@Override
		public boolean isNotificationEnabled(Notification notification) {
			String type = notification.getType();
			return MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type) || MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type);
		}
	};

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The instance of the configuration storage.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * The Platform manager used to get the correct platform ID.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * Connection.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * Sensor configuration.
	 */
	private JmxSensorTypeConfig sensorTypeConfig;

	/**
	 * Set of active MBeanServerHolders.
	 */
	private final Map<MBeanServer, MBeanServerHolder> activeServerMap = new ConcurrentHashMap<MBeanServer, MBeanServerHolder>();

	/**
	 * The timestamp of the last {@link #collectData(ICoreService, long)} method invocation.
	 */
	long lastDataCollectionTimestamp = 0;

	/**
	 * {@inheritDoc}
	 */
	public void init(JmxSensorTypeConfig sensorTypeConfig) {
		this.sensorTypeConfig = sensorTypeConfig;

		// check for forcing server creation
		Map<String, Object> parameters = sensorTypeConfig.getParameters();
		if (MapUtils.isNotEmpty(parameters)) {
			if (Boolean.TRUE.equals(parameters.get("forceMBeanServer"))) {
				// create only, get it via hook
				MBeanServer server = ManagementFactory.getPlatformMBeanServer();
				mbeanServerAdded(server);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mbeanServerAdded(MBeanServer server) {
		// if null or already registered do nothing
		if ((null == server) || activeServerMap.containsKey(server)) {
			return;
		}

		try {
			// create holder
			MBeanServerHolder holder = new MBeanServerHolder(server);

			// register listener
			try {
				server.addNotificationListener(new ObjectName(MBEAN_SERVER_DELEGATE_NAME), holder, NOTIFICATION_FILTER, null);
			} catch (Exception e) {
				log.warn("Failed to add notification listener to the MBean server " + server.toString() + ". New added beans/attributes will not be monitored.", e);
			}

			// register already existing beans
			registerMBeans(holder, null);

			// add to map if it all works
			activeServerMap.put(server, holder);
		} catch (Throwable t) { // NOPMD
			// catching throwable if anything goes wrong
			log.warn("Unable to add the MBean server " + server.toString() + ".", t);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mbeanServerRemoved(MBeanServer server) {
		// if null do nothing
		if (null == server) {
			return;
		}

		try {
			// remove holder
			MBeanServerHolder holder = activeServerMap.remove(server);
			if (null == holder) {
				return;
			}

			// un-register listener
			try {
				server.removeNotificationListener(new ObjectName(MBEAN_SERVER_DELEGATE_NAME), holder, NOTIFICATION_FILTER, null);
			} catch (Exception e) {
				log.warn("Failed to remove notification listener to the MBean server " + server.toString() + ".", e);
			}
		} catch (Throwable t) { // NOPMD
			// catching throwable if anything goes wrong
			log.warn("Unable to remove the MBean server " + server.toString() + ".", t);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JmxSensorTypeConfig getSensorTypeConfig() {
		return sensorTypeConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(ICoreService coreService) {
		long sensorTypeIdent = sensorTypeConfig.getId();
		long currentTime = System.currentTimeMillis();

		// Check if the collectData method should be invoked
		if (MapUtils.isNotEmpty(activeServerMap) && ((currentTime - lastDataCollectionTimestamp) > DATA_COLLECT_INTERVAL)) {
			// store the invocation timestamp
			lastDataCollectionTimestamp = System.currentTimeMillis();

			for (MBeanServerHolder holder : activeServerMap.values()) {
				collectData(holder, coreService, sensorTypeIdent);
			}
		}
	}

	/**
	 * Collects the data from the MBean server in the holder and sends it to the CMR.
	 *
	 * @param holder
	 *            {@link MBeanServerHolder} to collect data from
	 * @param coreService
	 *            The core service which is needed to store the measurements to.
	 * @param sensorTypeIdent
	 *            The ID of the sensor type so that old data can be found. (for aggregating etc.)
	 */
	private void collectData(MBeanServerHolder holder, ICoreService coreService, long sensorTypeIdent) {
		MBeanServer mBeanServer = holder.mBeanServer;
		Map<JmxAttributeDescriptor, Boolean> activeAttributes = holder.activeAttributes;
		Map<String, ObjectName> nameStringToObjectName = holder.nameStringToObjectName;

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
		for (Iterator<JmxAttributeDescriptor> iterator = activeAttributes.keySet().iterator(); iterator.hasNext();) {
			JmxAttributeDescriptor descriptor = iterator.next();

			try {
				// Retrieving the value of the, in the JmxAttributeDescriptor specified,
				// MBeanAttribute
				ObjectName objectName = nameStringToObjectName.get(descriptor.getmBeanObjectName());
				Object collectedValue = mBeanServer.getAttribute(objectName, descriptor.getAttributeName());

				String value;
				if (null == collectedValue) {
					value = "null";
				} else if (collectedValue.getClass().isArray()) {
					value = getArrayValue(collectedValue);
				} else {
					value = collectedValue.toString();
				}

				// Create a new JmxSensorValueData to be saved into the database
				long platformid = platformManager.getPlatformId();
				JmxSensorValueData jsvd = new JmxSensorValueData(descriptor.getId(), value, timestamp, platformid, sensorTypeIdent);

				coreService.addDefaultData(jsvd);
			} catch (AttributeNotFoundException e) {
				iterator.remove();
				log.warn("JMX::AttributeNotFound. Attribute was not found. Maybe currently not available on the server. Attribute removed from the actively read list.", e);
			} catch (InstanceNotFoundException e) {
				iterator.remove();
				log.warn("JMX::Instance not found. MBean may not be registered on the Server. Attribute removed from the actively read list.", e);
			} catch (MBeanException e) {
				iterator.remove();
				log.warn("JMX::MBean. Undefined problem with the MBean. Attribute removed from the actively read list.", e);
			} catch (ReflectionException e) {
				iterator.remove();
				log.warn("JMX::Reflection error. MBean may not be registered on the Server. Attribute removed from the actively read list.", e);
			} catch (RuntimeMBeanException e) {
				iterator.remove();
				log.warn("JMX::Runtime error reading the attribute " + descriptor.getAttributeName() + " from the MBean " + descriptor.getmBeanObjectName()
				+ ". Attribute removed from the actively read list.", e);
			}
		}
	}

	/**
	 * Registers all attributes of all object names that are returned as the result of querying with
	 * the given mBeanName on the server in the given holder.
	 *
	 * @param holder
	 *            {@link MBeanServerHolder} instance to register beans for
	 * @param mBeanName
	 *            Object name to be used in the query. Use <code>null</code> to include all MBeans.
	 */
	private void registerMBeans(MBeanServerHolder holder, ObjectName mBeanName) {
		MBeanServer mBeanServer = holder.mBeanServer;
		Map<JmxAttributeDescriptor, Boolean> activeAttributes = holder.activeAttributes;
		Map<String, ObjectName> nameStringToObjectName = holder.nameStringToObjectName;

		// do nothing if connection is not there
		if (!connection.isConnected()) {
			return;
		}

		List<JmxAttributeDescriptor> descriptors = new ArrayList<JmxAttributeDescriptor>();

		// query for all names using null null
		Set<ObjectName> allNames = mBeanServer.queryNames(mBeanName, null);
		for (ObjectName objectName : allNames) {
			// collect all attributes and send to the server
			try {
				MBeanAttributeInfo[] attributeInfos = mBeanServer.getMBeanInfo(objectName).getAttributes();
				for (MBeanAttributeInfo mBeanAttributeInfo : attributeInfos) {
					JmxAttributeDescriptor descriptor = new JmxAttributeDescriptor();
					descriptor.setmBeanObjectName(objectName.toString());
					descriptor.setAttributeName(mBeanAttributeInfo.getName());
					descriptor.setmBeanAttributeDescription(mBeanAttributeInfo.getDescription());
					descriptor.setmBeanAttributeIsIs(mBeanAttributeInfo.isIs());
					descriptor.setmBeanAttributeIsReadable(mBeanAttributeInfo.isReadable());
					descriptor.setmBeanAttributeIsWritable(mBeanAttributeInfo.isWritable());
					descriptor.setmBeanAttributeType(mBeanAttributeInfo.getType());
					descriptors.add(descriptor);
				}
			} catch (IntrospectionException e) {
				continue;
			} catch (InstanceNotFoundException e) {
				continue;
			} catch (ReflectionException e) {
				continue;
			}
		}

		try {
			Collection<JmxAttributeDescriptor> toMonitor = connection.analyzeJmxAttributes(platformManager.getPlatformId(), descriptors);

			// add to active attributes
			for (JmxAttributeDescriptor descriptor : toMonitor) {
				activeAttributes.put(descriptor, Boolean.FALSE);
			}
			// if call is working add object names to the map
			for (ObjectName name : allNames) {
				nameStringToObjectName.put(name.toString(), name);
			}
		} catch (ServerUnavailableException e) {
			if (log.isWarnEnabled()) {
				log.warn("Error registering JMX attributes on the server.", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		for (JmxSensorTypeConfig config : configurationStorage.getJmxSensorTypes()) {
			if (config.getClassName().equals(this.getClass().getName())) {
				this.init(config);
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() throws Exception {
		// remove listeners
		if (MapUtils.isNotEmpty(activeServerMap)) {
			for (MBeanServerHolder holder : activeServerMap.values()) {
				holder.mBeanServer.removeNotificationListener(new ObjectName(MBEAN_SERVER_DELEGATE_NAME), holder, NOTIFICATION_FILTER, null);
			}
		}
	}

	/**
	 * Correctly handles printing of the array no matter if the array class is primitive or not.
	 *
	 * @param collectedArray
	 *            collected array
	 * @return Value for the array or empty string if given object is <code>null</code> or not an
	 *         array.
	 */
	private String getArrayValue(Object collectedArray) {
		if ((null != collectedArray) && collectedArray.getClass().isArray()) {
			StringBuilder sb = new StringBuilder("[");
			int length = Array.getLength(collectedArray);
			for (int i = 0; i < length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(Array.get(collectedArray, i));
			}
			sb.append(']');
			return sb.toString();
		}
		return "";
	}

	/**
	 * Holder to keep needed information per server.
	 *
	 * @author Ivan Senic
	 *
	 */
	final class MBeanServerHolder implements NotificationListener {

		/**
		 * The MBeanServer providing information about registered MBeans.
		 */
		final MBeanServer mBeanServer;

		/**
		 * Map used to connect the ObjectName of a MBean with the string-representation of the same
		 * MBean. Recreation of the ObjectName is no longer necessary for the update-method.
		 */
		final Map<String, ObjectName> nameStringToObjectName = new ConcurrentHashMap<String, ObjectName>();

		/**
		 * Set of active attributes (represented as Map).
		 */
		final Map<JmxAttributeDescriptor, Boolean> activeAttributes = new ConcurrentHashMap<JmxAttributeDescriptor, Boolean>();

		/**
		 * Default constructor.
		 *
		 * @param mBeanServer
		 *            Server to store in this holder. Can not be <code>null</code>.
		 */
		MBeanServerHolder(MBeanServer mBeanServer) {
			if (mBeanServer == null) {
				throw new IllegalArgumentException("MBean server can not be null.");
			}

			this.mBeanServer = mBeanServer;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleNotification(Notification notification, Object handback) {
			if (notification instanceof MBeanServerNotification) {
				MBeanServerNotification serverNotification = (MBeanServerNotification) notification;
				ObjectName mBeanName = serverNotification.getMBeanName();
				if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(serverNotification.getType())) {
					// if we have registration pick up the attributes
					registerMBeans(MBeanServerHolder.this, mBeanName);
				} else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(serverNotification.getType())) {
					// get maps from holder
					// if we have un-registration remove from maps
					String mBeanNameString = mBeanName.toString();
					for (Iterator<JmxAttributeDescriptor> it = activeAttributes.keySet().iterator(); it.hasNext();) {
						JmxAttributeDescriptor descriptor = it.next();
						if (Objects.equal(descriptor.getmBeanObjectName(), mBeanNameString)) {
							it.remove();
						}
					}
					nameStringToObjectName.remove(mBeanNameString);
				}
			}
		}

	}

}