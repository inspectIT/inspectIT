package rocks.inspectit.agent.java.sensor.method.invocationsequence;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IConstructorHook;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sensor.exception.ExceptionSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.sensor.method.jdbc.ConnectionSensor;
import rocks.inspectit.agent.java.sensor.method.jdbc.PreparedStatementParameterSensor;
import rocks.inspectit.agent.java.sensor.method.jdbc.PreparedStatementSensor;
import rocks.inspectit.agent.java.sensor.method.logging.Log4JLoggingSensor;
import rocks.inspectit.agent.java.sensor.method.remote.client.http.ApacheHttpClientV40Sensor;
import rocks.inspectit.agent.java.sensor.method.remote.client.http.JettyHttpClientV61Sensor;
import rocks.inspectit.agent.java.sensor.method.remote.client.http.SpringRestTemplateClientSensor;
import rocks.inspectit.agent.java.sensor.method.remote.client.http.UrlConnectionSensor;
import rocks.inspectit.agent.java.sensor.method.remote.client.mq.JmsRemoteClientSensor;
import rocks.inspectit.agent.java.sensor.method.remote.server.http.JavaHttpRemoteServerSensor;
import rocks.inspectit.agent.java.sensor.method.remote.server.manual.ManualRemoteServerSensor;
import rocks.inspectit.agent.java.sensor.method.remote.server.mq.JmsListenerRemoteServerSensor;
import rocks.inspectit.agent.java.tracing.core.transformer.SpanContextTransformer;
import rocks.inspectit.agent.java.util.StringConstraint;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.LoggingData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMData;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;

/**
 * The invocation sequence hook stores the record of the invocation sequences in a
 * {@link ThreadLocal} object.
 * <p>
 * This hook implements the {@link ICoreService} interface which simulates the core service to all
 * other hooks which are called during the execution of this invocation. The
 * <code>defaultCoreService</code> field is used to delegate some calls directly to the original
 * core service and later sending of the data to the server.
 *
 * @author Patrice Bouillet
 *
 */
public class InvocationSequenceHook implements IMethodHook, IConstructorHook, ICoreService {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(InvocationSequenceHook.class);

	/**
	 * List of remote sensor names needed for
	 * {@link #removeDueToNoData(RegisteredSensorConfig, InvocationSequenceData)}.
	 */
	private static final Set<String> REMOVE_SENSOR_CLASS_NAMES = new HashSet<String>(
			Arrays.asList(ApacheHttpClientV40Sensor.class.getName(), JettyHttpClientV61Sensor.class.getName(), SpringRestTemplateClientSensor.class.getName(), UrlConnectionSensor.class.getName(),
					JavaHttpRemoteServerSensor.class.getName(), JmsRemoteClientSensor.class.getName(), JmsListenerRemoteServerSensor.class.getName(), ManualRemoteServerSensor.class.getName()));

	/**
	 * The Platform manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * The real core service needed to delegate spans to.
	 */
	private final ICoreService realCoreService;

	/**
	 * Current tracer for the spans.
	 */
	private final TracerImpl tracer;

	/**
	 * The property accessor.
	 */
	private final IPropertyAccessor propertyAccessor;

	/**
	 * The {@link ThreadLocal} object which holds an {@link InvocationSequenceData} object if an
	 * invocation record is started.
	 */
	private final ThreadLocal<InvocationSequenceData> threadLocalInvocationData = new ThreadLocal<InvocationSequenceData>();

	/**
	 * Stores the value of the method ID in the {@link ThreadLocal} object. Used to identify the
	 * correct start and end of the record.
	 */
	private final ThreadLocal<Long> invocationStartId = new ThreadLocal<Long>();

	/**
	 * Stores the count of the of the starting method being called in the same invocation sequence
	 * so that closing is done on the right end.
	 */
	private final ThreadLocal<Long> invocationStartIdCount = new ThreadLocal<Long>();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * Saves the min duration for faster access of the values.
	 */
	private final Map<Long, Double> minDurationMap = new HashMap<Long, Double>();

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private final StringConstraint strConstraint;

	/**
	 * If enhanced exception sensor is ON.
	 */
	private final boolean enhancedExceptionSensor;

	/**
	 * The default constructor is initialized with a reference to the original {@link ICoreService}
	 * implementation to delegate all calls to if the data needs to be sent.
	 *
	 * @param timer
	 *            The timer.
	 * @param platformManager
	 *            The Platform manager.
	 * @param coreService
	 *            The real core service needed to delegate spans to.
	 * @param tracer
	 *            Current tracer for the spans.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param param
	 *            Additional parameters.
	 * @param enhancedExceptionSensor
	 *            If enhanced exception sensor is ON.
	 */
	public InvocationSequenceHook(Timer timer, IPlatformManager platformManager, ICoreService coreService, TracerImpl tracer, IPropertyAccessor propertyAccessor, Map<String, Object> param,
			boolean enhancedExceptionSensor) {
		this.timer = timer;
		this.platformManager = platformManager;
		this.realCoreService = coreService;
		this.tracer = tracer;
		this.propertyAccessor = propertyAccessor;
		this.strConstraint = new StringConstraint(param);
		this.enhancedExceptionSensor = enhancedExceptionSensor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (skip(rsc)) {
			return;
		}

		long platformId = platformManager.getPlatformId();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		if (null == threadLocalInvocationData.get()) {
			// the sensor type is only available in the beginning of the
			// sequence trace

			// save the start time
			timeStack.push(new Double(timer.getCurrentTime()));

			// no invocation tracer is currently started, so we do that now.
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(timestamp, platformId, sensorTypeId, methodId);
			threadLocalInvocationData.set(invocationSequenceData);

			invocationStartId.set(Long.valueOf(methodId));
			invocationStartIdCount.set(Long.valueOf(1));
		} else {
			if (methodId == invocationStartId.get().longValue()) {
				long count = invocationStartIdCount.get().longValue();
				invocationStartIdCount.set(Long.valueOf(count + 1));
			}
			// A subsequent call to the before body method where an
			// invocation tracer is already started.
			InvocationSequenceData invocationSequenceData = threadLocalInvocationData.get();
			invocationSequenceData.setChildCount(invocationSequenceData.getChildCount() + 1L);

			InvocationSequenceData nestedInvocationSequenceData = new InvocationSequenceData(timestamp, platformId, invocationSequenceData.getSensorTypeIdent(), methodId);
			nestedInvocationSequenceData.setStart(timer.getCurrentTime());
			nestedInvocationSequenceData.setParentSequence(invocationSequenceData);

			invocationSequenceData.getNestedSequences().add(nestedInvocationSequenceData);

			threadLocalInvocationData.set(nestedInvocationSequenceData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
		if (skip(rsc)) {
			return;
		}

		InvocationSequenceData invocationSequenceData = threadLocalInvocationData.get();

		if (null != invocationSequenceData) {
			if (methodId == invocationStartId.get().longValue()) {
				long count = invocationStartIdCount.get().longValue();
				invocationStartIdCount.set(Long.valueOf(count - 1));

				if (0 == (count - 1)) {
					timeStack.push(new Double(timer.getCurrentTime()));
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) { // NOCHK:8-params
		if (skip(rsc)) {
			return;
		}

		InvocationSequenceData invocationSequenceData = threadLocalInvocationData.get();

		if (null != invocationSequenceData) {
			// check if some properties need to be accessed and saved
			if (rsc.isPropertyAccess()) {
				List<ParameterContentData> parameterContentData = propertyAccessor.getParameterContentData(rsc.getPropertyAccessorList(), object, parameters, result, exception);

				// crop the content strings of all ParameterContentData
				for (ParameterContentData contentData : parameterContentData) {
					contentData.setContent(strConstraint.crop(contentData.getContent()));
				}
			}

			if ((methodId == invocationStartId.get().longValue()) && (0 == invocationStartIdCount.get().longValue())) {
				double endTime = timeStack.pop().doubleValue();
				double startTime = timeStack.pop().doubleValue();
				double duration = endTime - startTime;

				// check if we belong to a span
				if (tracer.isCurrentContextExisting()) {
					invocationSequenceData.setSpanIdent(SpanContextTransformer.transformSpanContext(tracer.getCurrentContext()));
				}

				// complete the sequence and store the data object in the 'true'
				// core service so that it can be transmitted to the server. we
				// just need an arbitrary prefix so that this sequence will
				// never be overwritten in the core service!
				if (minDurationMap.containsKey(invocationStartId.get())) {
					checkForSavingOrNot(coreService, rsc, invocationSequenceData, startTime, endTime, duration);
				} else {
					// maybe not saved yet in the map
					if (rsc.getSettings().containsKey("minduration")) {
						Long minDuration = (Long) rsc.getSettings().get("minduration");
						minDurationMap.put(invocationStartId.get(), minDuration.doubleValue());
						checkForSavingOrNot(coreService, rsc, invocationSequenceData, startTime, endTime, duration);
					} else {
						invocationSequenceData.setDuration(duration);
						invocationSequenceData.setStart(startTime);
						invocationSequenceData.setEnd(endTime);
						coreService.addDefaultData(invocationSequenceData);
					}
				}

				threadLocalInvocationData.set(null);
			} else {
				// check for the correct id we must be sure that
				// we are closing the right sequence
				if (methodId != invocationSequenceData.getMethodIdent()) {
					return;
				}

				// just close the nested sequence and set the correct child count
				InvocationSequenceData parentSequence = invocationSequenceData.getParentSequence();
				// check if we should not include this invocation because of exception delegation,
				// SQL wrapping or empty logging
				if (removeDueToExceptionDelegation(rsc, invocationSequenceData) || removeDueToNoData(rsc, invocationSequenceData)) {
					parentSequence.getNestedSequences().remove(invocationSequenceData);
					parentSequence.setChildCount(parentSequence.getChildCount() - 1);
					// but connect all possible children to the parent then we are eliminating one
					// level here
					if (CollectionUtils.isNotEmpty(invocationSequenceData.getNestedSequences())) {
						for (InvocationSequenceData child : invocationSequenceData.getNestedSequences()) {
							child.setParentSequence(parentSequence);
							parentSequence.getNestedSequences().add(child);
						}
						parentSequence.setChildCount(parentSequence.getChildCount() + invocationSequenceData.getChildCount());
					}
				} else {
					invocationSequenceData.setEnd(timer.getCurrentTime());
					invocationSequenceData.setDuration(invocationSequenceData.getEnd() - invocationSequenceData.getStart());
					parentSequence.setChildCount(parentSequence.getChildCount() + invocationSequenceData.getChildCount());
				}
				threadLocalInvocationData.set(parentSequence);
			}
		}
	}

	/**
	 * Returns if the given {@link InvocationSequenceData} should be removed due to the exception
	 * constructor delegation.
	 *
	 * @param rsc
	 *            {@link RegisteredSensorConfig}
	 * @param invocationSequenceData
	 *            {@link InvocationSequenceData} to check.
	 * @return True if the invocation should be removed.
	 */
	private boolean removeDueToExceptionDelegation(RegisteredSensorConfig rsc, InvocationSequenceData invocationSequenceData) {
		List<IMethodSensor> sensors = rsc.getMethodSensors();
		if (1 == sensors.size()) {
			MethodSensorTypeConfig methodSensorTypeConfig = sensors.get(0).getSensorTypeConfig();

			if (ExceptionSensor.class.getName().equals(methodSensorTypeConfig.getClassName())) {
				return CollectionUtils.isEmpty(invocationSequenceData.getExceptionSensorDataObjects());
			}
		}

		return false;
	}

	/**
	 * Returns if the given {@link InvocationSequenceData} should be removed due to no data. Can be
	 * in case of
	 * <ul>
	 * <li>the wrapping of the prepared SQL statements.
	 * <li>having an empty logging element (if the logging occurred with a lower logging level than
	 * the configuration)
	 * <li>running remote sensor that provided no span
	 * </ul>
	 *
	 * @param rsc
	 *            {@link RegisteredSensorConfig}
	 * @param invocationSequenceData
	 *            {@link InvocationSequenceData} to check.
	 * @return True if the invocation should be removed.
	 */
	private boolean removeDueToNoData(RegisteredSensorConfig rsc, InvocationSequenceData invocationSequenceData) {
		List<IMethodSensor> sensors = rsc.getMethodSensors();
		int sensorsSize = sensors.size();
		if ((1 == sensorsSize) || ((2 == sensorsSize) && enhancedExceptionSensor)) {
			for (IMethodSensor methodSensor : sensors) {
				String className = methodSensor.getSensorTypeConfig().getClassName();
				// check if class name is null, return then nothing to check
				if (null == className) {
					return false;
				}

				if (PreparedStatementSensor.class.getName().equals(className)) {
					if ((null == invocationSequenceData.getSqlStatementData()) || (0 == invocationSequenceData.getSqlStatementData().getCount())) {
						return true;
					}
				} else if (Log4JLoggingSensor.class.getName().equals(className)) {
					return null == invocationSequenceData.getLoggingData();
				} else if (REMOVE_SENSOR_CLASS_NAMES.contains(className)) {
					return null == invocationSequenceData.getSpanIdent();
				}
			}
		}

		return false;
	}

	/**
	 * Defines if the invocation container should skip the creation and processing of the invocation
	 * for the given object and {@link RegisteredSensorConfig}. We will skip if any of following
	 * conditions are met:
	 * <ul>
	 * <li>{@link RegisteredSensorConfig} has only exception sensor and object class does not match
	 * the target class name.
	 * <li>{@link RegisteredSensorConfig} has only prepared statement parameter sensor.
	 * <li>{@link RegisteredSensorConfig} has only connection sensor.
	 * <li>{@link RegisteredSensorConfig} has only connection meta data sensor.
	 * </ul>
	 *
	 * @param rsc
	 *            {@link RegisteredSensorConfig}.
	 *
	 * @return Return <code>true</code> if hook should skip creation and processing, false
	 *         otherwise.
	 */
	private boolean skip(RegisteredSensorConfig rsc) {
		List<IMethodSensor> sensors = rsc.getMethodSensors();
		if ((1 == sensors.size()) || ((2 == sensors.size()) && enhancedExceptionSensor)) {
			for (IMethodSensor methodSensor : sensors) {
				MethodSensorTypeConfig methodSensorTypeConfig = methodSensor.getSensorTypeConfig();
				if (PreparedStatementParameterSensor.class.getName().equals(methodSensorTypeConfig.getClassName())) {
					return true;
				}

				if (ConnectionSensor.class.getName().equals(methodSensorTypeConfig.getClassName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This checks if the invocation has to be saved or not (like the min duration is set and the
	 * invocation is faster than the specified time).
	 *
	 * @param coreService
	 *            The reference to the core service which holds the data objects etc.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the information of the
	 *            executed method.
	 * @param invocationSequenceData
	 *            The invocation sequence data object.
	 * @param startTime
	 *            The start time.
	 * @param endTime
	 *            The end time.
	 * @param duration
	 *            The actual duration.
	 */
	private void checkForSavingOrNot(ICoreService coreService, RegisteredSensorConfig rsc, InvocationSequenceData invocationSequenceData, double startTime, double endTime, double duration) {
		double minduration = minDurationMap.get(invocationStartId.get()).doubleValue();
		if (duration >= minduration) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Saving invocation. " + duration + " > " + minduration + " ID(local): " + rsc.getId());
			}
			invocationSequenceData.setDuration(duration);
			invocationSequenceData.setStart(startTime);
			invocationSequenceData.setEnd(endTime);
			coreService.addDefaultData(invocationSequenceData);
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Not saving invocation. " + duration + " < " + minduration + " ID(local): " + rsc.getId());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc) {
		beforeBody(methodId, sensorTypeId, null, parameters, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		firstAfterBody(methodId, sensorTypeId, object, parameters, null, false, rsc);
		secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, null, false, rsc);
	}

	/**
	 * Save the data objects which are coming from all the different sensor types in the current
	 * invocation tracer context.
	 *
	 * @param dataObject
	 *            The data object to save.
	 */
	private void saveDataObject(DefaultData dataObject) {
		InvocationSequenceData invocationSequenceData = threadLocalInvocationData.get();

		if (dataObject.getClass().equals(SqlStatementData.class)) {
			// don't overwrite an already existing sql statement data object.
			if (null == invocationSequenceData.getSqlStatementData()) {
				invocationSequenceData.setSqlStatementData((SqlStatementData) dataObject);
			}
		}

		if (dataObject.getClass().equals(HttpTimerData.class)) {
			// don't overwrite ourself but overwrite timers
			if ((null == invocationSequenceData.getTimerData()) || invocationSequenceData.getTimerData().getClass().equals(TimerData.class)) {
				invocationSequenceData.setTimerData((HttpTimerData) dataObject);
			}
		}

		if (dataObject.getClass().equals(TimerData.class)) {
			// don't overwrite an already existing timerdata or httptimerdata
			// object.
			if (null == invocationSequenceData.getTimerData()) {
				invocationSequenceData.setTimerData((TimerData) dataObject);
			}
		}

		if (dataObject.getClass().equals(ExceptionSensorData.class)) {
			ExceptionSensorData exceptionSensorData = (ExceptionSensorData) dataObject;
			invocationSequenceData.addExceptionSensorData(exceptionSensorData);
		}

		if (dataObject.getClass().equals(LoggingData.class)) {
			LoggingData loggingData = (LoggingData) dataObject;
			invocationSequenceData.setLoggingData(loggingData);
		}

		if (AbstractSpan.class.isAssignableFrom(dataObject.getClass())) {
			AbstractSpan span = (AbstractSpan) dataObject;
			invocationSequenceData.setSpanIdent(span.getSpanIdent());
		}
	}

	// //////////////////////////////////////////////
	// All methods from the ICoreService are below //
	// //////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDefaultData(DefaultData defaultData) {
		if (null == threadLocalInvocationData.get()) {
			LOG.error("thread data NULL!!!!");
			return;
		}

		// delegate to real core service in case of the span
		if (AbstractSpan.class.isAssignableFrom(defaultData.getClass())) {
			realCoreService.addDefaultData(defaultData);
		}

		saveDataObject(defaultData);
	}

	// //////////////////////////////////////////////
	// All unsupported methods are below from here //
	// //////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEUMData(AbstractEUMData eumData) {
		throw new UnsupportedMethodException();
	}
}
