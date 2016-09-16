package rocks.inspectit.agent.java.eum.instrumentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;

/**
 * Helper class for creating a javascript agent which only has some specified modules (plugins).
 *
 * @author David Monschein
 *
 */
public final class JSAgentBuilder {

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
	private static final String EXECUTE_START_JAVASCRIPT = "inspectIT.start();";

	/**
	 * the path to the javascript in the resources.
	 */
	private static final String SCRIPT_RESOURCE_PATH = "/js/";

	/**
	 * the path to the js agent without any plugins.
	 */
	private static final String JSBASE_RESOURCE = SCRIPT_RESOURCE_PATH + "inspectit_jsagent_base.js";

	/**
	 * Cache for the source of the individual JS Agent modules.
	 */
	private static ConcurrentHashMap<JSAgentModule, String> moduleSourceCache = new ConcurrentHashMap<JSAgentModule, String>();

	/**
	 * Cache variable for the JS agent core source code.
	 */
	private static String agentCoreSource = null;

	/**
	 * Builds the JS agent from single char arguments.
	 *
	 * @param arguments
	 *            all arguments together as a string.
	 * @return the generated stream which builds the agent.
	 */
	public static String buildJsFile(String arguments) {

		StringBuilder script = new StringBuilder();
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

		script.append("\r\n").append(EXECUTE_START_JAVASCRIPT);

		return script.toString();
	}

	/**
	 * @return the core agent source code, either laoded from the resources or directly fetched form
	 *         the cache.
	 */
	@SuppressWarnings({ "PMD" })
	private static String getAgentCoreSource() {
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
	private static String getAgentModuleSource(JSAgentModule module) {
		if (!moduleSourceCache.containsKey(module)) {
			try {
				String src = readResourceFile(SCRIPT_RESOURCE_PATH + module.getModuleSourceFile());
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
	private static String readResourceFile(String path) throws IOException {
		InputStream is = JSAgentBuilder.class.getResourceAsStream(path);
		String source = new String(ByteStreams.toByteArray(is));
		is.close();
		return source;
	}

	/**
	 * No instance creation allowed.
	 */
	private JSAgentBuilder() {
	}

}
