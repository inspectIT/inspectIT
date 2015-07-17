package info.novatec.inspectit.jmeter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.jmeter.InspectITSamplerBase.Configuration;
import info.novatec.inspectit.jmeter.data.InspectITResultMarker;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.lang.reflect.Field;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
// NOCHKALL
public class SamplerBaseTest {

	@Mock
	CmrRepositoryDefinition repository;

	@Mock
	IGlobalDataAccessService globalDataAccessService;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void hostPortParameterIsMissing() {
		InspectITSamplerBase sampler = new InspectITSamplerBase() {
			public Configuration[] getRequiredConfig() {
				return null;
			}

			public void run() throws Throwable {
			}

			public InspectITResultMarker getResult() {
				return null;
			}

			public void setup() throws Exception {
			}
		};

		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, "172.16.145.234"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		// sampler.setup may not be called as this one initializes the CMR Repository.
		SampleResult result = sampler.runTest(context);

		assertThat(result.isSuccessful(), is(false));
		assertThat(result.getResponseMessage(), containsString("Context does not contain the required key " + Configuration.PORT.name));
	}

	@Test
	public void samplerParameterisMissing() {
		InspectITSamplerBase sampler = new InspectITSamplerBase() {
			public Configuration[] getRequiredConfig() {
				return new Configuration[] { Configuration.INVOC_COUNT };
			}

			public void run() throws Throwable {
			}

			public InspectITResultMarker getResult() {
				return null;
			}

			public void setup() throws Exception {
			}
		};

		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, "172.16.145.234"));
		arguments.addArgument(new Argument(Configuration.PORT.name, "8182"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		// sampler.setup may not be called as this one initializes the CMR Repository.
		SampleResult result = sampler.runTest(context);

		assertThat(result.isSuccessful(), is(false));
		assertThat(result.getResponseMessage(), containsString("Context does not contain the required key " + Configuration.INVOC_COUNT.name));
	}

	@Test
	public void samplerSetupThrowsException() {
		final String exceptionMessage = "MYEXCEPTION";

		InspectITSamplerBase sampler = new InspectITSamplerBase() {
			public Configuration[] getRequiredConfig() {
				return null;
			}

			public void run() throws Throwable {
			}

			public InspectITResultMarker getResult() {
				return null;
			}

			public void setup() throws Exception {
				throw new RuntimeException(exceptionMessage);
			}
		};

		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, "172.16.145.234"));
		arguments.addArgument(new Argument(Configuration.PORT.name, "8182"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		// sampler.setup may not be called as this one initializes the CMR Repository.
		SampleResult result = sampler.runTest(context);

		assertThat(result.isSuccessful(), is(false));
		assertThat(result.getResponseMessage(), containsString(exceptionMessage));
	}

	@Test
	public void samplerRunThrowsException() {
		final String exceptionMessage = "MYEXCEPTION";

		InspectITSamplerBase sampler = new InspectITSamplerBase() {
			public Configuration[] getRequiredConfig() {
				return null;
			}

			public void run() throws Throwable {
				throw new RuntimeException(exceptionMessage);
			}

			public InspectITResultMarker getResult() {
				return null;
			}

			public void setup() throws Exception {
			}
		};

		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, "172.16.145.234"));
		arguments.addArgument(new Argument(Configuration.PORT.name, "8182"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		// sampler.setup may not be called as this one initializes the CMR Repository.
		SampleResult result = sampler.runTest(context);

		assertThat(result.isSuccessful(), is(false));
		assertThat(result.getResponseMessage(), containsString(exceptionMessage));
	}

	@Test
	public void samplerRun() {
		final InspectITResultMarker inspectITResult = new InspectITResultMarker() {
		};

		InspectITSamplerBase sampler = new InspectITSamplerBase() {
			public Configuration[] getRequiredConfig() {
				return null;
			}

			public void run() throws Throwable {
			}

			public InspectITResultMarker getResult() {
				return inspectITResult;
			}

			public void setup() throws Exception {
			}
		};

		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, "172.16.145.234"));
		arguments.addArgument(new Argument(Configuration.PORT.name, "8182"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		// sampler.setup may not be called as this one initializes the CMR Repository.
		SampleResult result = sampler.runTest(context);

		assertThat(result.isSuccessful(), is(true));
	}

	@Test
	public void readConfiguration() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final InspectITResultMarker inspectITResult = new InspectITResultMarker() {
		};

		InspectITSamplerBase sampler = new InspectITSamplerBase() {
			public Configuration[] getRequiredConfig() {
				return null;
			}

			public void run() throws Throwable {
			}

			public InspectITResultMarker getResult() {
				return inspectITResult;
			}

			public void setup() throws Exception {
			}
		};

		final String host = "172.16.145.234";
		final String invocID = "2000";

		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, host));
		arguments.addArgument(new Argument(Configuration.PORT.name, "8182"));
		arguments.addArgument(new Argument(Configuration.INVOC_ID.name, invocID));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		setSamplerContextInSampler(sampler, context);

		String readHost = sampler.getValue(Configuration.HOST);
		Long readInvocId = sampler.getValue(Configuration.INVOC_ID);

		assertThat(readHost, is(equalTo(host)));
		assertThat(readInvocId, is(equalTo(Long.valueOf(invocID))));
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void readConfigurationNotThere() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final InspectITResultMarker inspectITResult = new InspectITResultMarker() {
		};

		InspectITSamplerBase sampler = new InspectITSamplerBase() {
			public Configuration[] getRequiredConfig() {
				return null;
			}

			public void run() throws Throwable {
			}

			public InspectITResultMarker getResult() {
				return inspectITResult;
			}

			public void setup() throws Exception {
			}
		};

		final String host = "172.16.145.234";

		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, host));
		arguments.addArgument(new Argument(Configuration.PORT.name, "8182"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		setSamplerContextInSampler(sampler, context);

		@SuppressWarnings("unused")
		Long readInvocId = sampler.getValue(Configuration.INVOC_ID);
	}

	/**
	 * Sets the JavaSamplerContext in the Sampler class.
	 * 
	 * We do not want to make this field package access, as we want to hide it from the realization
	 * classes.
	 * 
	 * @param base
	 *            the sampler base.
	 * @param context
	 *            the context to write.
	 * @return the adapted sampler base.
	 * @throws NoSuchFieldException
	 *             in case of an error.
	 * @throws SecurityException
	 *             in case of an error.
	 * @throws IllegalArgumentException
	 *             in case of an error.
	 * @throws IllegalAccessException
	 *             in case of an error.
	 */
	private InspectITSamplerBase setSamplerContextInSampler(InspectITSamplerBase base, JavaSamplerContext context) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		Field contextField = InspectITSamplerBase.class.getDeclaredField("context");
		contextField.setAccessible(true);
		contextField.set(base, context);
		return base;
	}
}
