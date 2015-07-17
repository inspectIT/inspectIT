package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.jmeter.data.InspectITResultMarker;
import info.novatec.inspectit.jmeter.util.ObjectConverter;
import info.novatec.inspectit.jmeter.util.ResultService;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Base class of an inspectIT JMeter sampler. This class is responsible for creating the connection
 * to the CMR server and for providing base services that all samplers will need.
 * 
 * This class also provides a list of all configuration elements that can be used for the samplers.
 * The sampler is to realize the method <code>getRequiredConfig</code> and return all configuration
 * elements that it needs. The base class will deal with providing the input in jmeter for these
 * configurations and allow to get the concrete value during the execution.
 * 
 * Note: In JMeter each threads gets its own instances of the sampler, thus internal synchronisation
 * is not necessary.
 * 
 * @author Stefan Siegl
 */
public abstract class InspectITSamplerBase implements JavaSamplerClient {

	/**
	 * The CMR repository that is accessed.
	 */
	CmrRepositoryDefinition repository;

	/**
	 * The JMeter context of this test run. The use of this context is usually not needed as all
	 * configuration elements can be easier read using the service methods of this base class.
	 */
	private JavaSamplerContext context;

	/**
	 * Available configuration settings that the sampler can use.
	 */
	public enum Configuration {
		/** The host of the CMR repository. */
		HOST("HOST", "${HOST}", String.class),
		/** The port of the CMR repository. */
		PORT("PORT", "${PORT}", Integer.class),
		/** The platformId of the agent the action will be performed on. */
		PLATFORM_ID("PLATFORM_IDENT", "${P_ID}", Long.class),
		/** The number of elements that should be requested. */
		INVOC_COUNT("COUNT", "-1", Integer.class),
		/** The ID of the invocation sequence. */
		INVOC_ID("INVOC_ID", "${INVOC_ID}", Long.class),
		/** Whether or not extended output should be provided. */
		OUTPUT("OUTPUT", "false", Boolean.class),
		/** Sorting of the invocation overview. */
		INVOCATION_OVERVIEW_SORT("SORT BY [TIMESTAMP|CHILD_COUNT|DURATION|NESTED_DATA|URI|USE_CASE]", "TIMESTAMP", ResultComparator.class),
		/** Sorting of the exception overview. */
		EXCEPTION_SORT("SORT BY [TIMESTAMP|FQN|ERROR_MESSAGE]", "TIMESTAMP", ResultComparator.class);

		/**
		 * the name of the configuration element. This will be displayed 1:1 in jmeter.
		 */
		public String name; // NOCHK
		/** the default value of the configuration. */
		public String defaultValue; // NOCHK
		/** the type of the configuration. */
		public Class<?> type; // NOCHK

		/**
		 * Creates a new instance of the configuration.
		 * 
		 * @param name
		 *            the name.
		 * @param defaultValue
		 *            the default value.
		 * @param type
		 *            the type.
		 */
		private Configuration(String name, String defaultValue, Class<?> type) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.type = type;
		}
	}

	@Override
	public final void setupTest(JavaSamplerContext context) {
		this.context = context;
		if (repository == null) {
			String host = context.getParameter(Configuration.HOST.name);
			String port = context.getParameter(Configuration.PORT.name);
			repository = new CmrRepositoryDefinition(host, Integer.parseInt(port));
		}

		repository.refreshOnlineStatus();
		if (repository.getOnlineStatus() != OnlineStatus.ONLINE) {
			throw new RuntimeException("Server is offline."); // NOPMD
		}
	}

	@Override
	public final SampleResult runTest(JavaSamplerContext context) {
		ResultService resultService = ResultService.newInstance();
		this.context = context;

		try {
			checkContext(context);
		} catch (ConfigurationNotExistException e) {
			resultService.start();
			return resultService.fail(e);
		}

		try {
			setup();
		} catch (Exception e) {
			resultService.start();
			return resultService.fail(e);
		}

		resultService.start();
		try {
			run();
			resultService.success();
			resultService.setResult(getResult());
		} catch (Throwable e) { // NOPMD
			return resultService.fail(e);
		}

		return resultService.getResult();
	}

	/**
	 * Checks if the required configurations are in fact passed as JMeter parameters.
	 * 
	 * @param context
	 *            the jmeter context.
	 * @throws ConfigurationNotExistException
	 *             if the context does not provide a required parameter.
	 */
	private void checkContext(JavaSamplerContext context) throws ConfigurationNotExistException {
		List<Configuration> necessaryConfigurations;
		Configuration[] requiredConfigs = getRequiredConfig();
		if (null == requiredConfigs) { // no configs required
			necessaryConfigurations = new ArrayList<Configuration>();
		} else {
			necessaryConfigurations = new ArrayList<Configuration>(Arrays.asList(requiredConfigs));
		}
		necessaryConfigurations.add(Configuration.HOST);
		necessaryConfigurations.add(Configuration.PORT);

		for (Configuration configuration : necessaryConfigurations) {
			if (!context.containsParameter(configuration.name)) {
				throw new ConfigurationNotExistException("Context does not contain the required key " + configuration.name + "!");
			}
		}
	}

	/**
	 * Provide all configuration elements that this sampler needs. Please be aware that the default
	 * configuration elements {@link Configuration}.HOST and {@link Configuration}.PORT all always
	 * integrated by default.
	 * 
	 * @return all {@code CONFIG} elements that this sampler needs.
	 */
	public abstract Configuration[] getRequiredConfig();

	/**
	 * Execute the test itself.
	 * 
	 * If an exception is raised, the test run is marked as an error, otherwise the test is expected
	 * to be ok. If you need additional functionality you need to override the method
	 * {@code runTest} and do all formatting and operations on the {@code SampleResult} by yourself.
	 * 
	 * @throws Throwable
	 *             if the test cannot be run.
	 */
	public abstract void run() throws Throwable;

	/**
	 * The test is expected to return an object representation of the result. This service will
	 * convert this representation to XML to make it easier to display.
	 * 
	 * @return the test results as object. Returning <code>null</code> means that nothing should be
	 *         displayed.
	 */
	public abstract InspectITResultMarker getResult();

	/**
	 * Called before the test run - use it to initialize everything you need. Yes you could also use
	 * the <code>setupTest</code> method, but this one is jmeter related so you could read the
	 * context yourself which you do not need as you can use the Configuration class.
	 * 
	 * @throws Exception
	 *             in case of initialization error.
	 */
	public abstract void setup() throws Exception;

	@Override
	public final void teardownTest(JavaSamplerContext arg0) { // NOPMD
		// not needed
	}

	@Override
	public final Arguments getDefaultParameters() {
		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, Configuration.HOST.defaultValue));
		arguments.addArgument(new Argument(Configuration.PORT.name, Configuration.PORT.defaultValue));

		if (null != getRequiredConfig()) {
			for (Configuration config : getRequiredConfig()) {
				arguments.addArgument(new Argument(config.name, config.defaultValue));
			}
		}

		return arguments;
	}

	/**
	 * Reads the given {@code Configuration} from the context that the jmeter testcase provided.
	 * 
	 * @param e
	 *            the {@code Configuration} to read.
	 * @param <T>
	 *            the type of the {@code Configuration}.
	 * @return the value of the {@code Configuration}
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(Configuration e) {
		T res = (T) e.type.cast(ObjectConverter.convert(context.getParameter(e.name), e.type));
		if (null == res) {
			throw new RuntimeException("null value when looking up " + e.name); // NOPMD
		}
		return res;
	}

	/**
	 * Exception thrown if a required configuration is not provided.
	 * 
	 * @author Stefan Siegl.
	 */
	@SuppressWarnings("serial")
	static class ConfigurationNotExistException extends Exception {

		/**
		 * Constructor.
		 * 
		 * @param string
		 *            exception message.
		 */
		public ConfigurationNotExistException(String string) {
			super(string);
		}
	}
}
