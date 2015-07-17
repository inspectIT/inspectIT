package info.novatec.inspectit.agent.sensor.method.invocationsequence;

import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IObjectStorage;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.core.ListListener;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.agent.sensor.exception.ExceptionSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementParameterSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementSensor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.util.StringConstraint;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.net.ConnectException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * The ID manager.
	 */
	private final IIdManager idManager;

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
	private Map<Long, Double> minDurationMap = new HashMap<Long, Double>();

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private StringConstraint strConstraint;

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
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param param
	 *            Additional parameters.
	 * @param enhancedExceptionSensor
	 *            If enhanced exception sensor is ON.
	 */
	public InvocationSequenceHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor, Map<String, Object> param, boolean enhancedExceptionSensor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
		this.strConstraint = new StringConstraint(param);
		this.enhancedExceptionSensor = enhancedExceptionSensor;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (skip(rsc)) {
			return;
		}

		try {
			long platformId = idManager.getPlatformId();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			long registeredMethodId = idManager.getRegisteredMethodId(methodId);

			if (null == threadLocalInvocationData.get()) {
				// save the start time
				timeStack.push(new Double(timer.getCurrentTime()));

				// the sensor type is only available in the beginning of the
				// sequence trace
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);

				// no invocation tracer is currently started, so we do that now.
				InvocationSequenceData invocationSequenceData = new InvocationSequenceData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
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

				InvocationSequenceData nestedInvocationSequenceData = new InvocationSequenceData(timestamp, platformId, invocationSequenceData.getSensorTypeIdent(), registeredMethodId);
				nestedInvocationSequenceData.setStart(timer.getCurrentTime());
				nestedInvocationSequenceData.setParentSequence(invocationSequenceData);

				invocationSequenceData.getNestedSequences().add(nestedInvocationSequenceData);

				threadLocalInvocationData.set(nestedInvocationSequenceData);
			}
		} catch (IdNotAvailableException idNotAvailableException) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not start invocation sequence because of a (currently) not mapped ID");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		if (skip(rsc)) {
			return;
		}

		InvocationSequenceData invocationSequenceData = threadLocalInvocationData.get();

		if (null != invocationSequenceData) {
			if (methodId == invocationStartId.get().longValue()) {
				long count = invocationStartIdCount.get().longValue();
				invocationStartIdCount.set(Long.valueOf(count - 1));

				if (0 == count - 1) {
					timeStack.push(new Double(timer.getCurrentTime()));
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		if (skip(rsc)) {
			return;
		}

		InvocationSequenceData invocationSequenceData = threadLocalInvocationData.get();

		if (null != invocationSequenceData) {
			// check if some properties need to be accessed and saved
			if (rsc.isPropertyAccess()) {
				List<ParameterContentData> parameterContentData = propertyAccessor.getParameterContentData(rsc.getPropertyAccessorList(), object, parameters, result);

				// crop the content strings of all ParameterContentData
				for (ParameterContentData contentData : parameterContentData) {
					contentData.setContent(strConstraint.crop(contentData.getContent()));
				}
			}

			if (methodId == invocationStartId.get().longValue() && 0 == invocationStartIdCount.get().longValue()) {
				double endTime = timeStack.pop().doubleValue();
				double startTime = timeStack.pop().doubleValue();
				double duration = endTime - startTime;

				// complete the sequence and store the data object in the 'true'
				// core service so that it can be transmitted to the server. we
				// just need an arbitrary prefix so that this sequence will
				// never be overwritten in the core service!
				if (minDurationMap.containsKey(invocationStartId.get())) {
					checkForSavingOrNot(coreService, methodId, sensorTypeId, rsc, invocationSequenceData, startTime, endTime, duration);
				} else {
					// maybe not saved yet in the map
					if (rsc.getSettings().containsKey("minduration")) {
						minDurationMap.put(invocationStartId.get(), Double.valueOf((String) rsc.getSettings().get("minduration")));
						checkForSavingOrNot(coreService, methodId, sensorTypeId, rsc, invocationSequenceData, startTime, endTime, duration);
					} else {
						invocationSequenceData.setDuration(duration);
						invocationSequenceData.setStart(startTime);
						invocationSequenceData.setEnd(endTime);
						coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.currentTimeMillis()), invocationSequenceData);
					}
				}

				threadLocalInvocationData.set(null);
			} else {
				// just close the nested sequence and set the correct child count
				InvocationSequenceData parentSequence = invocationSequenceData.getParentSequence();
				// check if we should not include this invocation because of exception delegation or
				// SQL wrapping
				if (removeDueToExceptionDelegation(rsc, invocationSequenceData) || removeDueToWrappedSqls(rsc, invocationSequenceData)) {
					parentSequence.getNestedSequences().remove(invocationSequenceData);
					parentSequence.setChildCount(parentSequence.getChildCount() - 1);
					// but connect all possible children to the parent then
					// we are eliminating one level here
					if (CollectionUtils.isNotEmpty(invocationSequenceData.getNestedSequences())) {
						parentSequence.getNestedSequences().addAll(invocationSequenceData.getNestedSequences());
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
		if (1 == rsc.getSensorTypeConfigs().size()) {
			MethodSensorTypeConfig methodSensorTypeConfig = rsc.getSensorTypeConfigs().get(0);

			if (ExceptionSensor.class.getCanonicalName().equals(methodSensorTypeConfig.getClassName())) {
				return CollectionUtils.isEmpty(invocationSequenceData.getExceptionSensorDataObjects());
			}
		}

		return false;
	}

	/**
	 * Returns if the given {@link InvocationSequenceData} should be removed due to the wrapping of
	 * the prepared SQL statements.
	 * 
	 * @param rsc
	 *            {@link RegisteredSensorConfig}
	 * @param invocationSequenceData
	 *            {@link InvocationSequenceData} to check.
	 * @return True if the invocation should be removed.
	 */
	private boolean removeDueToWrappedSqls(RegisteredSensorConfig rsc, InvocationSequenceData invocationSequenceData) {
		if (1 == rsc.getSensorTypeConfigs().size() || (2 == rsc.getSensorTypeConfigs().size() && enhancedExceptionSensor)) {
			for (MethodSensorTypeConfig methodSensorTypeConfig : rsc.getSensorTypeConfigs()) {

				if (PreparedStatementSensor.class.getCanonicalName().equals(methodSensorTypeConfig.getClassName())) {
					if (null == invocationSequenceData.getSqlStatementData() || 0 == invocationSequenceData.getSqlStatementData().getCount()) {
						return true;
					}
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
		if (1 == rsc.getSensorTypeConfigs().size() || (2 == rsc.getSensorTypeConfigs().size() && enhancedExceptionSensor)) {

			for (MethodSensorTypeConfig methodSensorTypeConfig : rsc.getSensorTypeConfigs()) {
				if (PreparedStatementParameterSensor.class.getCanonicalName().equals(methodSensorTypeConfig.getClassName())) {
					return true;
				}

				if (ConnectionSensor.class.getCanonicalName().equals(methodSensorTypeConfig.getClassName())) {
					return true;
				}

				if (ConnectionMetaDataSensor.class.getCanonicalName().equals(methodSensorTypeConfig.getClassName())) {
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
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
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
	private void checkForSavingOrNot(ICoreService coreService, long methodId, long sensorTypeId, RegisteredSensorConfig rsc, InvocationSequenceData invocationSequenceData, double startTime, // NOCHK
			double endTime, double duration) {
		double minduration = minDurationMap.get(invocationStartId.get()).doubleValue();
		if (duration >= minduration) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Saving invocation. " + duration + " > " + minduration + " ID(local): " + rsc.getId());
			}
			invocationSequenceData.setDuration(duration);
			invocationSequenceData.setStart(startTime);
			invocationSequenceData.setEnd(endTime);
			coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.currentTimeMillis()), invocationSequenceData);
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Not saving invocation. " + duration + " < " + minduration + " ID(local): " + rsc.getId());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc) {
		beforeBody(methodId, sensorTypeId, null, parameters, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		firstAfterBody(methodId, sensorTypeId, object, parameters, null, rsc);
		secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, null, rsc);
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
			if (null == invocationSequenceData.getTimerData() || invocationSequenceData.getTimerData().getClass().equals(TimerData.class)) {
				invocationSequenceData.setTimerData((HttpTimerData) dataObject);
			}
		}

		if (dataObject.getClass().equals(TimerData.class)) {
			// don't overwrite an already existing timerdata or httptimerdata object.
			if (null == invocationSequenceData.getTimerData()) {
				invocationSequenceData.setTimerData((TimerData) dataObject);
			}
		}

		if (dataObject.getClass().equals(ExceptionSensorData.class)) {
			ExceptionSensorData exceptionSensorData = (ExceptionSensorData) dataObject;
			invocationSequenceData.addExceptionSensorData(exceptionSensorData);
		}
	}

	// //////////////////////////////////////////////
	// All methods from the ICoreService are below //
	// //////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	public void addMethodSensorData(long sensorTypeId, long methodId, String prefix, MethodSensorData methodSensorData) {
		if (null == threadLocalInvocationData.get()) {
			LOG.error("thread data NULL!!!!");
			return;
		}
		saveDataObject(methodSensorData.finalizeData());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addObjectStorage(long sensorTypeId, long methodId, String prefix, IObjectStorage objectStorage) {
		if (null == threadLocalInvocationData.get()) {
			LOG.error("thread data NULL!!!!");
			return;
		}
		DefaultData defaultData = objectStorage.finalizeDataObject();
		saveDataObject(defaultData.finalizeData());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorData(long sensorTypeIdent, SystemSensorData systemSensorData) {
		saveDataObject(systemSensorData.finalizeData());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode, ExceptionSensorData exceptionSensorData) {
		if (null == threadLocalInvocationData.get()) {
			LOG.info("thread data NULL!!!!");
			return;
		}
		saveDataObject(exceptionSensorData.finalizeData());
	}

	// ///////////////////////////////////////////////// //
	// Return NULL because no saved data can be returned //
	// ///////////////////////////////////////////////// //

	/**
	 * {@inheritDoc}
	 */
	public ExceptionSensorData getExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodSensorData getMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IObjectStorage getObjectStorage(long sensorTypeIdent, long methodIdent, String prefix) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemSensorData getPlatformSensorData(long sensorTypeIdent) {
		return null;
	}

	// //////////////////////////////////////////////
	// All unsupported methods are below from here //
	// //////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	public void addListListener(ListListener<?> listener) {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSendStrategy(ISendingStrategy strategy) {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void connect() throws ConnectException {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListListener(ListListener<?> listener) {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendData() {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBufferStrategy(IBufferStrategy<DefaultData> bufferStrategy) {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void startSendingStrategies() {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorType(PlatformSensorTypeConfig platformSensorTypeConfig) {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		throw new UnsupportedMethodException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		throw new UnsupportedMethodException();
	}
}
