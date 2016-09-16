package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
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
	 * The url to which the instrumentation script is mapped, should not overwrite any server
	 * resources.
	 */
	public static final String JAVASCRIPT_URL_PREFIX = "inspectit_jsagent_";

	/**
	 * The url which gets called by our javascript for sending back the captured data.
	 */
	public static final String BEACON_SUB_PATH = "inspectIT_beacon_handler";

	/**
	 * Stores the full URL to which the JS Agent will send the beacons.
	 */
	private String completeBeaconURL;

	/**
	 * The prefix of the path to the JS-script including the full path.
	 */
	private String completeJavascriptURLPrefix;
	/**
	 * The tags to inject into the HTML.
	 */
	private String completeScriptTags;

	/**
	 * {@inheritDoc}
	 */
	public boolean interceptRequest(Object servletOrFilter, Object requestObj, Object responseObj) {
		// check the types:
		if (!WHttpServletRequest.isInstance(requestObj) || !WHttpServletResponse.isInstance(responseObj)) {
			return false;
		}
		WHttpServletRequest req = WHttpServletRequest.wrap(requestObj);
		WHttpServletResponse res = WHttpServletResponse.wrap(responseObj);

		String path = null;
		try {
			path = new URI(req.getRequestURI()).getPath();
		} catch (URISyntaxException e2) {
			return false;
		}

		if (path.toLowerCase().startsWith(completeJavascriptURLPrefix.toLowerCase())) {
			String scriptArgumentsWithEnding = path.substring(completeJavascriptURLPrefix.length());
			// remove revision and ignore it, we always send the newest version
			scriptArgumentsWithEnding = scriptArgumentsWithEnding.substring(scriptArgumentsWithEnding.indexOf('_') + 1);
			String scriptArgumentsNoEnding = scriptArgumentsWithEnding.substring(0, scriptArgumentsWithEnding.lastIndexOf('.'));
			sendScript(res, JSAgentBuilder.buildJsFile(scriptArgumentsNoEnding));
			return true;
		} else if (path.equalsIgnoreCase(completeBeaconURL)) {
			// send everything ok response
			res.setStatus(200);
			res.getWriter().flush();
			receiveBeacon(req);
			return true;
		} else {
			return false;
		}

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
	public Object instrumentResponse(Object servletOrFilter, Object httpRequestObj, Object httpResponseObj) {
		if (WHttpServletResponse.isInstance(httpResponseObj)) {
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
		return httpResponseObj;
	}

	/**
	 *
	 * Generates the cookie for tracking the user session. The returned Cookie is of type
	 * javax.servlet.http.Cookie.
	 *
	 * @param httpRequestObj
	 *            the incoming request
	 * @return null, if a session cookie is already present. Otherwise, the Cookie is created and
	 *         returned.
	 */
	private Object generateSessionIDCookie(Object httpRequestObj) {

		// check if it already has an id set
		WHttpServletRequest request = WHttpServletRequest.wrap(httpRequestObj);
		Object[] cookies = request.getCookies();
		if (cookies != null) {
			for (Object cookieObj : cookies) {
				WCookie cookie = WCookie.wrap(cookieObj);
				if (cookie.getName().equals(JSAgentBuilder.SESSION_ID_COOKIE_NAME)) {
					return null;
				}
			}
		}

		String id = generateUserSessionID();

		// otherweise generate the cookie
		Object cookie = WCookie.newInstance(httpRequestObj.getClass().getClassLoader(), JSAgentBuilder.SESSION_ID_COOKIE_NAME, id);
		WCookie wrappedCookie = WCookie.wrap(cookie);
		wrappedCookie.setMaxAge(JSAgentBuilder.SESSION_COOKIE_MAX_AGE_SECONDS);
		wrappedCookie.setPath("/");
		return cookie;
	}

	/**
	 * Generates a unique ID to identify the user session.
	 *
	 * @return the generated id.
	 */
	private String generateUserSessionID() {
		// TODO: use different method for better performance?
		return UUID.randomUUID().toString(); // will be unique
	}

	/**
	 * Initializes the URL configuration using the given configuration storage.
	 *
	 * @param configurationStorage
	 *            the configuration storage
	 */
	@Autowired
	public void setConfigurationStorage(IConfigurationStorage configurationStorage) {

		String base = configurationStorage.getEndUserMonitoringConfig().getScriptBaseUrl();
		if (!base.endsWith("/")) {
			base += "/";
		}
		completeBeaconURL = base + BEACON_SUB_PATH;
		completeJavascriptURLPrefix = base + JAVASCRIPT_URL_PREFIX;

		StringBuilder tags = new StringBuilder();
		tags.append("<script type=\"text/javascript\">\r\n" + "window.inspectIT_settings = {\r\n" + "eumManagementServer : \"");
		tags.append(completeBeaconURL);
		tags.append("\"\r\n};\r\n" + "</script>\r\n" + "<script type=\"text/javascript\" src=\"");
		tags.append(completeJavascriptURLPrefix);
		tags.append(JSAgentModule.JS_AGENT_REVISION).append('_');
		tags.append(configurationStorage.getEndUserMonitoringConfig().getActiveModules());
		tags.append(".js\"></script>\r\n");
		completeScriptTags = tags.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public void servletOrFilterExit(Object servletOrFilter) {
		// TODO Auto-generated method stub

	}

}
