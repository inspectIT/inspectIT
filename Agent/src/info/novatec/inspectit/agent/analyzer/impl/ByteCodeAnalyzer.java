package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IByteCodeAnalyzer;
import info.novatec.inspectit.agent.analyzer.IClassHashHelper;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IHookDispatcherMapper;
import info.novatec.inspectit.agent.instrumentation.asm.ClassAnalyzer;
import info.novatec.inspectit.agent.instrumentation.asm.ClassInstrumenter;
import info.novatec.inspectit.agent.instrumentation.asm.LoaderAwareClassWriter;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationDefinition;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.SensorInstrumentationPoint;
import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import info.novatec.inspectit.spring.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.io.ByteStreams;

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
	 * Log for the class.
	 */
	@Log
	Logger log;

	/**
	 * Id manager.
	 */
	@Autowired
	private IIdManager idManager;

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
	private IClassHashHelper classHashHelper;

	/**
	 * Core service needed for providing the executor service.
	 */
	@Autowired
	private ICoreService coreService;

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
	 * {@inheritDoc}
	 */
	public byte[] analyzeAndInstrument(byte[] byteCode, String className, final ClassLoader classLoader) {
		try {
			if (null == byteCode) {
				// try to read from class loader, if it fails just return
				byteCode = getByteCodeFromClassLoader(className, classLoader);

				if (null == byteCode) {
					return null;
				}
			}

			// create the hash
			String hash = DigestUtils.sha256Hex(byteCode);

			// TODO when asynch add registerLoaded to class cache helper

			InstrumentationDefinition instrumentationResult = null;
			if (classHashHelper.isSent(hash)) {
				// if sent load instrumentation result from the class hash helper
				instrumentationResult = classHashHelper.getInstrumentationResult(hash);
			} else {
				// if not sent we go for the sending
				// TODO when asynch call service in asynch mode
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

				// try connecting to server
				instrumentationResult = connection.analyze(idManager.getPlatformId(), hash, type);

				// register type as sent
				classHashHelper.register(hash, instrumentationResult);
			}

			// execute instrumentation if needed
			return performInstrumentation(byteCode, classLoader, instrumentationResult);

		} catch (IdNotAvailableException idNotAvailableException) {
			log.error("Error occurred instrumenting the byte code of class " + className, idNotAvailableException);
			return null;
		} catch (ServerUnavailableException serverUnavailableException) {
			log.error("Error occurred instrumenting the byte code of class " + className, serverUnavailableException);
			return null;
		} catch (BusinessException businessException) {
			log.error("Error occurred instrumenting the byte code of class " + className, businessException);
			return null;
		} catch (StorageException storageException) {
			log.error("Error occurred instrumenting the byte code of class " + className, storageException);
			return null;
		}
	}

	/**
	 * Performs the instrumentation. No instrumentation will be performed if instrumentation result
	 * is <code>null</code> or {@link InstrumentationDefinition#isEmpty()} returns <code>true</code>.
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
		if (null == instrumentationResult || instrumentationResult.isEmpty()) {
			return null;
		}

		Collection<MethodInstrumentationConfig> instrumentationConfigs = instrumentationResult.getMethodInstrumentationConfigs();

		// here do the instrumentation
		ClassReader classReader = new ClassReader(byteCode);
		LoaderAwareClassWriter classWriter = new LoaderAwareClassWriter(classReader, ClassWriter.COMPUTE_FRAMES, classLoader);
		ClassInstrumenter classInstrumenter = new ClassInstrumenter(classWriter, instrumentationConfigs);
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
			}

			// inform CMR of the applied instrumentation ids
			if (MapUtils.isNotEmpty(methodToSensorMap)) {
				coreService.getExecutorService().submit(new InstrumentationAppliedRunnable(methodToSensorMap));
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
		rsc.setTargetClassFqn(config.getTargetClassFqn());
		rsc.setTargetMethodName(config.getTargetMethodName());
		rsc.setReturnType(config.getReturnType());
		rsc.setParameterTypes(config.getParameterTypes());
		rsc.setId(sensorInstrumentationPoint.getId());
		rsc.setStartsInvocation(sensorInstrumentationPoint.isStartsInvocation());
		rsc.setSettings(sensorInstrumentationPoint.getSettings());
		rsc.setPropertyAccessorList(sensorInstrumentationPoint.getPropertyAccessorList());

		// resolve sensors
		for (long sensorId : sensorInstrumentationPoint.getSensorIds()) {
			IMethodSensor sensor = methodSensorMap.get(sensorId);
			if (null != sensor) {
				rsc.addMethodSensor(sensor);
			}
		}

		return rsc;
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
	public void afterPropertiesSet() throws Exception {
		methodSensorMap = new HashMap<Long, IMethodSensor>();
		for (IMethodSensor methodSensor : methodSensors) {
			methodSensorMap.put(methodSensor.getSensorTypeConfig().getId(), methodSensor);
		}
	}

	/**
	 * Private class for sending the applied instrumentation.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class InstrumentationAppliedRunnable implements Runnable {

		/**
		 * Map containing method id as key and applied sensor IDs.
		 */
		private final Map<Long, long[]> methodToSensorMap;

		/**
		 * @param methodToSensorMap
		 *            Map containing method id as key and applied sensor IDs.
		 */
		InstrumentationAppliedRunnable(Map<Long, long[]> methodToSensorMap) {
			this.methodToSensorMap = methodToSensorMap;
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			try {
				if (connection.isConnected()) {
					connection.instrumentationApplied(methodToSensorMap);
				}
			} catch (ServerUnavailableException e) {
				if (log.isDebugEnabled()) {
					if (e.isServerTimeout()) {
						log.debug("Instrumentations applied could not be sent to the CMR. Server timeout.", e);
					} else {
						log.debug("Instrumentations applied could not be sent to the CMR. Server not available.", e);
					}
				} else {
					log.info("Instrumentations applied could not be sent to the CMR due to the ServerUnavailableException.");
				}
			}
		}

	}

}
