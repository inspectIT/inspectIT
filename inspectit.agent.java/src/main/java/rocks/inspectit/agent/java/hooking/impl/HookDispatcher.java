package rocks.inspectit.agent.java.hooking.impl;

import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.hooking.IConstructorHook;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.IHookDispatcher;
import rocks.inspectit.agent.java.hooking.IHookDispatcherMapper;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.exception.ExceptionSensor;
import rocks.inspectit.agent.java.sensor.exception.IExceptionSensorHook;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.sensor.method.invocationsequence.InvocationSequenceSensor;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * The hook dispatching service which is called by all the hooks throughout the instrumented target
 * application.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Ivan Senic
 */
@Component
public class HookDispatcher implements IHookDispatcherMapper, IHookDispatcher {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * The default core service.
	 */
	@Autowired
	private ICoreService coreService;

	/**
	 * {@link InvocationSequenceSensor} directly auto-wired.
	 */
	@Autowired
	private InvocationSequenceSensor invocationSequenceSensor;

	/**
	 * {@link ExceptionSensor} directly auto-wired.
	 */
	@Autowired
	private ExceptionSensor exceptionSensor;

	/**
	 * Contains all hooks. Using concurrent map as we need to enable thread-safety of
	 * {@link #addMapping(long, RegisteredSensorConfig)}.
	 */
	private final NonBlockingHashMapLong<RegisteredSensorConfig> mappings = new NonBlockingHashMapLong<RegisteredSensorConfig>();

	/**
	 * Stores the current Status of the invocation sequence tracer in a {@link ThreadLocal} object.
	 */
	private final InvocationSequenceCount invocationSequenceCount = new InvocationSequenceCount();

	/**
	 * A thread local holder object to save the current started invocation sequence.
	 */
	private final ThreadLocal<IHook> invocationSequenceHolder = new ThreadLocal<IHook>();

	/**
	 * If an execution of the dispatching is already in progress, we don't dispatch anything else
	 * for this thread.
	 */
	private final ExecutionMarker executionMarker = new ExecutionMarker();

	/**
	 * {@inheritDoc}
	 */
	public void addMapping(long id, RegisteredSensorConfig rsc) {
		mappings.put(id, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMethodBeforeBody(long id, Object object, Object[] parameters) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				try {
					RegisteredSensorConfig rsc = mappings.get(id);

					if (rsc.isStartsInvocation()) {
						// The sensor configuration contains an invocation sequence
						// sensor. We have to set it on the thread local map for later
						// access. Additionally, we need to save the count of the called
						// invocation sensors, as another nested one could be started,
						// too.
						invocationSequenceCount.increment();

						if (null == invocationSequenceHolder.get()) {
							invocationSequenceHolder.set(invocationSequenceSensor.getHook());
						}
					} else if (null != invocationSequenceHolder.get()) {
						// We are executing the following sensor types in an invocation
						// sequence context, thus we have to execute the before body
						// method of the invocation sequence hook manually.
						IMethodHook invocationHook = (IMethodHook) invocationSequenceHolder.get();

						// The sensor type ID is not important here, thus we are passing
						// a -1. It is already stored in the data object
						invocationHook.beforeBody(id, -1, object, parameters, rsc);
					}

					// Now iterate over all registered sensor types and execute them
					// reverse execution (sensor with lowest priority first)
					for (IMethodSensor methodSensor : rsc.getMethodSensorsReverse()) {
						IMethodHook methodHook = (IMethodHook) methodSensor.getHook();
						methodHook.beforeBody(id, methodSensor.getSensorTypeConfig().getId(), object, parameters, rsc);
					}
				} catch (Throwable throwable) { // NOPMD
					log.error("An error happened in the Hook Dispatcher! (before body)", throwable);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchFirstMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				try {
					RegisteredSensorConfig rsc = mappings.get(id);

					// Now iterate over all registered sensor types and execute them
					// normal execution (sensor with highest priority first)
					for (IMethodSensor methodSensor : rsc.getMethodSensors()) {
						IMethodHook methodHook = (IMethodHook) methodSensor.getHook();
						methodHook.firstAfterBody(id, methodSensor.getSensorTypeConfig().getId(), object, parameters, returnValue, rsc);
					}
				} catch (Throwable throwable) { // NOPMD
					log.error("An error happened in the Hook Dispatcher! (after body)", throwable);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchSecondMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				try {
					RegisteredSensorConfig rsc = mappings.get(id);

					if (null != invocationSequenceHolder.get()) {
						// Need to replace the core service with the one from the invocation
						// sequence so that all data objects can be associated to that invocation
						// record.
						ICoreService invocCoreService = (ICoreService) invocationSequenceHolder.get();

						// Now iterate over all registered sensor types and execute them
						// normal execution (sensor with highest priority first)
						for (IMethodSensor methodSensor : rsc.getMethodSensors()) {
							IMethodHook methodHook = (IMethodHook) methodSensor.getHook();
							// the invocation sequence sensor needs the original core service!
							long sensorId = methodSensor.getSensorTypeConfig().getId();
							if (invocCoreService == methodHook) { // NOPMD
								methodHook.secondAfterBody(coreService, id, sensorId, object, parameters, returnValue, rsc);
							} else {
								methodHook.secondAfterBody(invocCoreService, id, sensorId, object, parameters, returnValue, rsc);
							}
						}
					} else {
						for (IMethodSensor methodSensor : rsc.getMethodSensors()) {
							IMethodHook methodHook = (IMethodHook) methodSensor.getHook();
							methodHook.secondAfterBody(coreService, id, methodSensor.getSensorTypeConfig().getId(), object, parameters, returnValue, rsc);
						}
					}

					if (rsc.isStartsInvocation()) {
						invocationSequenceCount.decrement();

						if (0 == invocationSequenceCount.getCount()) {
							invocationSequenceHolder.set(null);
						}
					} else if (null != invocationSequenceHolder.get()) {
						// We have to execute the after body method of the invocation sequence hook
						// manually.
						IMethodHook invocationHook = (IMethodHook) invocationSequenceHolder.get();

						// The sensor type ID is not important here, thus we are passing a -1. It is
						// already stored in the data object
						invocationHook.secondAfterBody(coreService, id, -1, object, parameters, returnValue, rsc);
					}
				} catch (Throwable throwable) { // NOPMD
					log.error("An error happened in the Hook Dispatcher! (second after body)", throwable);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				// rsc contains the settings for the actual method where the exception was thrown.
				RegisteredSensorConfig rsc = mappings.get(id);
				MethodSensorTypeConfig sensorTypeConfig = exceptionSensor.getSensorTypeConfig();
				long sensorTypeId = sensorTypeConfig.getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) exceptionSensor.getHook();
				if (null != invocCoreService) {
					exceptionHook.dispatchOnThrowInBody(invocCoreService, id, sensorTypeId, object, exceptionObject, parameters, rsc);
				} else {
					exceptionHook.dispatchOnThrowInBody(coreService, id, sensorTypeId, object, exceptionObject, parameters, rsc);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchBeforeCatch(long id, Object exceptionObject) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				// rsc contains the settings of the actual method where the exception is catched.
				RegisteredSensorConfig rsc = mappings.get(id);
				long sensorTypeId = exceptionSensor.getSensorTypeConfig().getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) exceptionSensor.getHook();
				if (null != invocCoreService) {
					exceptionHook.dispatchBeforeCatchBody(invocCoreService, id, sensorTypeId, exceptionObject, rsc);
				} else {
					exceptionHook.dispatchBeforeCatchBody(coreService, id, sensorTypeId, exceptionObject, rsc);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				// rsc contains the settings for the actual constructor where the exception was
				// thrown.
				RegisteredSensorConfig rsc = mappings.get(id);
				long sensorTypeId = exceptionSensor.getSensorTypeConfig().getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) exceptionSensor.getHook();
				if (null != invocCoreService) {
					exceptionHook.dispatchOnThrowInBody(invocCoreService, id, sensorTypeId, object, exceptionObject, parameters, rsc);
				} else {
					exceptionHook.dispatchOnThrowInBody(coreService, id, sensorTypeId, object, exceptionObject, parameters, rsc);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorBeforeCatch(long id, Object exceptionObject) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				// rsc contains the settings of the actual constructor where the exception is
				// catched.
				RegisteredSensorConfig rsc = mappings.get(id);
				long sensorTypeId = exceptionSensor.getSensorTypeConfig().getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) exceptionSensor.getHook();
				if (null != invocCoreService) {
					exceptionHook.dispatchBeforeCatchBody(invocCoreService, id, sensorTypeId, exceptionObject, rsc);
				} else {
					exceptionHook.dispatchBeforeCatchBody(coreService, id, sensorTypeId, exceptionObject, rsc);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorBeforeBody(long id, Object[] parameters) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				try {
					RegisteredSensorConfig rsc = mappings.get(id);

					if (rsc.isStartsInvocation()) {
						// The sensor configuration contains an invocation sequence sensor. We have
						// to set it on the thread local map for later access. Additionally, we need
						// to save the count of the called invocation sensors, as another nested one
						// could be started, too.
						invocationSequenceCount.increment();
						if (null == invocationSequenceHolder.get()) {
							invocationSequenceHolder.set(invocationSequenceSensor.getHook());
						}
					} else if (null != invocationSequenceHolder.get()) {
						// We are executing the following sensor types in an invocation sequence
						// context, thus we have to execute the before body method of the invocation
						// sequence hook manually.
						IConstructorHook invocationHook = (IConstructorHook) invocationSequenceHolder.get();

						// The sensor type ID is not important here, thus we are passing a -1. It is
						// already stored in the data object
						invocationHook.beforeConstructor(id, -1, parameters, rsc);
					}

					// Now iterate over all registered sensor types and execute them
					// reverse execution (sensor with lowest priority first)
					for (IMethodSensor methodSensor : rsc.getMethodSensorsReverse()) {
						IConstructorHook constructorHook = (IConstructorHook) methodSensor.getHook();
						constructorHook.beforeConstructor(id, methodSensor.getSensorTypeConfig().getId(), parameters, rsc);
					}
				} catch (Throwable throwable) { // NOPMD
					log.error("An error happened in the Hook Dispatcher! (before constructor)", throwable);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorAfterBody(long id, Object object, Object[] parameters) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				try {
					RegisteredSensorConfig rsc = mappings.get(id);

					if (null != invocationSequenceHolder.get()) {
						// Need to replace the core service with the one from the invocation
						// sequence so that all data objects can be associated to that invocation
						// record.
						ICoreService invocCoreService = (ICoreService) invocationSequenceHolder.get();

						for (IMethodSensor methodSensor : rsc.getMethodSensors()) {
							IConstructorHook constructorHook = (IConstructorHook) methodSensor.getHook();
							// the invocation sequence sensor and the exception sensor need the
							// original core service!
							long sensorId = methodSensor.getSensorTypeConfig().getId();
							if (invocCoreService == constructorHook) { // NOPMD
								constructorHook.afterConstructor(coreService, id, sensorId, object, parameters, rsc);
							} else {
								constructorHook.afterConstructor(invocCoreService, id, sensorId, object, parameters, rsc);
							}
						}
					} else {
						for (IMethodSensor methodSensor : rsc.getMethodSensors()) {
							IConstructorHook constructorHook = (IConstructorHook) methodSensor.getHook();
							constructorHook.afterConstructor(coreService, id, methodSensor.getSensorTypeConfig().getId(), object, parameters, rsc);
						}
					}

					if (rsc.isStartsInvocation()) {
						invocationSequenceCount.decrement();

						if (0 == invocationSequenceCount.getCount()) {
							invocationSequenceHolder.set(null);
						}
					} else if (null != invocationSequenceHolder.get()) {
						// We have to execute the after body method of the invocation
						// sequence hook manually.
						IConstructorHook invocationHook = (IConstructorHook) invocationSequenceHolder.get();

						// The sensor type ID is not important here, thus we are passing
						// a -1. It is already stored in the data object
						invocationHook.afterConstructor(coreService, id, -1, object, parameters, rsc);
					}
				} catch (Throwable throwable) { // NOPMD
					log.error("An error happened in the Hook Dispatcher! (after constructor)", throwable);
				}
			} finally {
				executionMarker.deactive();
			}
		}
	}

	/**
	 * Private inner class used to track the count of the started invocation sequences in one
	 * thread. Thus it extends {@link ThreadLocal} to provide a unique number for every Thread.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private static class InvocationSequenceCount extends ThreadLocal<Long> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Long initialValue() {
			return Long.valueOf(0);
		}

		/**
		 * Increments the stored value.
		 */
		public void increment() {
			super.set(Long.valueOf(super.get().longValue() + 1));
		}

		/**
		 * Decrements the stored value.
		 */
		public void decrement() {
			super.set(Long.valueOf(super.get().longValue() - 1));
		}

		/**
		 * Returns the current count.
		 *
		 * @return The count.
		 */
		public long getCount() {
			return super.get().longValue();
		}

	}

	/**
	 * ThreadLocal execution marker which is used to mark executions as already in progress to not
	 * dispatch over and over again.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private static class ExecutionMarker extends ThreadLocal<Boolean> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}

		/**
		 * Execution is active.
		 */
		public void active() {
			super.set(Boolean.TRUE);
		}

		/**
		 * Execution is deactive.
		 */
		public void deactive() {
			super.set(Boolean.FALSE);
		}

		/**
		 * Defines if our own execution is active, and thus we have to skip the whole processing
		 * (because it could happen, that we'll never end then).
		 *
		 * @return if own execution is active.
		 */
		public boolean isActive() {
			return super.get().booleanValue();
		}

	}

}
