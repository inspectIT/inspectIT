package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IByteCodeAnalyzer;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPathStart;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.hooking.IHookInstrumenter;
import info.novatec.inspectit.agent.hooking.impl.HookException;
import info.novatec.inspectit.communication.data.ParameterContentType;
import info.novatec.inspectit.spring.logger.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The default implementation of the {@link IByteCodeAnalyzer} interface. First it tries to analyze
 * the given byte code and collects all the methods which need to be instrumented in a Map. This is
 * done in the {@link #analyze(byte[], String, ClassLoader)} method. Afterwards, the Map is passed
 * to the {@link #instrument(Map)} method which will do the instrumentation.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
@Component
public class ByteCodeAnalyzer implements IByteCodeAnalyzer {

	/**
	 * Log for the class.
	 */
	@Log
	Logger log;

	/**
	 * The implementation of the hook instrumenter.
	 */
	private final IHookInstrumenter hookInstrumenter;

	/**
	 * The implementation of the configuration storage where all definitions of the user are stored.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The class pool analyzer is used here to add the passed byte code to the class pool which is
	 * responsible for the class loader.
	 */
	private final IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * If class loader delegation should be active.
	 */
	@Value("${instrumentation.classLoaderDelegation}")
	boolean classLoaderDelegation;

	/**
	 * The default constructor which accepts two parameters which are needed.
	 * 
	 * @param configurationStorage
	 *            The configuration storage reference.
	 * @param hookInstrumenter
	 *            The hook instrumenter reference.
	 * @param classPoolAnalyzer
	 *            The class pool analyzer reference.
	 */
	@Autowired
	public ByteCodeAnalyzer(IConfigurationStorage configurationStorage, IHookInstrumenter hookInstrumenter, IClassPoolAnalyzer classPoolAnalyzer) {
		if (null == configurationStorage) {
			throw new IllegalArgumentException("Configuration storage cannot be null!");
		}
		if (null == hookInstrumenter) {
			throw new IllegalArgumentException("Hook instrumenter cannot be null!");
		}
		if (null == classPoolAnalyzer) {
			throw new IllegalArgumentException("Class pool analyzer cannot be null!");
		}
		this.configurationStorage = configurationStorage;
		this.hookInstrumenter = hookInstrumenter;
		this.classPoolAnalyzer = classPoolAnalyzer;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] analyzeAndInstrument(byte[] byteCode, String className, ClassLoader classLoader) {
		// The reason to create a byte array class path here is to handle
		// classes created at runtime (reflection / byte code engineering
		// libraries etc.) and to get the real content of that class (think of
		// classes modified by other java agents before.)
		ClassPool classPool = classPoolAnalyzer.getClassPool(classLoader);
		ClassPath classPath = null;
		ClassPath loaderClassPath = null;
		try {
			if (null == byteCode) {
				// this occurs if we are in the initialization phase and are instrumenting classes
				// where we don't have the bytecode directly. Thus we try to load it.
				byteCode = classPool.get(className).toBytecode();
			}
			if (null != Thread.currentThread().getContextClassLoader() && classLoader != Thread.currentThread().getContextClassLoader()) {
				// only use the context class loader if it is even set and not the same as the
				// classloader being passed to the instrumentation
				loaderClassPath = new LoaderClassPath(Thread.currentThread().getContextClassLoader());
				classPool.insertClassPath(loaderClassPath);
			}
			// the byte array classpath needs to be the last one to be the first for access
			classPath = new ByteArrayClassPath(className, byteCode);
			classPool.insertClassPath(classPath);

			byte[] instrumentedByteCode = null;
			Map<CtBehavior, List<UnregisteredSensorConfig>> behaviorToConfigMap = analyze(className, classLoader);

			// class loader delegation behaviors
			List<? extends CtBehavior> classLoaderDelegationBehaviors = analyzeForClassLoaderDelegation(className, classLoader);

			CtBehavior ctBehavior = null;
			if (!behaviorToConfigMap.isEmpty()) {
				ctBehavior = instrumentSensors(behaviorToConfigMap);
			}

			if (!classLoaderDelegationBehaviors.isEmpty()) {
				ctBehavior = instrumentClassLoader(classLoaderDelegationBehaviors);
			}

			if (null != ctBehavior) {
				instrumentedByteCode = ctBehavior.getDeclaringClass().toBytecode();
			}

			return instrumentedByteCode;
		} catch (NotFoundException notFoundException) {
			log.error("Error occurred instrumenting the byte code of class " + className, notFoundException);
			return null;
		} catch (IOException iOException) {
			log.error("Error occurred instrumenting the byte code of class " + className, iOException);
			return null;
		} catch (CannotCompileException cannotCompileException) {
			log.error("Error occurred instrumenting the byte code of class " + className, cannotCompileException);
			return null;
		} catch (HookException hookException) {
			log.error("Error occurred instrumenting the byte code of class " + className, hookException);
			return null;
		} catch (StorageException storageException) {
			log.error("Error occurred instrumenting the byte code of class " + className, storageException);
			return null;
		} finally {
			// Remove the byte array class path from the class pool. The class
			// loader now should know this class, thus it can be accessed
			// through the standard way.
			if (null != classPath) {
				classPool.removeClassPath(classPath);
			}
			if (null != loaderClassPath) {
				classPool.removeClassPath(loaderClassPath);
			}
		}
	}

	/**
	 * Returns the list of {@link CtBehavior} that relate to the class loader delegation.
	 * 
	 * @param className
	 *            The name of the class.
	 * @param classLoader
	 *            The class loader of the passed class.
	 * @return Returns the list of {@link CtBehavior} that relate to the class loader delegation.
	 * @throws NotFoundException
	 *             Something could not be found.
	 */
	private List<? extends CtBehavior> analyzeForClassLoaderDelegation(String className, ClassLoader classLoader) throws NotFoundException {
		if (!classLoaderDelegation) {
			return Collections.emptyList();
		}

		if (log.isTraceEnabled()) {
			log.trace("analyzeForClassLoaderDelegation: " + className);
		}

		for (IMatcher matcher : configurationStorage.getClassLoaderDelegationMatchers()) {
			if (null != matcher && matcher.compareClassName(classLoader, className)) {
				List<? extends CtBehavior> behaviors = matcher.getMatchingMethods(classLoader, className);
				if (CollectionUtils.isNotEmpty(behaviors)) {
					matcher.checkParameters(behaviors);
					return behaviors;
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * The analyze method will analyze the passed byte code, class name and class loader and returns
	 * a {@link Map} with all matching methods to be instrumented.
	 * 
	 * @param className
	 *            The name of the class.
	 * @param classLoader
	 *            The class loader of the passed class.
	 * @return Returns a {@link Map} with all found methods ({@link CtBehavior}) as the Key and a
	 *         {@link List} of {@link UnregisteredSensorConfig} as the value.
	 * @throws NotFoundException
	 *             Something could not be found.
	 * @throws StorageException
	 *             Sensor could not be added.
	 */
	private Map<CtBehavior, List<UnregisteredSensorConfig>> analyze(String className, ClassLoader classLoader) throws NotFoundException, StorageException {
		Map<CtBehavior, List<UnregisteredSensorConfig>> behaviorToConfigMap = new HashMap<CtBehavior, List<UnregisteredSensorConfig>>();

		// Iterating over all stored unregistered sensor configurations
		for (UnregisteredSensorConfig unregisteredSensorConfig : configurationStorage.getUnregisteredSensorConfigs()) {
			// try to match the class name first
			IMatcher matcher = unregisteredSensorConfig.getMatcher();
			if (matcher.compareClassName(classLoader, className)) {
				List<? extends CtBehavior> behaviors;
				// differentiate between constructors and methods.
				if (unregisteredSensorConfig.isConstructor()) {
					// the constructors
					behaviors = matcher.getMatchingConstructors(classLoader, className);
				} else {
					// the methods
					behaviors = matcher.getMatchingMethods(classLoader, className);
				}
				matcher.checkParameters(behaviors);

				// iterating over all methods which passed the matcher
				for (CtBehavior behavior : behaviors) {
					if (behaviorToConfigMap.containsKey(behavior)) {
						// access the already initialized list and store the
						// unregistered sensor configuration in it.
						List<UnregisteredSensorConfig> configs = behaviorToConfigMap.get(behavior);
						configs.add(unregisteredSensorConfig);
					} else {
						// key does not exist already, thus we have to
						// create the list first.
						List<UnregisteredSensorConfig> configs = new ArrayList<UnregisteredSensorConfig>();
						configs.add(unregisteredSensorConfig);
						behaviorToConfigMap.put(behavior, configs);
					}
				}
			}
		}

		return behaviorToConfigMap;
	}

	/**
	 * Instruments the methods in the {@link Map} and creates the appropriate
	 * {@link RegisteredSensorConfig} classes.
	 * 
	 * @param methodToConfigMap
	 *            The initialized {@link Map} which is filled by the
	 *            {@link #analyze(byte[], String, ClassLoader)} method.
	 * @return Returns the instrumented byte code.
	 * @throws NotFoundException
	 *             Something could not be found.
	 * @throws HookException
	 *             The hook instrumenter generated an exception.
	 * @throws IOException
	 *             The byte code could not be generated.
	 * @throws CannotCompileException
	 *             The byte code could not be generated.
	 */
	private CtBehavior instrumentSensors(Map<CtBehavior, List<UnregisteredSensorConfig>> methodToConfigMap) throws NotFoundException, HookException, IOException, CannotCompileException {
		CtBehavior ctBehavior = null;
		for (Map.Entry<CtBehavior, List<UnregisteredSensorConfig>> entry : methodToConfigMap.entrySet()) {
			ctBehavior = entry.getKey();
			List<UnregisteredSensorConfig> configs = entry.getValue();

			List<String> parameterTypes = new ArrayList<String>();
			CtClass[] parameterClasses = ctBehavior.getParameterTypes();
			for (int pos = 0; pos < parameterClasses.length; pos++) {
				parameterTypes.add(parameterClasses[pos].getName());
			}

			RegisteredSensorConfig rsc = new RegisteredSensorConfig();
			rsc.setTargetPackageName(ctBehavior.getDeclaringClass().getPackageName());
			rsc.setTargetClassName(ctBehavior.getDeclaringClass().getSimpleName());
			rsc.setTargetMethodName(ctBehavior.getName());
			rsc.setParameterTypes(parameterTypes);
			rsc.setModifiers(ctBehavior.getModifiers());
			rsc.setCtBehavior(ctBehavior);
			rsc.setConstructor(ctBehavior instanceof CtConstructor);

			// return type only for methods available, otherwise the return type is set to empty
			// string.
			if (!rsc.isConstructor()) {
				CtMethod ctMethod = (CtMethod) ctBehavior;
				rsc.setReturnType(ctMethod.getReturnType().getName());
			}

			for (UnregisteredSensorConfig usc : configs) {
				rsc.addSensorTypeConfig(usc.getSensorTypeConfig());
				rsc.getSettings().putAll(usc.getSettings());

				if (usc.isPropertyAccess()) {
					for (PropertyPathStart propertyPathStart : usc.getPropertyAccessorList()) {
						// Filter not meaningful property accessors.
						if (isMeaningfulCapturing(propertyPathStart.getContentType(), rsc)) {
							rsc.getPropertyAccessorList().add(propertyPathStart);
						}
					}
				}
			}

			rsc.setPropertyAccess(!rsc.getPropertyAccessorList().isEmpty());

			// only when there is an enhanced Exception Sensor defined
			if (configurationStorage.isExceptionSensorActivated() && configurationStorage.isEnhancedExceptionSensorActivated()) {
				// iterate over the exception sensor types - currently there is only one
				for (MethodSensorTypeConfig config : configurationStorage.getExceptionSensorTypes()) {
					// need to add the exception sensor config separately, because otherwise it
					// would be added to the other method hooks, but the exception sensor is a
					// constructor hook
					rsc.setExceptionSensorTypeConfig(config);
				}
			}

			if (!rsc.isConstructor()) {
				hookInstrumenter.addMethodHook((CtMethod) ctBehavior, rsc);
			} else {
				hookInstrumenter.addConstructorHook((CtConstructor) ctBehavior, rsc);
			}
		}
		return ctBehavior;
	}

	/**
	 * Instruments the methods in the {@link List} with the class loader delegation hook.
	 * 
	 * @param classLoaderDelegationBehaviors
	 *            {@link CtBehavior}s that relate to the class loader boot delegation and have to be
	 *            instrumented in different way that the normal user specified instrumentation.
	 * 
	 * @return Returns the {@link CtBehavior}.
	 * @throws NotFoundException
	 *             Something could not be found.
	 * @throws HookException
	 *             The hook instrumenter generated an exception.
	 * @throws IOException
	 *             The byte code could not be generated.
	 * @throws CannotCompileException
	 *             The byte code could not be generated.
	 */
	private CtBehavior instrumentClassLoader(List<? extends CtBehavior> classLoaderDelegationBehaviors) throws NotFoundException, HookException, IOException, CannotCompileException {
		CtBehavior ctBehavior = null;
		if (CollectionUtils.isNotEmpty(classLoaderDelegationBehaviors)) {
			for (CtBehavior clDelegationBehavior : classLoaderDelegationBehaviors) {
				ctBehavior = clDelegationBehavior;
				hookInstrumenter.addClassLoaderDelegationHook((CtMethod) ctBehavior);
			}
		}
		return ctBehavior;
	}

	/**
	 * Checks whether the property accessor is meaningful. Please note that during the creation of
	 * the property accessor certain checks are already in place. For example it is checked that no
	 * return value capturing is set on a constructor. Please ensure that checks that could be done
	 * at creation time are already performed at this time ({@link
	 * info.novatec.inspectit.agent.config.impl.ConfigurationStorage.addSensor()}).
	 * 
	 * Certain checks cannot be done on creation time. One example is the return value capturing on
	 * method defining a void return type. At creation time the information that the method the
	 * sensor is attached to has in fact no return value is not known.
	 * 
	 * @param type
	 *            the type of capturing
	 * @param rsc
	 *            the sensor configuration
	 * @return if this property accessor is meaningful.
	 */
	private boolean isMeaningfulCapturing(ParameterContentType type, RegisteredSensorConfig rsc) {
		// Return value capturing on constructors is not meaningful (property accessors should
		// never be placed on constructors anyway, so this is just an additional layer of safety).
		if (ParameterContentType.RETURN.equals(type) && rsc.isConstructor()) {
			return false;
		}

		// Return value capturing for void returning methods is just not meaningful.
		if (ParameterContentType.RETURN.equals(type) && !rsc.isConstructor() && "void".equals(rsc.getReturnType())) {
			return false;
		}

		return true;
	}
}
