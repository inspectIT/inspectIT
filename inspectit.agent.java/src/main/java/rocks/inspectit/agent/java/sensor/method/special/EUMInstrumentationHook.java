package rocks.inspectit.agent.java.sensor.method.special;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.eum.data.DataHandler;
import rocks.inspectit.agent.java.eum.data.IDataHandler;
import rocks.inspectit.agent.java.eum.instrumentation.JSAgentBuilder;
import rocks.inspectit.agent.java.eum.instrumentation.TagInjectionResponseWrapper;
import rocks.inspectit.agent.java.eum.reflection.WCookie;
import rocks.inspectit.agent.java.eum.reflection.WHttpServletRequest;
import rocks.inspectit.agent.java.eum.reflection.WHttpServletResponse;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.impl.RuntimeLinker;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;

/**
 * @author Jonas Kunz
 *
 */
public class EUMInstrumentationHook implements ISpecialHook {
	/**
	 * The name of the RegEx-Group within the RegEx for matching the Agent specifying the Agent
	 * Modules.
	 */
	private static final int AGENT_MODULES_GROUP_INDEX = 1;

	/**
	 * The runtime linker for creating proxies.
	 */
	private IRuntimeLinker linker;

	/**
	 * Handles the data which we get from the JS agent.
	 */
	private IDataHandler dataHandler;

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(EUMInstrumentationHook.class);;

	/**
	 * A unique prefix for EUM session IDs.
	 */
	private String sessionIDPrefix;

	/**
	 * Atomic long for assigning session ids.
	 */
	private AtomicLong sessionIdCounter;

	/**
	 * A matcher for the beacon URL.
	 */
	private Pattern beaconURLRegEx;

	/**
	 * A matcher matching requests requesting the JS Agent script file.
	 */
	private Pattern jsAgentURLRegEx;

	/**
	 * The tags to inject into the HTML.
	 */
	private String completeScriptTags;

	/**
	 * Marks the call hierarchy depth. Only the FIrst call received performs an interception check.
	 */
	private StartEndMarker interceptionCheckPerformed = new StartEndMarker();

	/**
	 * State variable indicating whether a correct configuration has been supplied. If no correct
	 * configuration has been specified, the instrumenter performs NOOP.
	 */
	private boolean configurationValid = false;

	/**
	 * Initialises this hook.
	 *
	 * @param linker
	 *            the {@link RuntimeLinker} to use for generating proxies.
	 * @param dataHandler
	 *            the {@link DataHandler} responsible for decoding received beacons.
	 * @param config
	 *            the configuration storage containing the EUM config.
	 */
	public EUMInstrumentationHook(IRuntimeLinker linker, IDataHandler dataHandler, IConfigurationStorage config) {
		super();
		this.linker = linker;
		this.dataHandler = dataHandler;
		initConfig(config);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		Object servletRequest = parameters[0];
		Object servletResponse = parameters[1];
		if (interceptRequest(servletRequest, servletResponse)) {
			interceptionCheckPerformed.markEndCall();
			if (interceptionCheckPerformed.matchesFirst()) {
				interceptionCheckPerformed.remove(); // cleanup
			}
			return 1;
		} else {
			parameters[1] = instrumentResponse(servletRequest, servletResponse);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc) {
		interceptionCheckPerformed.markEndCall();
		if (interceptionCheckPerformed.matchesFirst()) {
			interceptionCheckPerformed.remove(); // cleanup
		}
		return null;
	}

	/**
	 * Lets InspecIT decide whether it will intercept the request. If the request is intercepted,
	 * InspectIT will write the response of this request to the client and the application server
	 * will not process this request any further.
	 *
	 * @param requestObj
	 *            the request (an instance of javax.servlet.ServletRequest)
	 * @param responseObj
	 *            the response object (an instance of javax.servlet.ServletResponse)
	 * @return true if the request was intercepted (the application server should not continue
	 *         processing), false otherwise
	 */
	private boolean interceptRequest(Object requestObj, Object responseObj) {
		boolean performCheck = !interceptionCheckPerformed.isMarkerSet();
		interceptionCheckPerformed.markCall();
		if (performCheck && configurationValid) {
			try {
				// check the types:
				if (!WHttpServletRequest.isInstance(requestObj) || !WHttpServletResponse.isInstance(responseObj)) {
					return false;
				}
				WHttpServletRequest req = WHttpServletRequest.wrap(requestObj);
				WHttpServletResponse res = WHttpServletResponse.wrap(responseObj);

				String path = req.getRequestURI();

				Matcher beaconURLMatcher = beaconURLRegEx.matcher(path);
				if (beaconURLMatcher.matches()) {
					// send everything ok response
					res.setStatus(200);
					res.getWriter().flush();
					receiveBeacon(req);
					return true;
				}

				Matcher agentURLMatcher = jsAgentURLRegEx.matcher(path);
				if (agentURLMatcher.matches()) {
					String modules = agentURLMatcher.group(AGENT_MODULES_GROUP_INDEX).toLowerCase();
					sendScript(res, JSAgentBuilder.buildJsFile(modules));
					return true;
				}
				return false;
			} catch (Throwable e) { // NOPMD
				LOG.error("Error intercepting request.", e);
			}
		}
		return false;

	}

	/**
	 * Received and decodes the given beacon.
	 *
	 * @param req
	 *            the beacon request
	 */
	private void receiveBeacon(WHttpServletRequest req) {
		BufferedReader reader = req.getReader();

		String contentData;
		try {
			contentData = CharStreams.toString(reader);
			dataHandler.insertBeacon(contentData);
		} catch (IOException e) {
			LOG.error("Error receiving beacon!", e);
		}
	}

	/**
	 * Sends the script using the given response object.
	 *
	 * @param res
	 *            the response to write
	 * @param scriptSource
	 *            the source of the script to send.
	 */
	private void sendScript(WHttpServletResponse res, String scriptSource) {
		// we respond with the script code
		res.setStatus(200);
		res.setContentType("application/javascript");
		res.addHeader("Cache-Control", "public, max-age=" + JSAgentBuilder.JS_AGENT_CACHE_MAX_AGE_SECONDS);

		PrintWriter writer = res.getWriter();
		writer.write(scriptSource);
		writer.flush();
	}

	/**
	 * Lets InspectIT optionally instrument the given response by wrapping it.
	 *
	 * @param httpRequestObj
	 *            the request (an instance of javax.servlet.ServletRequest)
	 * @param httpResponseObj
	 *            the response object (an instance of javax.servlet.ServletResponse)
	 * @return the new response object to use, or the original one if it was not instrumented.
	 */
	public Object instrumentResponse(Object httpRequestObj, Object httpResponseObj) {
		try {
			if (configurationValid && WHttpServletResponse.isInstance(httpResponseObj)) {
				if (!linker.isProxyInstance(httpResponseObj, TagInjectionResponseWrapper.class)) {

					Object sessionIdCookie = generateSessionIDCookie(httpRequestObj);

					ClassLoader cl = httpResponseObj.getClass().getClassLoader();
					TagInjectionResponseWrapper wrap = new TagInjectionResponseWrapper(httpResponseObj, sessionIdCookie, completeScriptTags);
					Object proxy = linker.createProxy(TagInjectionResponseWrapper.class, wrap, cl);
					if (proxy == null) {
						return httpResponseObj;
					} else {
						return proxy;
					}

				}
			}
		} catch (Throwable e) { // NOPMD
			LOG.error("Error instrumenting response object.", e);
		}
		return httpResponseObj; // No instrumentation
	}

	/**
	 *
	 * Generates the cookie for tracking the user session. The returned Cookie is of type
	 * javax.servlet.http.Cookie.
	 *
	 * @param httpRequestObj
	 *            the incoming request
	 * @return the new session ID cookie, or null if it is already set.
	 */
	private Object generateSessionIDCookie(Object httpRequestObj) {

		// check if it already has an id set, if yes reuse it and renew session expiration
		WHttpServletRequest request = WHttpServletRequest.wrap(httpRequestObj);
		Object[] cookies = request.getCookies();
		if (cookies != null) {
			for (Object cookieObj : cookies) {
				WCookie cookie = WCookie.wrap(cookieObj);
				if (cookie.getName().equals(JSAgentBuilder.SESSION_ID_COOKIE_NAME)) {
					return null; // cookie already present, nothing todo
				}
			}
		}

		String sessionID = generateUserSessionID();

		// otherwise generate the cookie
		Object cookie = WCookie.newInstance(httpRequestObj.getClass().getClassLoader(), JSAgentBuilder.SESSION_ID_COOKIE_NAME, sessionID);
		WCookie wrappedCookie = WCookie.wrap(cookie);
		wrappedCookie.setPath("/");
		// We do not set any expiration age - the default age "-1" represents a session cookie which
		// is deleted when the browser is closed
		return cookie;
	}

	/**
	 * Generates a unique ID to identify the user session.
	 *
	 * @return the generated id.
	 */
	private String generateUserSessionID() {
		return sessionIDPrefix + sessionIdCounter.incrementAndGet(); // will be unique
	}

	/**
	 * Initializes the URL configuration using the given configuration storage.
	 *
	 * @param configurationStorage
	 *            the configuration storage
	 */
	public void initConfig(IConfigurationStorage configurationStorage) {
		try {
			sessionIDPrefix = configurationStorage.getAgentName() + "_" + System.currentTimeMillis() + "_";
			sessionIdCounter = new AtomicLong();

			String base = configurationStorage.getEndUserMonitoringConfig().getScriptBaseUrl();
			beaconURLRegEx = Pattern.compile(Pattern.quote(base + JSAgentModule.BEACON_SUB_PATH), Pattern.CASE_INSENSITIVE);

			// modules regex matches any string consisting only of valid module identifiers
			StringBuilder modulesRegex = new StringBuilder("(");
			for (JSAgentModule module : JSAgentModule.values()) {
				if (modulesRegex.length() > 1) {
					modulesRegex.append('|');
				}
				modulesRegex.append(Pattern.quote(String.valueOf(module.getIdentifier())));
			}
			modulesRegex.append(")*");

			// match any revision
			String pattern = Pattern.quote(base + JSAgentModule.JAVASCRIPT_URL_PREFIX) + "\\d+_" + "(" + modulesRegex.toString() + ")\\.js";
			jsAgentURLRegEx = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

			StringBuilder tags = new StringBuilder();
			tags.append("<script type=\"text/javascript\">" + "window.inspectIT_settings = {" + "eumManagementServer : \"");
			tags.append(base).append(JSAgentModule.BEACON_SUB_PATH);
			tags.append("\"}; </script> <script type=\"text/javascript\" src=\"");
			tags.append(base).append(JSAgentModule.JAVASCRIPT_URL_PREFIX);
			tags.append(JSAgentModule.JS_AGENT_REVISION).append('_');
			tags.append(configurationStorage.getEndUserMonitoringConfig().getActiveModules());
			tags.append(".js\"></script>");
			completeScriptTags = tags.toString();
			configurationValid = true;
		} catch (StorageException e) {
			configurationValid = false;
		}
	}
}
