package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.eum.data.IDataHandler;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Implementation for IServletInstrumenter.
 *
 * @author Jonas Kunz
 */
@Component
public class ServletInstrumenter implements IServletInstrumenter {
	/**
	 * The name of the RegEx-Group within the RegEx for matching the Agent specifying the Agent
	 * Modules.
	 */
	private static final int AGENT_MODULES_GROUP_INDEX = 1;

	/**
	 * The runtime linker for creating proxies.
	 */
	@Autowired
	private IRuntimeLinker linker;

	/**
	 * Handles the data which we get from the JS agent.
	 */
	@Autowired
	private IDataHandler dataHandler;

	/**
	 * The logger.
	 */
	@Log
	Logger log;

	/**
	 * A unique prefix for EUM session IDs.
	 */
	private String sessionIDPrefix;

	/**
	 * Attomic long for assigning session ids.
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
	 * State variable indicating whether a correct configuration has been supplied. If no correct
	 * configuration has been specified, the instrumenter performs NOOP.
	 */
	private boolean configurationValid = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean interceptRequest(Object servletOrFilter, Object requestObj, Object responseObj) {
		try {
			// check the types:
			if (!configurationValid || !WHttpServletRequest.isInstance(requestObj) || !WHttpServletResponse.isInstance(responseObj)) {
				return false;
			}
			WHttpServletRequest req = WHttpServletRequest.wrap(requestObj);
			WHttpServletResponse res = WHttpServletResponse.wrap(responseObj);

			String path = req.getRequestURI();
			Matcher agentURLMatcher = jsAgentURLRegEx.matcher(path);
			Matcher beaconURLMatcher = beaconURLRegEx.matcher(path);

			if (agentURLMatcher.matches()) {
				String modules = agentURLMatcher.group(AGENT_MODULES_GROUP_INDEX).toLowerCase();
				sendScript(res, JSAgentBuilder.buildJsFile(modules));
				return true;
			} else if (beaconURLMatcher.matches()) {
				// send everything ok response
				res.setStatus(200);
				res.getWriter().flush();
				receiveBeacon(req);
				return true;
			}
		} catch (Throwable e) { // NOPMD
			log.error("Error intercepting request.", e);
		}
		return false;

	}

	/**
	 * Receiving data from the injected javascript.
	 *
	 * @param req
	 *            the request which holds the data as parameters
	 */
	private void receiveBeacon(WHttpServletRequest req) {
		BufferedReader reader = req.getReader();
		StringBuffer callbackData = new StringBuffer();
		String line;
		try {
			line = reader.readLine();
			while (line != null) {
				callbackData.append(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			return;
		}

		String contentData = callbackData.toString();
		dataHandler.insertBeacon(contentData);
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
	 * {@inheritDoc}
	 */
	@Override
	public Object instrumentResponse(Object servletOrFilter, Object httpRequestObj, Object httpResponseObj) {
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
			log.error("Error instrumenting response object.", e);
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
	 * @return the new session ID cookie, reusing the existing session ID if it was present.
	 */
	private Object generateSessionIDCookie(Object httpRequestObj) {

		String sessionID = null;
		// check if it already has an id set, if yes reuse it and renew session expiration
		WHttpServletRequest request = WHttpServletRequest.wrap(httpRequestObj);
		Object[] cookies = request.getCookies();
		if (cookies != null) {
			for (Object cookieObj : cookies) {
				WCookie cookie = WCookie.wrap(cookieObj);
				if (cookie.getName().equals(JSAgentBuilder.SESSION_ID_COOKIE_NAME)) {
					sessionID = cookie.getValue();
					break;
				}
			}
		}

		if (sessionID == null) {
			sessionID = generateUserSessionID();
		}

		// otherweise generate the cookie
		Object cookie = WCookie.newInstance(httpRequestObj.getClass().getClassLoader(), JSAgentBuilder.SESSION_ID_COOKIE_NAME, sessionID);
		WCookie wrappedCookie = WCookie.wrap(cookie);
		wrappedCookie.setMaxAge(JSAgentModule.EUM_SESSION_MAX_AGE_SECONDS);
		wrappedCookie.setPath("/");
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
	@Autowired
	public void setConfigurationStorage(IConfigurationStorage configurationStorage) {
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

			String pattern = Pattern.quote(base + JSAgentModule.JAVASCRIPT_URL_PREFIX) + "\\d+_" // match
					// any
					// revision
					+ "(" + modulesRegex.toString() + ")\\.js";
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void servletOrFilterExit(Object servletOrFilter) {
		// Nothing to do currently
	}

}
