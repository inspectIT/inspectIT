package rocks.inspectit.agent.java.analyzer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.io.ByteStreams;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import rocks.inspectit.agent.java.analyzer.IByteCodeAnalyzer;
import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.config.impl.AbstractSensorConfig;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHookDispatcherMapper;
import rocks.inspectit.agent.java.instrumentation.InstrumenterFactory;
import rocks.inspectit.agent.java.instrumentation.asm.ClassAnalyzer;
import rocks.inspectit.agent.java.instrumentation.asm.ClassInstrumenter;
import rocks.inspectit.agent.java.instrumentation.asm.LoaderAwareClassWriter;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.stats.AgentStatisticsLogger;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AgentManagementErrorCodeEnum;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * {@link IByteCodeAnalyzer} that uses {@link IConnection} to connect to the CMR and send the
 * analyzed type. If needed performs instrumentation based on the result of the CMR answer.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ByteCodeAnalyzer implements IByteCodeAnalyzer, InitializingBean {

	/**
	 * Amount of milliseconds to wait for the result of the {@link AnalyzeCallable}.
	 */
	private static final int ANALYZE_TIMEOUT_MILLIS = 2000;

	/**
	 * Log for the class.
	 */
	@Log
	Logger log;

	/**
	 * Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * {@link IConfigurationStorage} for getting if exception sensor is active or not.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * {@link IConnection}.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * {@link IHookDispatcherMapper}.
	 */
	@Autowired
	private IHookDispatcherMapper hookDispatcherMapper;

	/**
	 * {@link IClassHashHelper}.
	 */
	@Autowired
	private ClassHashHelper classHashHelper;

	/**
	 * Core-service executor service.
	 */
	@Autowired
	@Qualifier("coreServiceExecutorService")
	private ExecutorService executorService;

	/**
	 * {@link InstrumenterFactory} needed for the instrumentation process.
	 */
	@Autowired
	private InstrumenterFactory instrumenterFactory;

	/**
	 * All initialized {@link IMethodSensor}s.
	 */
	@Autowired
	private List<IMethodSensor> methodSensors;

	/**
	 * Map of {@link IMethodSensor}s to their IDs for faster lookups.
	 */
	private Map<Long, IMethodSensor> methodSensorMap;

	/**
	 * Logs once, if the class cache is not available.
	 */
	@Autowired
	private AgentStatisticsLogger agentStatisticsLogger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] analyzeAndInstrument(byte[] byteCode, String className, final ClassLoader classLoader) {
		return analyzeAndInstrumentInternal(byteCode, className, classLoader, true);
	}

	/**
	 * Internal implementation of the {@link #analyzeAndInstrument(byte[], String, ClassLoader)}.
	 * Provides option to define if the instrumentation needs to be performed or not.
	 *
	 * @param byteCode
	 *            The byte-code of the class to analyze. If <code>null</code> is passed byte code
	 *            will be loaded using {@link #getByteCodeFromClassLoader(String, ClassLoader)}.
	 * @param className
	 *            The class name.
	 * @param classLoader
	 *            The class loader.
	 * @param performInstrumentation
	 *            If instrumentation should be performed with the instrumentation result sent by the
	 *            server.
	 * @return The instrumented byte code or <code>null</code> if instrumentation was not performed
	 *         (or in case of error).
	 */
	private byte[] analyzeAndInstrumentInternal(byte[] byteCode, String className, final ClassLoader classLoader, boolean performInstrumentation) {
		// clear any interrupted flag that might be there on the thread loading the class
		boolean isInterrupted = Thread.interrupted();
		try {
			if (null == byteCode) {
				// try to read from class loader, if it fails just return
				byteCode = getByteCodeFromClassLoader(className, classLoader);

				if (null == byteCode) {
					return null;
				}
			}

			// no matter what first register class being analyzed with class loader
			classHashHelper.registerAnalyzed(className);

			// create the hash
			String hash = DigestUtils.sha256Hex(byteCode);
			InstrumentationDefinition instrumentationResult = null;
			if (classHashHelper.isSent(className, hash)) {
				// if sent load instrumentation result from the class hash helper
				instrumentationResult = classHashHelper.getInstrumentationDefinition(className);
			} else {
				// if not sent we go for the sending
				if (!connection.isConnected()) {
					// we will not do anything else if there is no connection
					if (log.isDebugEnabled()) {
						log.debug("Not parsing and sending data for " + className + " as connection to server does not exist.");
					}
					return null;
				}

				// parse first, do not use internFQNs
				ClassReader classReader = new ClassReader(byteCode);
				ClassAnalyzer classAnalyzer = new ClassAnalyzer(hash);
				classReader.accept(classAnalyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				Type type = (Type) classAnalyzer.getType();

				// analyze all necessary depending classes before
				analyzeDependingTypes(type, classLoader);

				// try connecting to server
				Callable<InstrumentationDefinition> analyzeCallable = new AnalyzeCallable(connection, platformManager.getPlatformId(), hash, type);
				try {
					instrumentationResult = executorService.submit(analyzeCallable).get(ANALYZE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					isInterrupted = true;
					if (log.isWarnEnabled()) {
						log.warn("Error occurred instrumenting the byte code of class " + className + ". Thread loading the class was interrupted during communication with the server.", e);
					}
					return null;
				} catch (TimeoutException e) {
					if (log.isWarnEnabled()) {
						log.warn("Error occurred instrumenting the byte code of class " + className + ". Sending the class structure to the CMR resulted in a time-out.", e);
					}
					return null;
				}

				// register type as sent
				classHashHelper.registerSent(className, hash);
				classHashHelper.registerInstrumentationDefinition(className, instrumentationResult);
			}

			// execute instrumentation if needed
			if (performInstrumentation) {
				return performInstrumentation(byteCode, classLoader, instrumentationResult);
			} else {
				return null;
			}
		} catch (StorageException storageException) {
			log.error("Error occurred instrumenting the byte code of class " + className, storageException);
			return null;
		} catch (ExecutionException executionException) {
			if (executionException.getCause() instanceof BusinessException) {
				BusinessException businessException = (BusinessException) executionException.getCause();
				if (businessException.getErrorCode().equals(AgentManagementErrorCodeEnum.AGENT_DOES_NOT_EXIST)) {
					agentStatisticsLogger.noClassCacheAvailable();
				}
			} else {
				log.error("Error occurred instrumenting the byte code of class " + className, executionException);
			}
			return null;
		} finally {
			if (isInterrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Analyze the depending types of the given type and sends the results to the server if needed.
	 *
	 * @param type
	 *            {@link Type}
	 * @param classLoader
	 *            {@link ClassLoader} used for loading the given type.
	 */
	private void analyzeDependingTypes(Type type, ClassLoader classLoader) {
		Collection<Type> dependingTypes = type.getDependingTypes();
		if (CollectionUtils.isNotEmpty(dependingTypes)) {
			for (Type dependingType : dependingTypes) {
				if (!classHashHelper.isAnalyzed(dependingType.getFQN())) {
					analyzeAndInstrumentInternal(null, dependingType.getFQN(), classLoader, false);
				}
			}
		}
	}

	/**
	 * Performs the instrumentation. No instrumentation will be performed if instrumentation result
	 * is <code>null</code> or {@link InstrumentationDefinition#isEmpty()} returns <code>true</code>
	 * .
	 *
	 * @param byteCode
	 *            original byte code
	 * @param classLoader
	 *            class loader loading the class
	 * @param instrumentationResult
	 *            {@link InstrumentationDefinition} holding instrumentation properties.
	 * @return instrumented byte code or <code>null</code> if instrumentation result is
	 *         <code>null</code> or contains no instrumentation points
	 * @throws StorageException
	 *             If storage exception occurs when reading if enhanced exception sensor is active
	 */
	private byte[] performInstrumentation(byte[] byteCode, ClassLoader classLoader, InstrumentationDefinition instrumentationResult) throws StorageException {
		// if no instrumentation result or empty return null
		if ((null == instrumentationResult) || instrumentationResult.isEmpty()) {
			return null;
		}

		Collection<MethodInstrumentationConfig> instrumentationConfigs = instrumentationResult.getMethodInstrumentationConfigs();

		// here do the instrumentation
		ClassReader classReader = new ClassReader(byteCode);
		LoaderAwareClassWriter classWriter = new LoaderAwareClassWriter(classReader, ClassWriter.COMPUTE_FRAMES, classLoader);
		ClassInstrumenter classInstrumenter = new ClassInstrumenter(instrumenterFactory, classWriter, instrumentationConfigs, configurationStorage.isEnhancedExceptionSensorActivated());
		classReader.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

		// return changed byte code if we did actually add some byte code
		if (classInstrumenter.isByteCodeAdded()) {
			Map<Long, long[]> methodToSensorMap = new HashMap<Long, long[]>(0);

			// map the instrumentation points if we have them
			for (MethodInstrumentationConfig config : classInstrumenter.getAppliedInstrumentationConfigs()) {
				RegisteredSensorConfig registeredSensorConfig = createRegisteredSensorConfig(config);
				if (null != registeredSensorConfig) {
					SensorInstrumentationPoint sensorInstrumentationPoint = config.getSensorInstrumentationPoint();
					hookDispatcherMapper.addMapping(registeredSensorConfig.getId(), registeredSensorConfig);
					methodToSensorMap.put(Long.valueOf(registeredSensorConfig.getId()), sensorInstrumentationPoint.getSensorIds());
				}

				SpecialSensorConfig specialSensorConfig = createSpecialSensorConfig(config);
				if (null != specialSensorConfig) {
					SpecialInstrumentationPoint specialInstrumentationPoint = config.getSpecialInstrumentationPoint();
					hookDispatcherMapper.addMapping(specialSensorConfig.getId(), specialSensorConfig);
					methodToSensorMap.put(Long.valueOf(specialSensorConfig.getId()), new long[] { specialInstrumentationPoint.getSensorId() });
				}
			}

			// inform CMR of the applied instrumentation ids
			if (MapUtils.isNotEmpty(methodToSensorMap)) {
				executorService.submit(new InstrumentationAppliedRunnable(connection, platformManager.getPlatformId(), methodToSensorMap));
			}

			return classWriter.toByteArray();
		} else {
			return null;
		}
	}

	/**
	 * Creates the new {@link RegisteredSensorConfig} from the {@link MethodInstrumentationConfig}
	 * if the {@link SensorInstrumentationPoint} is defined in the configuration.
	 * <p>
	 * Data from the {@link SensorInstrumentationPoint} is copied to the
	 * {@link RegisteredSensorConfig} and method sensors are resolved and attached to the returned
	 * sensor configuration.
	 *
	 * @param config
	 *            {@link MethodInstrumentationConfig}
	 * @return {@link RegisteredSensorConfig} or <code>null</code> if this instrumentation config
	 *         does not defined the {@link SensorInstrumentationPoint}.
	 */
	private RegisteredSensorConfig createRegisteredSensorConfig(MethodInstrumentationConfig config) {
		SensorInstrumentationPoint sensorInstrumentationPoint = config.getSensorInstrumentationPoint();
		if (null == sensorInstrumentationPoint) {
			return null;
		}

		// copy properties
		RegisteredSensorConfig rsc = new RegisteredSensorConfig();
		this.copyInfo(rsc, config);
		rsc.setId(sensorInstrumentationPoint.getId());
		rsc.setStartsInvocation(sensorInstrumentationPoint.isStartsInvocation());
		rsc.setSettings(sensorInstrumentationPoint.getSettings());
		// accessor list must be thread safe
		if (CollectionUtils.isNotEmpty(sensorInstrumentationPoint.getPropertyAccessorList())) {
			rsc.setPropertyAccessorList(new CopyOnWriteArrayList<PropertyPathStart>(sensorInstrumentationPoint.getPropertyAccessorList()));
		} else {
			rsc.setPropertyAccessorList(Collections.<PropertyPathStart> emptyList());
		}

		// resolve sensors
		for (long sensorId : sensorInstrumentationPoint.getSensorIds()) {
			IMethodSensor sensor = methodSensorMap.get(sensorId);
			if (null != sensor) {
				rsc.addMethodSensor(sensor);
			} else {
				String methodFull = config.getTargetClassFqn() + "#" + config.getTargetMethodName();
				log.error("Sensor with the id " + sensorId + " does not exists on the agent, but it's defined for the method: " + methodFull);
			}
		}

		return rsc;
	}

	/**
	 * Creates the new {@link SpecialSensorConfig} from the {@link MethodInstrumentationConfig} if
	 * the {@link SpecialInstrumentationPoint} is defined in the configuration.
	 * <p>
	 * Data from the {@link SpecialSensorConfig} is copied to the {@link SpecialSensorConfig} and
	 * special method sensor is resolved and attached to the returned sensor configuration.
	 *
	 * @param config
	 *            {@link MethodInstrumentationConfig}
	 * @return {@link SpecialSensorConfig} or <code>null</code> if this instrumentation config does
	 *         not defined the {@link SpecialInstrumentationPoint}.
	 */
	private SpecialSensorConfig createSpecialSensorConfig(MethodInstrumentationConfig config) {
		SpecialInstrumentationPoint specialInstrumentationPoint = config.getSpecialInstrumentationPoint();
		if (null == specialInstrumentationPoint) {
			return null;
		}

		SpecialSensorConfig ssc = new SpecialSensorConfig();
		this.copyInfo(ssc, config);
		ssc.setId(specialInstrumentationPoint.getId());

		// resolve sensor
		long sensorId = specialInstrumentationPoint.getSensorId();
		IMethodSensor sensor = methodSensorMap.get(sensorId);
		if (null != sensor) {
			ssc.setSensor(sensor);
		} else {
			String methodFull = config.getTargetClassFqn() + "#" + config.getTargetMethodName();
			log.error("Sensor with the id " + sensorId + " does not exists on the agent, but it's defined for the method: " + methodFull);
		}
		return ssc;
	}

	/**
	 * Copies all the class/method information from the {@link MethodInstrumentationConfig} to the
	 * {@link AbstractSensorConfig}.
	 *
	 * @param asc
	 *            {@link AbstractSensorConfig}
	 * @param config
	 *            {@link MethodInstrumentationConfig}
	 */
	private void copyInfo(AbstractSensorConfig asc, MethodInstrumentationConfig config) {
		asc.setTargetClassFqn(config.getTargetClassFqn());
		asc.setTargetMethodName(config.getTargetMethodName());
		asc.setReturnType(config.getReturnType());
		asc.setParameterTypes(config.getParameterTypes());
	}

	/**
	 * Tries to read the byte code form the input stream provided by the given class loader. If the
	 * class loader is <code>null</code>, then {@link ClassLoader#getResourceAsStream(String)} will
	 * be called in order to find byte code.
	 * <p>
	 * This method returns provided byte code or <code>null</code> if reading was not successful.
	 *
	 * @param className
	 *            Class name defined by the class object.
	 * @param classLoader
	 *            Class loader loading the class.
	 * @return Byte code or <code>null</code> if reading was not successful
	 */
	private byte[] getByteCodeFromClassLoader(String className, ClassLoader classLoader) {
		InputStream is = null;
		try {
			if (null != classLoader) {
				is = classLoader.getResourceAsStream(className.replace('.', '/') + ".class");
			} else {
				is = ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class");
			}

			if (null == is) {
				// nothing we can do here
				return null;
			}

			return ByteStreams.toByteArray(is);
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("Can not load byte-code for the class " + className + " and class loader " + classLoader + ". Class will be ignored and not instrumented.", e);
			} else {
				log.info("Can not load byte-code for the class " + className + " and class loader " + classLoader + ". Class will be ignored and not instrumented.");
			}
			return null;
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) { // NOPMD //NOCHK
					// ignore
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		methodSensorMap = new HashMap<Long, IMethodSensor>();
		for (IMethodSensor methodSensor : methodSensors) {
			methodSensorMap.put(methodSensor.getSensorTypeConfig().getId(), methodSensor);
		}
	}

}
