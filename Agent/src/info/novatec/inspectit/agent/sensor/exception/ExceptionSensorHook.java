package info.novatec.inspectit.agent.sensor.exception;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.util.StringConstraint;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class adds additional code to a constructor of type {@link Throwable}, to the
 * <code>throw</code> statement and to the <code>catch</code> block catching type {@link Throwable}.
 * 
 * @author Eduard Tudenhoefner
 * @see IExceptionSensorHook
 * 
 */
public class ExceptionSensorHook implements IExceptionSensorHook {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExceptionSensorHook.class);

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The thread local containing the {@link IdentityHashToDataObject} object.
	 */
	private ThreadLocal<IdentityHashToDataObject> exceptionDataHolder = new ThreadLocal<IdentityHashToDataObject>();

	/**
	 * The thread local containing the id of the method where the exception was handled.
	 */
	private ThreadLocal<Long> exceptionHandlerId = new ThreadLocal<Long>();

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private StringConstraint strConstraint;

	/**
	 * The default constructor which needs one parameter for initialization.
	 * 
	 * @param idManager
	 *            The ID manager.
	 * @param parameter
	 *            Additional parameters.
	 */
	public ExceptionSensorHook(IIdManager idManager, Map<String, Object> parameter) {
		this.idManager = idManager;
		this.strConstraint = new StringConstraint(parameter);
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// getting the actual object class and comparing to the registered sensor config target
		// class
		String throwableClass = object.getClass().getName();
		String rscTragetClassname = rsc.getQualifiedTargetClassName();
		if (throwableClass.equals(rscTragetClassname)) {
			try {
				long platformId = idManager.getPlatformId();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				long registeredConstructorId = idManager.getRegisteredMethodId(methodId);
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				Long identityHash = Long.valueOf(System.identityHashCode(object));

				// need to reset the exception handler id
				exceptionHandlerId.set(null);

				// getting the actual object with information
				Throwable throwable = (Throwable) object;

				// creating the data object
				ExceptionSensorData data = new ExceptionSensorData(timestamp, platformId, registeredSensorTypeId, registeredConstructorId);
				data.setThrowableIdentityHashCode(identityHash.longValue());
				data.setExceptionEvent(ExceptionEvent.CREATED);
				data.setThrowableType(throwable.getClass().getName());

				// set the static information of the current object
				setStaticInformation(data, throwable);

				// creating the mapping object and setting it on the thread local
				exceptionDataHolder.set(new IdentityHashToDataObject(identityHash, data));

				// adding the data object to the core service
				coreService.addExceptionSensorData(registeredSensorTypeId, data.getThrowableIdentityHashCode(), data);
			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not start exception sequence because of a (currently) not mapped ID");
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchOnThrowInBody(ICoreService coreService, long id, long sensorTypeId, Object object, Object exceptionObject, Object[] parameters, RegisteredSensorConfig rsc) {
		// get the mapping object
		IdentityHashToDataObject mappingObject = exceptionDataHolder.get();

		if (null != mappingObject) {
			try {
				long platformId = idManager.getPlatformId();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				long registeredMethodId = idManager.getRegisteredMethodId(id);
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				Long identityHash = Long.valueOf(System.identityHashCode(exceptionObject));

				// getting the actual object with information
				Throwable throwable = (Throwable) exceptionObject;

				// creating the data object
				ExceptionSensorData data = new ExceptionSensorData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
				data.setThrowableIdentityHashCode(identityHash.longValue());
				data.setThrowableType(throwable.getClass().getName());

				// check whether it's the same Throwable object as before
				if (mappingObject.getIdentityHash().equals(identityHash)) {
					// we have to check whether the Throwable object is just passed or explicitly
					// rethrown
					if (null != exceptionHandlerId.get() && registeredMethodId == exceptionHandlerId.get().longValue()) {
						// the Throwable object is explicitly rethrown
						data.setExceptionEvent(ExceptionEvent.RETHROWN);
					} else {
						// the Throwable object is thrown the first time or just passed by the JVM,
						// so it's a PASSED event
						data.setExceptionEvent(ExceptionEvent.PASSED);
					}

					// current object is the child of the previous object
					ExceptionSensorData parent = mappingObject.getExceptionSensorData();
					parent.setChild(data);

					// we are just exchanging the data object and setting it on the mapping object
					mappingObject.setExceptionSensorData(data);
					exceptionDataHolder.set(mappingObject);
				} else {
					// it's a new Throwable object, that we didn't recognize earlier
					data.setExceptionEvent(ExceptionEvent.UNREGISTERED_PASSED);
					setStaticInformation(data, throwable);

					// we are creating a new mapping object and setting it on the thread local
					exceptionDataHolder.set(new IdentityHashToDataObject(identityHash, data));
				}

				// adding the data object to the core service
				coreService.addExceptionSensorData(registeredSensorTypeId, data.getThrowableIdentityHashCode(), data);
			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not start exception sequence because of a (currently) not mapped ID");
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchBeforeCatchBody(ICoreService coreService, long id, long sensorTypeId, Object exceptionObject, RegisteredSensorConfig rsc) {
		// get the mapping object
		IdentityHashToDataObject mappingObject = exceptionDataHolder.get();

		if (null != mappingObject) {
			try {
				long platformId = idManager.getPlatformId();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				long registeredMethodId = idManager.getRegisteredMethodId(id);
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				Long identityHash = Long.valueOf(System.identityHashCode(exceptionObject));

				// save id of the method where the exception is catched
				exceptionHandlerId.set(Long.valueOf(registeredMethodId));

				// getting the actual object with information
				Throwable throwable = (Throwable) exceptionObject;

				// creating the data object
				ExceptionSensorData data = new ExceptionSensorData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
				data.setThrowableIdentityHashCode(identityHash.longValue());
				data.setThrowableType(throwable.getClass().getName());
				data.setExceptionEvent(ExceptionEvent.HANDLED);

				// check whether it's the same Throwable object as before
				if (mappingObject.getIdentityHash().equals(identityHash)) {
					// current object is the child of the previous object
					ExceptionSensorData parent = mappingObject.getExceptionSensorData();
					parent.setChild(data);

					// we are just exchanging the data object and setting it on the mapping object
					mappingObject.setExceptionSensorData(data);
					exceptionDataHolder.set(mappingObject);
				} else {
					// it's a Throwable object, that we didn't recognize earlier
					data.setExceptionEvent(ExceptionEvent.UNREGISTERED_PASSED);
					setStaticInformation(data, throwable);

					// we are creating a new mapping object and setting it on the thread local
					exceptionDataHolder.set(new IdentityHashToDataObject(identityHash, data));
				}

				// adding the data object to the core service
				coreService.addExceptionSensorData(registeredSensorTypeId, data.getThrowableIdentityHashCode(), data);
			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not start exception sequence because of a (currently) not mapped ID");
				}
			}
		}
	}

	/**
	 * Gets static information (class name, stackTrace, cause) from the {@link Throwable} object and
	 * sets them on the passed data object.
	 * 
	 * @param exceptionSensorData
	 *            The {@link ExceptionSensorData} object where to set the information.
	 * @param throwable
	 *            The current {@link Throwable} object where to get the information.
	 */
	private void setStaticInformation(ExceptionSensorData exceptionSensorData, Throwable throwable) {
		// see INSPECTIT-387
		// getting the static content could not be always possible due to the fact that this method
		// can be executed before the creation of the concrete exception object. Getting some
		// of the information could be done by overriding, meaning that we are executing the
		// methods on the object which creation is still not finished. This can cause exceptions
		// that we need to handle properly

		try {
			Throwable cause = throwable.getCause();
			if (null != cause) {
				exceptionSensorData.setCause(strConstraint.crop(cause.getClass().getName()));
			}
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("It was not possible to retrieve the exception cause from " + throwable.getClass().getName(), e);
			}
		}

		try {
			exceptionSensorData.setErrorMessage(strConstraint.crop(throwable.getMessage()));
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("It was not possible to retrieve the error message from " + throwable.getClass().getName(), e);
			}
		}

		try {
			exceptionSensorData.setStackTrace(strConstraint.crop(stackTraceToString(throwable)));
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("It was not possible to retrieve the stack trace from " + throwable.getClass().getName(), e);
			}
		}
	}

	/**
	 * Gets the stack trace from the {@link Throwable} object and returns it as a string.
	 * 
	 * @param throwable
	 *            The {@link Throwable} object where to get the stack trace from.
	 * @return A string representation of a stack trace.
	 */
	private String stackTraceToString(Throwable throwable) {
		Writer result = new StringWriter();
		PrintWriter writer = new PrintWriter(result);
		throwable.printStackTrace(writer);
		return result.toString();
	}
}
