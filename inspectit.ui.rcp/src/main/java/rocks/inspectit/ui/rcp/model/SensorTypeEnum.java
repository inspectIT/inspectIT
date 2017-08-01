package rocks.inspectit.ui.rcp.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Image;

import rocks.inspectit.shared.all.cmr.model.MethodIdentToSensorType;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.cs.ci.sensor.exception.impl.ExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ConnectionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ExecutorClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.Log4jLoggingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteApacheHttpClientV40SensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteAsyncApacheHttpClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJavaHttpServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJettyHttpClientV61ClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJmsClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJmsListenerServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteManualServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteSpringRestTemplateClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteUrlConnectionClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.StatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ClassLoadingDelegationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.EUMInstrumentationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ExecutorIntercepterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.MBeanServerInterceptorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CompilationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CpuSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.MemorySensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.RuntimeSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.SystemSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ThreadSensorConfig;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;

/**
 * This enumeration holds all available sensor types with their full qualified name and their image.
 *
 * @author Patrice Bouillet
 * @author Stefan Siegl
 */
public enum SensorTypeEnum {
	/** The timer sensor type. */
	TIMER(TimerSensorConfig.CLASS_NAME, InspectITImages.IMG_TIMER),
	/** The average timer sensor type. hard-coded the sensor name as it will be removed in future */
	AVERAGE_TIMER("rocks.inspectit.agent.java.sensor.method.averagetimer.AverageTimerSensor", InspectITImages.IMG_TIMER),
	/** Charting with single timers. */
	CHARTING_TIMER(TimerSensorConfig.CLASS_NAME + "#charting", InspectITImages.IMG_TIMER),
	/** Charting with multiple timers. */
	CHARTING_MULTI_TIMER(TimerSensorConfig.CLASS_NAME + "#chartingMulti", InspectITImages.IMG_TIMER),
	/** The invocation sequence sensor type. */
	INVOCATION_SEQUENCE(InvocationSequenceSensorConfig.CLASS_NAME, InspectITImages.IMG_INVOCATION),
	/** The sql sensor type. */
	SQL("rocks.inspectit.agent.java.sensor.method.jdbc", InspectITImages.IMG_DATABASE),
	/** The jdbc connection sensor type. */
	JDBC_CONNECTION(ConnectionSensorConfig.CLASS_NAME, InspectITImages.IMG_DATABASE, false),
	/** The jdbc statement sensor type. */
	JDBC_STATEMENT(StatementSensorConfig.CLASS_NAME, InspectITImages.IMG_DATABASE, false),
	/** The jdbc prepared statement sensor type. */
	JDBC_PREPARED_STATEMENT(PreparedStatementSensorConfig.CLASS_NAME, InspectITImages.IMG_DATABASE, false),
	/** The jdbc prepared statement parameter sensor type. */
	JDBC_PREPARED_STATEMENT_PARAMETER(PreparedStatementParameterSensorConfig.CLASS_NAME, InspectITImages.IMG_DATABASE, false),
	/** The exception sensor. */
	EXCEPTION_SENSOR(ExceptionSensorConfig.CLASS_NAME, InspectITImages.IMG_EXCEPTION_SENSOR),
	/** The exception sensor overview. */
	EXCEPTION_SENSOR_GROUPED(ExceptionSensorConfig.CLASS_NAME + "#grouped", InspectITImages.IMG_EXCEPTION_SENSOR),
	/** The Http timer sensor type. */
	HTTP_TIMER_SENSOR(HttpSensorConfig.CLASS_NAME, InspectITImages.IMG_HTTP),
	/** The Http timer sensor type. */
	TAGGED_HTTP_TIMER_SENSOR(HttpSensorConfig.CLASS_NAME + "#tagged", InspectITImages.IMG_HTTP),
	/** The charting Http timer sensor type. */
	CHARTING_HTTP_TIMER_SENSOR(HttpSensorConfig.CLASS_NAME + "#charting", InspectITImages.IMG_HTTP),
	/** The classloading information sensor type. */
	CLASSLOADING_INFORMATION(ClassLoadingSensorConfig.CLASS_NAME, InspectITImages.IMG_CLASS_OVERVIEW),
	/** The compilation information sensor type. */
	COMPILATION_INFORMATION(CompilationSensorConfig.CLASS_NAME, null),
	/** The memory information sensor type. */
	MEMORY_INFORMATION(MemorySensorConfig.CLASS_NAME, InspectITImages.IMG_MEMORY_OVERVIEW),
	/** The cpu information sensor type. */
	CPU_INFORMATION(CpuSensorConfig.CLASS_NAME, InspectITImages.IMG_CPU_OVERVIEW),
	/** The runtime information sensor type. */
	RUNTIME_INFORMATION(RuntimeSensorConfig.CLASS_NAME, null),
	/** The system information sensor type. */
	SYSTEM_INFORMATION(SystemSensorConfig.CLASS_NAME, InspectITImages.IMG_SYSTEM_OVERVIEW),
	/** The thread information sensor type. */
	THREAD_INFORMATION(ThreadSensorConfig.CLASS_NAME, InspectITImages.IMG_THREADS_OVERVIEW),
	/** The navigation invocation sequence sensor type. */
	NAVIGATION_INVOCATION(InvocationSequenceSensorConfig.CLASS_NAME + "#navigation", InspectITImages.IMG_INVOCATION),
	/** The multi invocation timer data sensor type. */
	MULTI_INVOC_DATA(InvocationSequenceSensorConfig.CLASS_NAME + "#multi", InspectITImages.IMG_INVOCATION),
	/** Log4JLogging sensor. */
	LOG4J_LOGGING_DATA(Log4jLoggingSensorConfig.CLASS_NAME, InspectITImages.IMG_LOG),
	/** The JMX sensor data type. hard-coded until support for the JMX in CI */
	JMX_SENSOR_DATA("rocks.inspectit.agent.java.sensor.jmx.JmxSensor", InspectITImages.IMG_BEAN),
	/** The JMX sensor data type. hard-coded until support for the JMX in CI */
	CHARTING_JMX_SENSOR_DATA("rocks.inspectit.agent.java.sensor.jmx.JmxSensor#charting", InspectITImages.IMG_BEAN),
	/** Class loading delegation. */
	CLASS_LOADING_DELEGATION(ClassLoadingDelegationSensorConfig.CLASS_NAME, InspectITImages.IMG_EXTERNALIZE, false),
	/** MBean server interceptor. */
	MBEAN_SERVER_INTERCEPTOR(MBeanServerInterceptorSensorConfig.CLASS_NAME, InspectITImages.IMG_BEAN, false),
	/** The alert invocation sequence sensor type. */
	ALERT_INVOCATION(InvocationSequenceSensorConfig.CLASS_NAME + "#alert", InspectITImages.IMG_ALARM_INVOCATION),
	/** The Remote Apache sensor. */
	REMOTE_APACHE_HTTP_CLIENT_V40(RemoteApacheHttpClientV40SensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The Remote Async Apache sensor. */
	REMOTE_ASYNC_APACHE_HTTP(RemoteAsyncApacheHttpClientSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The Remote UrlConnection sensor. */
	REMOTE_HTTP_URL_CONNECTION(RemoteUrlConnectionClientSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The Remote UrlConnection sensor. */
	REMOTE_SPRING_REST_TEMPLATE_CLIENT(RemoteSpringRestTemplateClientSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The Remote Jetty sensor. */
	REMOTE_JETTY_HTTP_CLIENT_V61(RemoteJettyHttpClientV61ClientSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The Remote http client sensor. */
	REMOTE_JAVA_HTTP_SERVER(RemoteJavaHttpServerSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The Remote jms client sensor. */
	REMOTE_JMS_CLIENT(RemoteJmsClientSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The Remote jms listener sensor. */
	REMOTE_JMS_LISTENER(RemoteJmsListenerServerSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** The manual Remote server sensor. */
	REMOTE_MANUAL_SERVER(RemoteManualServerSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** Tracing view. */
	TRACING(AbstractRemoteSensorConfig.class.getName(), InspectITImages.IMG_REMOTE, false),
	/** Tracing details view. */
	TRACING_DETAILS(AbstractRemoteSensorConfig.class.getName() + "#deatils", InspectITImages.IMG_REMOTE, false),
	/** End User Monitoring. */
	END_USER_MONITORING(EUMInstrumentationSensorConfig.CLASS_NAME, InspectITImages.IMG_ASSIGNEE_LABEL_ICON, false),
	/** Special sensor for substituting {@link java.lang.Runnable}s for thread correlation. */
	EXECUTOR_INTERCEPTOR(ExecutorIntercepterSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false),
	/** Executor client sensor for correlating threads via executors. */
	EXECUTOR_CLIENT(ExecutorClientSensorConfig.CLASS_NAME, InspectITImages.IMG_REMOTE, false);

	/**
	 * The LOOKUP map which is used to get an element of the enumeration when passing the full
	 * qualified name.
	 */
	private static final Map<String, SensorTypeEnum> LOOKUP = new HashMap<>();

	static {
		for (SensorTypeEnum s : EnumSet.allOf(SensorTypeEnum.class)) {
			LOOKUP.put(s.getFqn(), s);
		}
	}

	/**
	 * The full qualified name string.
	 */
	private String fqn;

	/**
	 * The image descriptor.
	 */
	private Image image;

	/**
	 * Defines if this sensor type can be opened somehow. By default <b>true</b>.
	 */
	private boolean openable = true;

	/**
	 * Constructs an element of the enumeration with the given full qualified name string.
	 *
	 * @param fqn
	 *            The full qualified name.
	 * @param imageName
	 *            The name of the image. Names are defined in {@link InspectITImages}.
	 */
	SensorTypeEnum(String fqn, String imageName) {
		this.fqn = fqn;
		this.image = InspectIT.getDefault().getImage(imageName);
	}

	/**
	 * Same as standard constructor but the openable can be specified in addition.
	 *
	 * @param fqn
	 *            The full qualified name.
	 * @param imageName
	 *            The name of the image. Names are defined in {@link InspectITImages}.
	 * @param openable
	 *            Defines if this can be opened somehow in the UI.
	 */
	SensorTypeEnum(String fqn, String imageName, boolean openable) {
		this.fqn = fqn;
		this.image = InspectIT.getDefault().getImage(imageName);
		this.openable = openable;
	}

	/**
	 * The full qualified name of the sensor type.
	 *
	 * @return The full qualified name.
	 */
	public String getFqn() {
		return fqn;
	}

	/**
	 * Returns an element of the enumeration for the given full qualified name.
	 *
	 * @param fqn
	 *            The full qualified class name of the sensor type.
	 * @return An element of the enumeration.
	 */
	public static SensorTypeEnum get(String fqn) {
		return LOOKUP.get(fqn);
	}

	/**
	 * Returns all elements of the enumeration for the given list of {@link MethodIdentToSensorType}
	 * objects.
	 *
	 * @param methodIdentToSensorTypes
	 *            The passed {@link MethodIdentToSensorType} objects.
	 * @return A set of SensorTypeEnum objects.
	 */
	public static Set<SensorTypeEnum> getAllOf(Set<MethodIdentToSensorType> methodIdentToSensorTypes) {
		Set<SensorTypeEnum> sensorTypeSet = EnumSet.noneOf(SensorTypeEnum.class);
		for (MethodIdentToSensorType methodIdentToSensorType : methodIdentToSensorTypes) {
			SensorTypeIdent sensorType = methodIdentToSensorType.getMethodSensorTypeIdent();
			SensorTypeEnum sensorTypeEnum = get(sensorType.getFullyQualifiedClassName());
			if (null != sensorTypeEnum) {
				sensorTypeSet.add(sensorTypeEnum);
			} else {
				// This might happen if we realize a new sensor type and forget to add it to the
				// sensor type enum. We put this here as a failfast reminder.
				throw new RuntimeException("Lookup for the enum of sensor type " + sensorType.getFullyQualifiedClassName() + " fails");
			}
		}
		return sensorTypeSet;
	}

	/**
	 * Returns an image descriptor for this sensor type.
	 *
	 * @return The sensor type image descriptor.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Returns a displayable name of the sensor type.
	 *
	 * @return The displayable name.
	 */
	public String getDisplayName() {
		StringBuilder name = new StringBuilder(name().toLowerCase().replaceAll("_", " "));
		Character character = name.charAt(0);
		character = Character.toUpperCase(character);
		name.setCharAt(0, character);

		int i = 0;
		while (i >= 0) {
			i = name.indexOf(" ", i);
			if (i >= 0) {
				i = i + 1;
				character = Character.toUpperCase(name.charAt(i));
				name.setCharAt(i, character);
			}
		}

		return name.toString();
	}

	/**
	 * Returns if this sensor type can be opened somehow.
	 *
	 * @return if its openable.
	 */
	public boolean isOpenable() {
		return openable;
	}

}
