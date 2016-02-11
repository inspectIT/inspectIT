package info.novatec.inspectit.agent.hooking.impl;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.hooking.IHookDispatcher;
import info.novatec.inspectit.agent.hooking.IHookDispatcherMapper;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.exception.IExceptionSensorHook;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.spring.logger.Log;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The hook dispatching service which is called by all the hooks throughout the instrumented target
 * application.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
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
	private final ICoreService coreService;

	/**
	 * Contains all method hooks.
	 */
	private Map<Long, RegisteredSensorConfig> methodHooks = new HashMap<Long, RegisteredSensorConfig>();

	/**
	 * Contains all constructor hooks.
	 */
	private Map<Long, RegisteredSensorConfig> constructorHooks = new HashMap<Long, RegisteredSensorConfig>();

	/**
	 * Stores the current Status of the invocation sequence tracer in a {@link ThreadLocal} object.
	 */
	private InvocationSequenceCount invocationSequenceCount = new InvocationSequenceCount();

	/**
	 * A thread local holder object to save the current started invocation sequence.
	 */
	private ThreadLocal<IHook> invocationSequenceHolder = new ThreadLocal<IHook>();

	/**
	 * If an execution of the dispatching is already in progress, we don't dispatch anything else
	 * for this thread.
	 */
	private ExecutionMarker executionMarker = new ExecutionMarker();

	/**
	 * Default constructor which needs a reference to the core service. This is needed for the
	 * invocation sensor.
	 * 
	 * @param coreService
	 *            The core service.
	 */
	@Autowired
	public HookDispatcher(ICoreService coreService) {
		this.coreService = coreService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodMapping(long id, RegisteredSensorConfig rsc) {
		methodHooks.put(Long.valueOf(id), rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addConstructorMapping(long id, RegisteredSensorConfig rsc) {
		constructorHooks.put(Long.valueOf(id), rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMethodBeforeBody(long id, Object object, Object[] parameters) {
		if (!executionMarker.isActive()) {
			try {
				executionMarker.active();

				try {
					RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(Long.valueOf(id));

					if (rsc.startsInvocationSequence()) {
						// The sensor configuration contains an invocation sequence
						// sensor. We have to set it on the thread local map for later
						// access. Additionally, we need to save the count of the called
						// invocation sensors, as another nested one could be started,
						// too.
						invocationSequenceCount.increment();

						if (null == invocationSequenceHolder.get()) {
							invocationSequenceHolder.set(((IMethodSensor) rsc.getInvocationSequenceSensorTypeConfig().getSensorType()).getHook());
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
					for (Map.Entry<Long, IHook> entry : rsc.getReverseMethodHooks().entrySet()) {
						IMethodHook methodHook = (IMethodHook) entry.getValue();
						methodHook.beforeBody(id, entry.getKey().longValue(), object, parameters, rsc);
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
					RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(Long.valueOf(id));
					for (Map.Entry<Long, IHook> entry : rsc.getMethodHooks().entrySet()) {
						IMethodHook methodHook = (IMethodHook) entry.getValue();
						methodHook.firstAfterBody(id, entry.getKey().longValue(), object, parameters, returnValue, rsc);
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
					RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(Long.valueOf(id));

					if (null != invocationSequenceHolder.get()) {
						// Need to replace the core service with the one from the invocation
						// sequence so that all data objects can be associated to that invocation
						// record.
						ICoreService invocCoreService = (ICoreService) invocationSequenceHolder.get();

						for (Map.Entry<Long, IHook> entry : rsc.getMethodHooks().entrySet()) {
							IMethodHook methodHook = (IMethodHook) entry.getValue();
							// the invocation sequence sensor needs the original core service!
							if (invocCoreService == methodHook) { // NOPMD
								methodHook.secondAfterBody(coreService, id, entry.getKey().longValue(), object, parameters, returnValue, rsc);
							} else {
								methodHook.secondAfterBody(invocCoreService, id, entry.getKey().longValue(), object, parameters, returnValue, rsc);
							}
						}
					} else {
						for (Map.Entry<Long, IHook> entry : rsc.getMethodHooks().entrySet()) {
							IMethodHook methodHook = (IMethodHook) entry.getValue();
							methodHook.secondAfterBody(coreService, id, entry.getKey().longValue(), object, parameters, returnValue, rsc);
						}
					}

					if (rsc.startsInvocationSequence()) {
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
				RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(Long.valueOf(id));
				long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) ((IMethodSensor) rsc.getExceptionSensorTypeConfig().getSensorType()).getHook();

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
				RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(Long.valueOf(id));
				long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) ((IMethodSensor) rsc.getExceptionSensorTypeConfig().getSensorType()).getHook();

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
				RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(Long.valueOf(id));
				long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) ((IMethodSensor) rsc.getExceptionSensorTypeConfig().getSensorType()).getHook();

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
				RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(Long.valueOf(id));
				long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

				ICoreService invocCoreService = null;
				if (null != invocationSequenceHolder.get()) {
					// Need to replace the core service with the one from the invocation sequence so
					// that all data objects can be associated to that invocation record.
					invocCoreService = (ICoreService) invocationSequenceHolder.get();
				}

				IExceptionSensorHook exceptionHook = (IExceptionSensorHook) ((IMethodSensor) rsc.getExceptionSensorTypeConfig().getSensorType()).getHook();

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
					RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(Long.valueOf(id));

					if (rsc.startsInvocationSequence()) {
						// The sensor configuration contains an invocation sequence sensor. We have
						// to set it on the thread local map for later access. Additionally, we need
						// to save the count of the called invocation sensors, as another nested one
						// could be started, too.
						invocationSequenceCount.increment();
						if (null == invocationSequenceHolder.get()) {
							invocationSequenceHolder.set(((IMethodSensor) rsc.getInvocationSequenceSensorTypeConfig().getSensorType()).getHook());
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
					for (Map.Entry<Long, IHook> entry : rsc.getReverseMethodHooks().entrySet()) {
						IConstructorHook constructorHook = (IConstructorHook) entry.getValue();
						constructorHook.beforeConstructor(id, entry.getKey().longValue(), parameters, rsc);
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
					RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(Long.valueOf(id));

					if (null != invocationSequenceHolder.get()) {
						// Need to replace the core service with the one from the invocation
						// sequence so that all data objects can be associated to that invocation
						// record.
						ICoreService invocCoreService = (ICoreService) invocationSequenceHolder.get();

						for (Map.Entry<Long, IHook> entry : rsc.getMethodHooks().entrySet()) {
							IConstructorHook constructorHook = (IConstructorHook) entry.getValue();
							// the invocation sequence sensor and the exception sensor need the
							// original core service!
							if (invocCoreService == constructorHook) { // NOPMD
								constructorHook.afterConstructor(coreService, id, entry.getKey().longValue(), object, parameters, rsc);
							} else {
								constructorHook.afterConstructor(invocCoreService, id, entry.getKey().longValue(), object, parameters, rsc);
							}
						}
					} else {
						for (Map.Entry<Long, IHook> entry : rsc.getMethodHooks().entrySet()) {
							IConstructorHook constructorHook = (IConstructorHook) entry.getValue();
							constructorHook.afterConstructor(coreService, id, entry.getKey().longValue(), object, parameters, rsc);
						}
					}

					if (rsc.startsInvocationSequence()) {
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
