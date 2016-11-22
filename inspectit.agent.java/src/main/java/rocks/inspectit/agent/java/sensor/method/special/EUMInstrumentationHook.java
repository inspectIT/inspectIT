package rocks.inspectit.agent.java.sensor.method.special;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
import rocks.inspectit.agent.java.eum.instrumentation.EumScriptTagPrinter;
import rocks.inspectit.agent.java.eum.instrumentation.JSAgentBuilder;
import rocks.inspectit.agent.java.eum.instrumentation.TagInjectionResponseWrapper;
import rocks.inspectit.agent.java.eum.reflection.WHttpServletRequest;
import rocks.inspectit.agent.java.eum.reflection.WHttpServletResponse;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.impl.RuntimeLinker;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentEndUserMonitoringConfig;
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
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(EUMInstrumentationHook.class);;

	/**
	 * The runtime linker for creating proxies.
	 */
	private IRuntimeLinker linker;

	/**
	 * Handles the data which we get from the JS agent.
	 */
	private IDataHandler dataHandler;

	/**
	 * The builder for generating the agent source code.
	 */
	private JSAgentBuilder agentBuilder;

	/**
	 * The tracer for performing correlation.
	 */
	private TracerImpl tracer;

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
	private EumScriptTagPrinter scriptTags;

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
	 * @param tracer
	 *            the {@link TracerImpl} for correlating front- and backend traces.
	 * @param dataHandler
	 *            the {@link DataHandler} responsible for decoding received beacons.
	 * @param config
	 *            the configuration storage containing the EUM config.
	 * @param agentBuilder
	 *            the agent script builder.
	 */
	public EUMInstrumentationHook(IRuntimeLinker linker, TracerImpl tracer, IDataHandler dataHandler, IConfigurationStorage config, JSAgentBuilder agentBuilder) {
		super();
		this.linker = linker;
		this.tracer = tracer;
		this.dataHandler = dataHandler;
		this.agentBuilder = agentBuilder;
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
			// remove marker here as afterBody will not be called
			interceptionCheckPerformed.markEndCall();
			if (interceptionCheckPerformed.matchesFirst()) {
				interceptionCheckPerformed.remove(); // cleanup
			}
			return 1; // prevents the original request handling from being executed
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
					receiveBeacon(req, res);
					return true;
				}

				Matcher agentURLMatcher = jsAgentURLRegEx.matcher(path);
				if (agentURLMatcher.matches()) {
					String modules = agentURLMatcher.group(AGENT_MODULES_GROUP_INDEX).toLowerCase();
					sendScript(res, modules);
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
	 * @param res
	 *            the response object
	 */
	private void receiveBeacon(WHttpServletRequest req, WHttpServletResponse res) {
		BufferedReader reader = req.getReader();

		res.setStatus(200);
		res.setContentType("application/json");
		PrintWriter writer = res.getWriter();

		String contentData;
		try {
			contentData = CharStreams.toString(reader);
			String response = dataHandler.insertBeacon(contentData);
			writer.write(response);
		} catch (IOException e) {
			LOG.error("Error receiving beacon!", e);
		}
		writer.flush();
	}

	/**
	 * Sends the script using the given response object.
	 *
	 * @param res
	 *            the response to write
	 * @param activeModules
	 *            a String listing the identifiers of the active modules.
	 */
	private void sendScript(WHttpServletResponse res, String activeModules) {
		// we respond with the script code
		res.setStatus(200);
		res.setContentType("application/javascript");
		res.addHeader("Cache-Control", "public, max-age=" + JSAgentBuilder.JS_AGENT_CACHE_MAX_AGE_SECONDS);

		PrintWriter writer = res.getWriter();
		writer.write(agentBuilder.buildJsFile(activeModules));
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

					ClassLoader cl = httpResponseObj.getClass().getClassLoader();
					TagInjectionResponseWrapper wrap = new TagInjectionResponseWrapper(httpRequestObj, httpResponseObj, tracer, scriptTags);
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
	 * Initializes the URL configuration using the given configuration storage.
	 *
	 * @param configurationStorage
	 *            the configuration storage
	 */
	public final void initConfig(IConfigurationStorage configurationStorage) {
		try {
			AgentEndUserMonitoringConfig endUserMonitoringConfig = configurationStorage.getEndUserMonitoringConfig();

			String base = endUserMonitoringConfig.getScriptBaseUrl();
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

			scriptTags = new EumScriptTagPrinter();

			StringBuilder beaconPath = new StringBuilder();
			beaconPath.append('\"').append(base).append(JSAgentModule.BEACON_SUB_PATH).append('\"');

			StringBuilder scriptSrcURL = new StringBuilder();
			scriptSrcURL.append(base).append(JSAgentModule.JAVASCRIPT_URL_PREFIX);
			scriptSrcURL.append(JSAgentModule.JS_AGENT_REVISION).append('_');
			scriptSrcURL.append(configurationStorage.getEndUserMonitoringConfig().getActiveModules());
			scriptSrcURL.append(".js");

			scriptTags.setScriptSourceURL(scriptSrcURL.toString());
			scriptTags.setSetting("eumManagementServer", beaconPath.toString());
			scriptTags.setSetting("relevancyThreshold", String.valueOf(endUserMonitoringConfig.getRelevancyThreshold()));
			scriptTags.setSetting("allowListenerInstrumentation", String.valueOf(endUserMonitoringConfig.isListenerInstrumentationAllowed()));

			configurationValid = true;
		} catch (StorageException e) {
			configurationValid = false;
		}
	}
}
