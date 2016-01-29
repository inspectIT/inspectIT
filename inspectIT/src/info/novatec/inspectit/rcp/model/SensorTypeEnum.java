package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Image;

/**
 * This enumeration holds all available sensor types with their full qualified name and their image.
 * 
 * @author Patrice Bouillet
 * @author Stefan Siegl
 */
public enum SensorTypeEnum {
	/** The timer sensor type. */
	TIMER("info.novatec.inspectit.agent.sensor.method.timer.TimerSensor", InspectITImages.IMG_TIMER),
	/** The average timer sensor type. */
	AVERAGE_TIMER("info.novatec.inspectit.agent.sensor.method.averagetimer.AverageTimerSensor", InspectITImages.IMG_TIMER),
	/** Charting with single timers. */
	CHARTING_TIMER("info.novatec.inspectit.agent.sensor.method.averagetimer.ChartingTimer", InspectITImages.IMG_TIMER),
	/** Charting with multiple timers. */
	CHARTING_MULTI_TIMER("info.novatec.inspectit.agent.sensor.method.averagetimer.ChartingMultiTimer", InspectITImages.IMG_TIMER),
	/** The invocation sequence sensor type. */
	INVOCATION_SEQUENCE("info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceSensor", InspectITImages.IMG_INVOCATION),
	/** The sql sensor type. */
	SQL("info.novatec.inspectit.agent.sensor.method.jdbc.SQLTimerSensor", InspectITImages.IMG_DATABASE),
	/** The jdbc connection sensor type. */
	JDBC_CONNECTION("info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionSensor", InspectITImages.IMG_DATABASE, false),
	/** Meta data from the connection. */
	JDBC_CONNECTION_META_DATA("info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataSensor", InspectITImages.IMG_DATABASE, false),
	/** The jdbc statement sensor type. */
	JDBC_STATEMENT("info.novatec.inspectit.agent.sensor.method.jdbc.StatementSensor", InspectITImages.IMG_DATABASE, false),
	/** The jdbc prepared statement sensor type. */
	JDBC_PREPARED_STATEMENT("info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementSensor", InspectITImages.IMG_DATABASE, false),
	/** The jdbc prepared statement parameter sensor type. */
	JDBC_PREPARED_STATEMENT_PARAMETER("info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementParameterSensor", InspectITImages.IMG_DATABASE, false),
	/** The exception sensor. */
	EXCEPTION_SENSOR("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", InspectITImages.IMG_EXCEPTION_SENSOR),
	/** The exception sensor overview. */
	EXCEPTION_SENSOR_GROUPED("info.novatec.inspectit.agent.sensor.exception.ExceptionSensorOverview", InspectITImages.IMG_EXCEPTION_SENSOR),
	/** The combined metrics sensor type. */
	MARVIN_WORKFLOW("info.novatec.inspectit.agent.sensor.method.marvintimer.MarvinWorkflowSensor", InspectITImages.IMG_INVOCATION),
	/** The Http timer sensor type. */
	HTTP_TIMER_SENSOR("info.novatec.inspectit.agent.sensor.method.http.HttpSensor", InspectITImages.IMG_HTTP),
	/** The Http timer sensor type. */
	TAGGED_HTTP_TIMER_SENSOR("info.novatec.inspectit.agent.sensor.method.http.HttpSensor", InspectITImages.IMG_HTTP),
	/** The charting Http timer sensor type. */
	CHARTING_HTTP_TIMER_SENSOR("info.novatec.inspectit.agent.sensor.method.http.ChartingHttpSensor", InspectITImages.IMG_HTTP),
	/** The classloading information sensor type. */
	CLASSLOADING_INFORMATION("info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation", InspectITImages.IMG_CLASS_OVERVIEW),
	/** The compilation information sensor type. */
	COMPILATION_INFORMATION("info.novatec.inspectit.agent.sensor.platform.CompilationInformation", null),
	/** The memory information sensor type. */
	MEMORY_INFORMATION("info.novatec.inspectit.agent.sensor.platform.MemoryInformation", InspectITImages.IMG_MEMORY_OVERVIEW),
	/** The cpu information sensor type. */
	CPU_INFORMATION("info.novatec.inspectit.agent.sensor.platform.CpuInformation", InspectITImages.IMG_CPU_OVERVIEW),
	/** The runtime information sensor type. */
	RUNTIME_INFORMATION("info.novatec.inspectit.agent.sensor.platform.RuntimeInformation", null),
	/** The system information sensor type. */
	SYSTEM_INFORMATION("info.novatec.inspectit.agent.sensor.platform.SystemInformation", InspectITImages.IMG_SYSTEM_OVERVIEW),
	/** The thread information sensor type. */
	THREAD_INFORMATION("info.novatec.inspectit.agent.sensor.platform.ThreadInformation", InspectITImages.IMG_THREADS_OVERVIEW),
	/** The navigation invocation sequence sensor type. */
	NAVIGATION_INVOCATION("info.novatec.inspectit.agent.sensor.method.invocationsequence.NavigationInvocationSequenceSensor", InspectITImages.IMG_INVOCATION),
	/** The multi invocation timer data sensor type. */
	MULTI_INVOC_DATA("info.novatec.inspectit.agent.sensor.method.MultiInvocSensor", InspectITImages.IMG_INVOCATION),
	/** Log4JLogging sensor. */
	LOG4J_LOGGING_DATA("info.novatec.inspectit.agent.sensor.method.logging.Log4JLoggingSensor", InspectITImages.IMG_LOG),
	/** The JMX sensor data type. */
	JMX_SENSOR_DATA("info.novatec.inspectit.agent.sensor.jmx.JmxSensor", InspectITImages.IMG_BEAN),
	/** The JMX sensor data type. */
	CHARTING_JMX_SENSOR_DATA("info.novatec.inspectit.agent.sensor.jmx.JmxSensor", InspectITImages.IMG_BEAN);
	
	

	/**
	 * The LOOKUP map which is used to get an element of the enumeration when passing the full
	 * qualified name.
	 */
	private static final Map<String, SensorTypeEnum> LOOKUP = new HashMap<String, SensorTypeEnum>();

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
	private SensorTypeEnum(String fqn, String imageName) {
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
	private SensorTypeEnum(String fqn, String imageName, boolean openable) {
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
