package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for JS Agent modules.
 *
 * @author Jonas Kunz
 *
 */
public enum JSAgentModule {
	/**
	 * Enum values for all existing JS modules.
	 */
	BROWSERINFO_MODULE('m', "plugins/browsermetainfo.js", "Browser Meta-Info Capturing", "When enabled, the browser alongside with additional meta information of users is captured."),

	/**
	 * Module for tracking AJAX requests.
	 */
	AJAX_MODULE('a', "plugins/ajax.js", "AJAX Capturing Module", "This module is responsible for capturing AJAX requests."),
	/**
	 * Module for instrumenting asynchronous JS functions.
	 */
	ASYNC_MODULE('b', "plugins/async.js", "Async Module", "This module instruments asynchronous Javascrpt functions like setTimeout. This helps the JS Agent to be more precise with bundling user actions. This module has no impact on other Scripts using such functions."),
	/**
	 * Module for instrumenting listener functions on HTML Elements.
	 */
	LISTENER_MODULE('l',
			"plugins/listener.js",
			"Listener Instrumentation Module",
			"This module instruments the addListener functions for DOM elements and therefore is able to capture User actions like a Click or something similar. Like the asynchronous module this one also has no impact on your own Scripts."),
	/**
	 * Speedindex module.
	 */
	SPEEDINDEX_MODULE('r', "plugins/speedindex.js", "Speed Index Module",
			"This module handles the calculation of the RUM speed index. See: https://github.com/WPO-Foundation/RUM-SpeedIndex/"),
	/**
	 * Navigation timings API module.
	 */
	NAVTIMINGS_MODULE('1', "plugins/navtimings.js",
			"Navigation Timings Module",
			"This module deals with the collection of data captured by the Navigation Timings API. See: https://www.w3.org/TR/navigation-timing/ for further information."),
	/**
	 * Resource timings API module.
	 */
	RESTIMINGS_MODULE(
			'2', "plugins/restimings.js", "Resource Timings Module",
			"This module deals with collecting Resource timings provided by the Resource Timings API. See: https://www.w3.org/TR/resource-timing/ for further information.");


	/**
	 * The url which gets called by our javascript for sending back the captured data.
	 */
	public static final String BEACON_SUB_PATH = "inspectIT_beacon_handler";

	/**
	 * The url to which the instrumentation script is mapped, should not overwrite any server
	 * resources.
	 */
	public static final String JAVASCRIPT_URL_PREFIX = "inspectit_jsagent_";

	/**
	 * Maps single characters to an JS Agent module.
	 */
	public static final Map<Character, JSAgentModule> IDENTIFIER_MAP;

	/**
	 * Increment for each release where the JS Agent has changed. This value is embedded into the
	 * URL for fetching the agent, therefore incrementing the revision ensures that the newest
	 * version is fetched instead of using an old one from the HTTP cache.
	 */
	public static final int JS_AGENT_REVISION = 2;

	static {
		HashMap<Character, JSAgentModule> temp = new HashMap<Character, JSAgentModule>();
		for (JSAgentModule mod : JSAgentModule.values()) {

			char identifier = mod.getIdentifier();
			if (temp.containsKey(identifier)) {
				throw new RuntimeException("Duplicate usage of identifier " + identifier);
			}
			temp.put(identifier, mod);
		}
		IDENTIFIER_MAP = Collections.unmodifiableMap(temp);
	}

	/**
	 * The single char identifier. Only lower-case characters are allowed.
	 */
	private char identifier;

	/**
	 * The whole module source file.
	 */
	private String moduleSourceFile;

	/**
	 * The name in the UI.
	 */
	private String uiName;

	/**
	 * The description in the UI.
	 */
	private String uiDescription;

	/**
	 * Creates a new configuration.
	 *
	 * @param identifier
	 *            single char identifier
	 * @param moduleSourceFile
	 *            the whole module source file
	 * @param uiName
	 *            name in the UI
	 * @param uiDescription
	 *            description in the UI
	 */
	JSAgentModule(char identifier, String moduleSourceFile, String uiName, String uiDescription) {
		this.identifier = Character.toLowerCase(identifier);
		this.uiName = uiName;
		this.uiDescription = uiDescription;
		this.moduleSourceFile = moduleSourceFile;
	}

	/**
	 * Gets {@link #IDENTIFIER_MAP}.
	 *
	 * @return {@link #IDENTIFIER_MAP}
	 */
	public static Map<Character, JSAgentModule> getIdentifierMap() {
		return IDENTIFIER_MAP;
	}

	/**
	 * Gets {@link #identifier}.
	 *
	 * @return {@link #identifier}
	 */
	public char getIdentifier() {
		return this.identifier;
	}

	/**
	 * Gets {@link #uiName}.
	 *
	 * @return {@link #uiName}
	 */
	public String getUiName() {
		return this.uiName;
	}

	/**
	 * Gets {@link #moduleSourceFile}.
	 *
	 * @return {@link #moduleSourceFile}
	 */
	public String getModuleSourceFile() {
		return this.moduleSourceFile;
	}

	/**
	 * Gets {@link #uiDescription}.
	 *
	 * @return {@link #uiDescription}
	 */
	public String getUiDescription() {
		return this.uiDescription;
	}

}
