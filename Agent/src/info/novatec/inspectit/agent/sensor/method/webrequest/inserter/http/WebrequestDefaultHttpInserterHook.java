package info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook is the default implementation of http inserter. It puts the InspectIT header as
 * additional header/attribute to the remote call.
 * 
 * @author Thomas Kluge
 * 
 */
public abstract class WebrequestDefaultHttpInserterHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebrequestDefaultHttpInserterHook.class);

	/**
	 * Name of the InspectItHeader.
	 */
	protected static final String INSPECTIT_HEADER = "InspectITHeader";

	/**
	 * The stack containing the start time values.
	 */
	protected final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * The timer used for accurate measuring.
	 */
	protected final Timer timer;

	/**
	 * The ID manager.
	 */
	protected final IIdManager idManager;

	/**
	 * The thread MX bean.
	 */
	protected ThreadMXBean threadMXBean;

	/**
	 * Defines if the thread CPU time is supported.
	 */
	private boolean threadCPUTimeJMXAvailable = false;

	/**
	 * Defines if the thread CPU time is enabled.
	 */
	protected boolean threadCPUTimeEnabled = false;

	/**
	 * The stack containing the start time values.
	 */
	protected final ThreadLocalStack<Long> threadCpuTimeStack = new ThreadLocalStack<Long>();

	/**
	 * Thread local {@link RemoteCallData} object.
	 */
	protected final ThreadLocal<RemoteCallData> threadRemoteCallData = new ThreadLocal<RemoteCallData>();

	/**
	 * The remote identification manager.
	 */
	protected final RemoteIdentificationManager remoteIdentificationManager;

	/**
	 * Constructor.
	 * 
	 * * @param timer The timer
	 * 
	 * @param idManager
	 *            The id manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 * @param threadMXBean
	 *            the threadMx Bean for cpu timing
	 */
	protected WebrequestDefaultHttpInserterHook(IIdManager idManager, Timer timer, RemoteIdentificationManager remoteIdentificationManager, ThreadMXBean threadMXBean) {
		this.idManager = idManager;
		this.timer = timer;
		this.threadMXBean = threadMXBean;
		this.remoteIdentificationManager = remoteIdentificationManager;

		try {
			// if it is even supported by this JVM
			threadCPUTimeJMXAvailable = threadMXBean.isThreadCpuTimeSupported();
			if (threadCPUTimeJMXAvailable) {
				// check if its enabled
				threadCPUTimeEnabled = threadMXBean.isThreadCpuTimeEnabled();
				if (!threadCPUTimeEnabled) {
					// try to enable it
					threadMXBean.setThreadCpuTimeEnabled(true);
					// check again now if it is enabled now
					threadCPUTimeEnabled = threadMXBean.isThreadCpuTimeEnabled();
				}
			}
		} catch (RuntimeException e) {
			// catching the runtime exceptions which could be thrown by the
			// above statements.
			LOG.warn("Your environment does not support to capture CPU timings.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {

		insertInspectItHeader(methodId, sensorTypeId, object, parameters);

		timeStack.push(new Double(timer.getCurrentTime()));
		if (threadCPUTimeEnabled) {
			threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (threadCPUTimeEnabled) {
			threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
		}

	}

	/**
	 * Builds the InspectIT Header for http requests. The format of the header is
	 * "platformId;registeredSensorTypeId;registeredMethodId;timestamp"
	 * 
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param identification
	 *            The identification used as unique ID.
	 * @return The inspectItHeader as String.
	 */
	protected String getInspectItHeader(long methodId, long sensorTypeId, long identification) {

		String inspectItHeader = null;
		try {
			long platformId = idManager.getPlatformId();
			long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
			long registeredMethodId = idManager.getRegisteredMethodId(methodId);

			inspectItHeader = platformId + ";" + registeredSensorTypeId + ";" + registeredMethodId + ";" + identification;

		} catch (IdNotAvailableException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
			}
		}

		return inspectItHeader;
	}

	/**
	 * 
	 * Plattform dependent implemtation to insert the InspectItHeader into the http request.
	 * 
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 */
	protected abstract void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters);

	/**
	 * Read the http response code from Webrequest. Implementation depends on the application
	 * server.
	 * 
	 * @param object
	 *            The Object.
	 * @return The http response code.
	 */
	protected abstract int readResponseCode(Object object);

	/**
	 * Read the URL Object from Webrequest. Implementation depends on the application server.
	 * 
	 * @param object
	 *            The Object.
	 * @return The requested URL.
	 */
	protected abstract String readURL(Object object);

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {

		RemoteCallData data = this.threadRemoteCallData.get();

		if (data != null) {

			try {
				double endTime = timeStack.pop().doubleValue();
				double startTime = timeStack.pop().doubleValue();
				double duration = endTime - startTime;

				// default setting to a negative number
				double cpuDuration = -1.0d;
				if (threadCPUTimeEnabled) {
					long cpuEndTime = threadCpuTimeStack.pop().longValue();
					long cpuStartTime = threadCpuTimeStack.pop().longValue();
					cpuDuration = (cpuEndTime - cpuStartTime) / 1000000.0d;
				}

				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(methodId);
				Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));

				data.setPlatformIdent(platformId);
				data.setMethodIdent(registeredMethodId);
				data.setSensorTypeIdent(registeredSensorTypeId);
				data.setTimeStamp(timestamp);

				data.setDuration(duration);
				data.calculateMin(duration);
				data.calculateMax(duration);
				data.setCpuDuration(cpuDuration);
				data.calculateCpuMax(cpuDuration);
				data.calculateCpuMin(cpuDuration);
				data.setCount(1L);

				// set RemoteCallData specific fields
				data.setCalling(true);
				data.setResponseCode(this.readResponseCode(object));
				data.setUrl(this.readURL(object));

				// returning gathered information
				coreService.addMethodSensorData(registeredSensorTypeId, registeredMethodId, null, data);

			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
				}
			}
		}

	}
}
