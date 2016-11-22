package rocks.inspectit.agent.java.eum.instrumentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import com.google.common.io.ByteStreams;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;

/**
 * Helper class for creating a javascript agent which only has some specified modules (plugins).
 *
 * @author David Monschein
 *
 */
@Component
public class JSAgentBuilder {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(JSAgentBuilder.class);

	/**
	 * The name of the cookie to use for storing the UEM session ID.
	 */
	public static final String SESSION_ID_COOKIE_NAME = "inspectIT_cookieId";

	/**
	 * Defines how long the JS Agent of the current revision shall stay in the browsers cache.
	 */
	public static final long JS_AGENT_CACHE_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;

	/**
	 * Javascript code which starts the execution at the end when all plugins are loaded.
	 */
	private static final String EXECUTE_START_JAVASCRIPT = "inspectIT.init();";

	/**
	 * The path to the not minified javascript in the resources.
	 */
	private static final String NORMAL_SCRIPT_RESOURCE_PATH = "/js/";

	/**
	 * The path to the minified javascript in the resources.
	 */
	private static final String MINIFIED_SCRIPT_RESOURCE_PATH = "/js/min/";

	/**
	 * the path to the js agent without any plugins.
	 */
	private static final String JSBASE_RESOURCE = "inspectit_jsagent_base.js";

	/**
	 * Cache for the source of the individual JS Agent modules.
	 */
	private ConcurrentHashMap<JSAgentModule, String> moduleSourceCache = new ConcurrentHashMap<JSAgentModule, String>();

	/**
	 * Cache variable for the JS agent core source code.
	 */
	private String agentCoreSource = null;

	/**
	 * Flag whether to use the minified agent.
	 */
	private boolean useMinifedAgent;

	/**
	 * Configuration initialization. Automatically called by spring.
	 *
	 * @param config
	 *            the configuration
	 */
	@Required
	@Autowired
	public void setConfiguration(IConfigurationStorage config) {
		if (config != null) {
			try {
				useMinifedAgent = config.getEndUserMonitoringConfig().isAgentMinificationEnabled();
			} catch (StorageException e) { // NOPMD
				// fallback to normal agent
				useMinifedAgent = false;
			}
		} else {
			useMinifedAgent = false;
		}
	}

	/**
	 * Builds the JS agent from single char arguments.
	 *
	 * @param arguments
	 *            all arguments together as a string.
	 * @return the generated stream which builds the agent.
	 */
	public String buildJsFile(String arguments) {

		StringBuilder script = new StringBuilder();
		script.append("window.inspectIT_settings.activeAgentModules = \"").append(arguments).append("\";");

		script.append(getAgentCoreSource());

		// prevent duplicates of the modules added
		Set<JSAgentModule> alreadyAddedModules = new HashSet<JSAgentModule>();

		// add the modules source code
		for (char moduleIdentifier : arguments.toCharArray()) {
			if (JSAgentModule.IDENTIFIER_MAP.containsKey(moduleIdentifier)) {
				JSAgentModule module = JSAgentModule.IDENTIFIER_MAP.get(moduleIdentifier);
				if (!alreadyAddedModules.contains(module)) {
					script.append(getAgentModuleSource(module));
					alreadyAddedModules.add(module);
				}
			}
		}
		script.append(EXECUTE_START_JAVASCRIPT);

		return script.toString();
	}

	/**
	 * @return the core agent source code, either laoded from the resources or directly fetched form
	 *         the cache.
	 */
	@SuppressWarnings({ "PMD" })
	private String getAgentCoreSource() {
		if (agentCoreSource == null) {
			synchronized (JSAgentBuilder.class) {
				if (agentCoreSource == null) {
					try {
						agentCoreSource = readResourceFile(JSBASE_RESOURCE);
					} catch (Exception e) {
						LOG.error("unable to read JS Agent core");
						return "";
					}
				}
			}
		}
		return agentCoreSource;
	}

	/**
	 * @param module
	 *            the module of which the source code shall be returned
	 * @return @return the modules source code, either loaded from the resources or directly fetched
	 *         form the cache.
	 */
	private String getAgentModuleSource(JSAgentModule module) {
		if (!moduleSourceCache.containsKey(module)) {
			try {
				String src = readResourceFile(module.getModuleSourceFile());
				moduleSourceCache.putIfAbsent(module, src);
				return src;
			} catch (Exception e) {
				LOG.error("unable to read JS Agent core");
				return "";
			}
		} else {
			return moduleSourceCache.get(module);
		}
	}

	/**
	 * Utility method for reading resource text files.
	 *
	 * @param path
	 *            the path of the resource to fetch
	 * @return the read text contents of the resource
	 * @throws IOException
	 *             if the reading of the resource fails
	 */
	private String readResourceFile(String path) throws IOException {
		StringBuilder fullpath = new StringBuilder();
		if (useMinifedAgent) {
			fullpath.append(MINIFIED_SCRIPT_RESOURCE_PATH);
		} else {
			fullpath.append(NORMAL_SCRIPT_RESOURCE_PATH);
		}
		fullpath.append(path);
		InputStream is = JSAgentBuilder.class.getResourceAsStream(fullpath.toString());
		String result = new String(ByteStreams.toByteArray(is));
		is.close();
		return result;
	}

}
