package rocks.inspectit.agent.java.sensor.method.http;

import java.lang.management.ThreadMXBean;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.timer.TimerHook;
import rocks.inspectit.agent.java.util.ClassUtil;
import rocks.inspectit.agent.java.util.StringConstraint;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;

/**
 * The hook implementation for the http sensor. It uses the {@link ThreadLocalStack} class to save
 * the time when the method was called.
 * <p>
 * This hook measures timer data like the {@link TimerHook} but in addition provides Http
 * information. Another difference is that we ensure that only one Http metric per request is
 * created.
 *
 * @author Stefan Siegl
 *
 */
public class HttpHook implements IMethodHook {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(HttpHook.class);

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The Platform manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * The thread MX bean.
	 */
	private final ThreadMXBean threadMXBean;

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
	private final ThreadLocalStack<Long> threadCpuTimeStack = new ThreadLocalStack<Long>();

	/**
	 * Extractor for Http information.
	 */
	private final HttpInformationExtractor extractor;

	/**
	 * Configuration setting if session data should be captured.
	 */
	private final boolean captureSessionData;

	/**
	 * Expected name of the HttpServletRequest interface.
	 */
	private static final String HTTP_SERVLET_REQUEST_CLASS = "javax.servlet.http.HttpServletRequest";

	/**
	 * Expected name of the HttpServletResponse interface.
	 */
	private static final String HTTP_SERVLET_RESPONSE_CLASS = "javax.servlet.http.HttpServletResponse";

	/**
	 * Whitelist that contains all classes that we already checked if they provide
	 * HttpServletMetrics and do. We are talking about the class of the ServletRequest here. This
	 * list is extended if a new Class that provides this interface is found.
	 */
	private static final CopyOnWriteArrayList<Class<?>> HTTP_REQUEST_WHITE_LIST = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Blacklist that contains all classes that we already checked if they provide
	 * HttpServletMetrics and do not. We are talking about the class of the ServletRequest here.
	 * This list is extended if a new Class that does not provides this interface is found.
	 */
	private static final CopyOnWriteArrayList<Class<?>> HTTP_REQUEST_BLACK_LIST = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Whitelist that contains all classes that we already checked if they provide
	 * HttpServletMetrics and do. We are talking about the class of the ServletResponse here. This
	 * list is extended if a new Class that provides this interface is found.
	 */
	private static final CopyOnWriteArrayList<Class<?>> HTTP_RESPONSE_WHITE_LIST = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Blacklist that contains all classes that we already checked if they provide
	 * HttpServletMetrics and do not. We are talking about the class of the ServletResponse here.
	 * This list is extended if a new Class that does not provides this interface is found.
	 */
	private static final CopyOnWriteArrayList<Class<?>> HTTP_RESPONSE_BLACK_LIST = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Helps us to ensure that we only store on http metric per request.
	 */
	private final StartEndMarker refMarker = new StartEndMarker();

	/**
	 * This constructor creates a new instance of a <code>HttpHook</code>.
	 *
	 * @param timer
	 *            The timer
	 * @param platformManager
	 *            The Platform manager
	 * @param threadMXBean
	 *            the threadMx Bean for cpu timing
	 * @param parameters
	 *            the map containing the configuration parameters
	 */
	public HttpHook(Timer timer, IPlatformManager platformManager, Map<String, Object> parameters, ThreadMXBean threadMXBean) {
		this.timer = timer;
		this.platformManager = platformManager;
		this.threadMXBean = threadMXBean;
		this.extractor = new HttpInformationExtractor(new StringConstraint(parameters));

		if ("true".equals(parameters.get("sessioncapture"))) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Enabling session capturing for the http sensor");
			}
			captureSessionData = true;
		} else {
			captureSessionData = false;
		}

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
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {

		// We mark the invocation of the first servlet and the calls from within it. This way we
		// gather information just once (from the first one) and avoid overhead and inconclusive
		// information.

		// Only during the first invocation, we make preparations.
		if (!refMarker.isMarkerSet()) {

			// We expect the first parameter to be of the type javax.servlet.ServletRequest
			// If this is not the case then the configuration was wrong.
			if (parameters.length >= 2) {
				Object httpServletRequest = parameters[0];
				Object httpServletResponse = parameters[1];
				Class<?> servletRequestClass = httpServletRequest.getClass();
				Class<?> servletResponseClass = httpServletResponse.getClass();

				// Check if metrics interface provided
				if (providesHttpRequestMetrics(servletRequestClass) && providesHttpResponseMetrics(servletResponseClass)) {

					// We must take the time as soon as we know that we are dealing with an http
					// timer. We cannot do that after we read the information from the request
					// object because these methods could be instrumented and thus the whole http
					// timer would be off - resulting in very strange results.
					timeStack.push(new Double(timer.getCurrentTime()));
					if (threadCPUTimeEnabled) {
						threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
					}

					// Mark first invocation
					refMarker.markCall();

				}
			}
		} else {
			// Mark sub invocation, first already marked.
			refMarker.markCall();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {

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
	@Override
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) { // NOCHK:8-params

		// check if in the right(first) invocation
		if (refMarker.isMarkerSet() && refMarker.matchesFirst()) {
			// call ended, remove the marker.
			refMarker.remove();

			// double check if nothing changed
			if (parameters.length >= 2) {

				Object httpServletRequest = parameters[0];
				Object httpServletResponse = parameters[1];
				Class<?> servletRequestClass = httpServletRequest.getClass();
				Class<?> servletResponseClass = httpServletResponse.getClass();

				// double check interface
				if (providesHttpRequestMetrics(servletRequestClass) && providesHttpResponseMetrics(servletResponseClass)) {

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

					long platformId = platformManager.getPlatformId();
					Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));

					// Creating return data object
					HttpTimerData data = new HttpTimerData();

					data.setPlatformIdent(platformId);
					data.setMethodIdent(methodId);
					data.setSensorTypeIdent(sensorTypeId);
					data.setTimeStamp(timestamp);

					data.setDuration(duration);
					data.calculateMin(duration);
					data.calculateMax(duration);
					data.setCpuDuration(cpuDuration);
					data.calculateCpuMax(cpuDuration);
					data.calculateCpuMin(cpuDuration);
					data.setCount(1L);

					// Include additional http information
					data.getHttpInfo().setUri(extractor.getRequestUri(servletRequestClass, httpServletRequest));
					data.getHttpInfo().setRequestMethod(extractor.getRequestMethod(servletRequestClass, httpServletRequest));
					data.getHttpInfo().setScheme(extractor.getScheme(servletRequestClass, httpServletRequest));
					data.getHttpInfo().setServerName(extractor.getServerName(servletRequestClass, httpServletRequest));
					data.getHttpInfo().setServerPort(extractor.getServerPort(servletRequestClass, httpServletRequest));
					data.getHttpInfo().setQueryString(extractor.getQueryString(servletRequestClass, httpServletRequest));
					data.setParameters(extractor.getParameterMap(servletRequestClass, httpServletRequest));
					data.setAttributes(extractor.getAttributes(servletRequestClass, httpServletRequest));
					data.setHeaders(extractor.getHeaders(servletRequestClass, httpServletRequest));
					if (captureSessionData) {
						data.setSessionAttributes(extractor.getSessionAttributes(servletRequestClass, httpServletRequest));
					}

					// Include HTTP response information
					data.setHttpResponseStatus(extractor.getResponseStatus(servletResponseClass, httpServletResponse));

					boolean charting = Boolean.TRUE.equals(rsc.getSettings().get("charting"));
					data.setCharting(charting);

					// returning gathered information
					coreService.addDefaultData(data);
				}
			}
		}
	}

	/**
	 * Checks if the given Class is realizing the HttpServletRequest interface directly or
	 * indirectly. Only if this interface is realized, we can get Http metric information.
	 *
	 * @param c
	 *            The class to check
	 * @return whether or not the HttpServletRequest interface is realized.
	 */
	private boolean providesHttpRequestMetrics(Class<?> c) {
		return implementsInterface(c, HTTP_SERVLET_REQUEST_CLASS, HTTP_REQUEST_WHITE_LIST, HTTP_REQUEST_BLACK_LIST);
	}

	/**
	 * Checks if the given Class is realizing the HttpServletResponse interface directly or
	 * indirectly. Only if this interface is realized, we can get Http metric information.
	 *
	 * @param c
	 *            The class to check
	 * @return whether or not the HttpServletResponse interface is realized.
	 */
	private boolean providesHttpResponseMetrics(Class<?> c) {
		return implementsInterface(c, HTTP_SERVLET_RESPONSE_CLASS, HTTP_RESPONSE_WHITE_LIST, HTTP_RESPONSE_BLACK_LIST);
	}

	/**
	 * Checks if the given class implements the given interface.
	 *
	 * @param c
	 *            The class to check.
	 * @param interfaceName
	 *            The name of the target interface.
	 * @param whiteList
	 *            A whitelist (cache) of classes from which we know that they implement the
	 *            interface.
	 * @param blackList
	 *            A blacklist (cache) of classes from which we know that they do not implement the
	 *            interface.
	 * @return True, if the given class implements the given interface.
	 */
	private boolean implementsInterface(Class<?> c, String interfaceName, CopyOnWriteArrayList<Class<?>> whiteList, CopyOnWriteArrayList<Class<?>> blackList) {
		if (whiteList.contains(c)) {
			return true;
		}
		if (blackList.contains(c)) {
			return false;
		}
		Class<?> intf = ClassUtil.searchInterface(c, interfaceName);
		if (null != intf) {
			whiteList.addIfAbsent(c);
			return true;
		} else {
			blackList.addIfAbsent(c);
			return false;
		}
	}

}
