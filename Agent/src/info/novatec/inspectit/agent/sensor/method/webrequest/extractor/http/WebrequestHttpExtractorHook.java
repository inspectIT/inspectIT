package info.novatec.inspectit.agent.sensor.method.webrequest.extractor.http;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.http.StartEndMarker;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implements the {@link WebrequestHttpExtractorSensor} class. It extracts the InspectIT
 * header from a remote Call.
 * 
 * @author Thomas Kluge
 * 
 */
public class WebrequestHttpExtractorHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebrequestHttpExtractorHook.class);

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

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
	private boolean threadCPUTimeEnabled = false;

	/**
	 * The stack containing the start time values.
	 */
	protected final ThreadLocalStack<Long> threadCpuTimeStack = new ThreadLocalStack<Long>();

	/**
	 * The extractor.
	 */
	private final WebrequestHttpParameterExtractor extractor;

	/**
	 * Helps us to ensure that we only store on remote call per request.
	 */
	private final StartEndMarker refMarker = new StartEndMarker();

	/**
	 * Constructor.
	 * 
	 * * @param timer The timer
	 * 
	 * @param idManager
	 *            The id manager
	 * @param threadMXBean
	 *            the threadMx Bean for cpu timing
	 */
	public WebrequestHttpExtractorHook(IIdManager idManager, Timer timer, ThreadMXBean threadMXBean) {
		this.extractor = new WebrequestHttpParameterExtractor();
		this.idManager = idManager;
		this.timer = timer;
		this.threadMXBean = threadMXBean;

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
			LOG.warn("Your environment does not support to capture CPU timings.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {

		// Only during the first invocation, we make preparations.
		if (!refMarker.isMarkerSet()) {
			// Mark first invocation
			refMarker.markCall();
			// save start time
			timeStack.push(new Double(timer.getCurrentTime()));
			if (threadCPUTimeEnabled) {
				threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
			}
		} else {
			// Mark sub invocation, first already marked.
			refMarker.markCall();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// no invocation marked -> skip
		if (!refMarker.isMarkerSet()) {
			return;
		}

		// remove mark from sub call
		refMarker.markEndCall();

		if (refMarker.matchesFirst()) {
			// Get the timer and store it.
			timeStack.push(new Double(timer.getCurrentTime()));
			if (threadCPUTimeEnabled) {
				threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {

		// check if in the right(first) invocation
		if (refMarker.isMarkerSet() && refMarker.matchesFirst()) {
			// call ended, remove the marker.
			refMarker.remove();

			// extract InspectItHeader Informations
			Object httpServletRequest = parameters[0];
			Class<?> servletRequestClass = httpServletRequest.getClass();

			// just save data if insptectItHeader is avable, it makes no sense without the header
			if (extractor.providesInspectItHeader(servletRequestClass, httpServletRequest)) {
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

					// Creating return data object
					RemoteCallData data = new RemoteCallData();

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
					data.setIdentification(extractor.getIdentification(servletRequestClass, httpServletRequest));
					data.setRemotePlatformIdent(extractor.getRemotePlatformIdent(servletRequestClass, httpServletRequest));
					data.setCalling(false);

					// returning gathered information
					coreService.addMethodSensorData(registeredSensorTypeId, registeredMethodId, null, data);
				} catch (IdNotAvailableException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Could not save the remote call data because of an unavailable id. " + e.getMessage());
					}
				}
			}

		}
	}

}
