package info.novatec.inspectit.jmeter.localexecution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.jmeter.InspectITAggregatedSQL;
import info.novatec.inspectit.jmeter.InspectITAggregatedTimer;
import info.novatec.inspectit.jmeter.InspectITExceptionResult;
import info.novatec.inspectit.jmeter.InspectITGetConnectedAgents;
import info.novatec.inspectit.jmeter.InspectITHttpAggregation;
import info.novatec.inspectit.jmeter.InspectITHttpUsecaseAggregation;
import info.novatec.inspectit.jmeter.InspectITInvocationDetails;
import info.novatec.inspectit.jmeter.InspectITInvocationOverview;
import info.novatec.inspectit.jmeter.InspectITSamplerBase;
import info.novatec.inspectit.jmeter.InspectITSamplerBase.Configuration;
import info.novatec.inspectit.jmeter.data.AggregatedSQLResult;
import info.novatec.inspectit.jmeter.data.AggregatedTimerResult;
import info.novatec.inspectit.jmeter.data.ConnectedAgents;
import info.novatec.inspectit.jmeter.data.ExceptionResult;
import info.novatec.inspectit.jmeter.data.HttpAggregatedResult;
import info.novatec.inspectit.jmeter.data.HttpUsecaseResult;
import info.novatec.inspectit.jmeter.data.InvocationDetailResult;
import info.novatec.inspectit.jmeter.data.InvocationOverviewResult;
import info.novatec.inspectit.jmeter.util.XStreamFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Local test. This class can be used to run the JMeter sampler test locally against a remote CMR.
 * By default the central CMR in NovaTec is used. The address of the CMR can be overwritten.
 * 
 * Please understand, that this class will not be automatically be invoked.
 * 
 * @author Stefan Siegl
 */
@SuppressWarnings("PMD")
// NOCHKALL
public final class LocalRunner {

	private String cmrIp;
	private String cmrPort;
	/** filled by the connectedAgent test case. */
	private ConnectedAgents agents;

	private Map<String, List<Long>> platformIdToInvocationDetails = new HashMap<>();
	private Map<String, Arguments> platformIdToArguments = new HashMap<>();

	/** Constructor. */
	private LocalRunner() {
	}

	@Parameters({ "ip", "port" })
	@BeforeTest
	public void init(@Optional("172.16.145.234") String ip, @Optional("8182") String port) {
		cmrIp = ip;
		cmrPort = port;
	}

	private Arguments buildBasicArguments() {
		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(Configuration.HOST.name, cmrIp));
		arguments.addArgument(new Argument(Configuration.PORT.name, cmrPort));
		return arguments;
	}

	private String readPlatformId(JavaSamplerContext ctx) {
		return ctx.getParameter(Configuration.PLATFORM_ID.name);
	}

	private Arguments cloneArgument(Arguments arguments) {
		Arguments cloned = new Arguments();
		for (int i = 0; i < arguments.getArgumentCount(); i++) {
			Argument arg = arguments.getArgument(i);
			cloned.addArgument(new Argument(arg.getName(), arg.getValue()));
		}
		return cloned;
	}

	@DataProvider(name = "ArgumentsWithAgent")
	public Object[][] getArgumentsForConnectedAgents() {
		Object[][] result = new Object[agents.getAgents().size()][1];
		for (int i = 0; i < agents.getAgents().size(); i++) {
			Arguments arguments = buildBasicArguments();
			arguments.addArgument(new Argument(Configuration.PLATFORM_ID.name, "" + agents.getAgents().get(i).getPlatformId()));
			result[i][0] = arguments;
		}
		return result;
	}

	@DataProvider(name = "ArgumentsForInvocationDetail")
	public Object[][] getArgumentsForInvocationDetails() {
		int size = 0;
		for (List<Long> values : platformIdToInvocationDetails.values()) {
			size += values.size();
		}

		Object[][] result = new Object[size][2];

		int i = 0;
		for (String platformId : platformIdToArguments.keySet()) {
			for (Long id : platformIdToInvocationDetails.get(platformId)) {
				result[i][0] = cloneArgument(platformIdToArguments.get(platformId));
				result[i][1] = id;
				i++;
			}
		}

		return result;
	}

	@Test
	public void connectedAgents() {
		JavaSamplerContext context = new JavaSamplerContext(buildBasicArguments());
		InspectITSamplerBase testcase = new InspectITGetConnectedAgents();
		testcase.setupTest(context);
		SampleResult result = testcase.runTest(context);
		testcase.teardownTest(context);

		agents = (ConnectedAgents) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat("No agents found, is your CMR running?", agents.getAgents(), is(not(empty())));
	}

	@Test(dataProvider = "ArgumentsWithAgent", dependsOnMethods = { "connectedAgents" })
	public void invocationSequence10(Arguments arguments) {
		InspectITSamplerBase invocs = new InspectITInvocationOverview();
		arguments.addArgument(new Argument(Configuration.INVOC_COUNT.name, "10"));
		arguments.addArgument(new Argument(Configuration.OUTPUT.name, "true"));
		arguments.addArgument(new Argument(Configuration.INVOCATION_OVERVIEW_SORT.name, "TIMESTAMP"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		invocs.setupTest(context);
		SampleResult result = invocs.runTest(context);

		InvocationOverviewResult invovOverviewResult = (InvocationOverviewResult) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat("We did not find any invocation sequences, please check that there are some", invovOverviewResult.invocationIds, is(not(empty())));
		assertThat(result.isSuccessful(), is(true));

		// provide the invocation details (that is the id of the invocation sequences) to other
		// tests.
		String platformId = readPlatformId(context);
		platformIdToArguments.put(platformId, arguments);
		platformIdToInvocationDetails.put(platformId, invovOverviewResult.invocationIds);
	}

	@Test(dataProvider = "ArgumentsForInvocationDetail", dependsOnMethods = { "invocationSequence10" })
	public void invocationDetail(Arguments arguments, Long id) {
		arguments.addArgument(new Argument(Configuration.INVOC_ID.name, id.toString()));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		InspectITInvocationDetails invocDetails = new InspectITInvocationDetails();

		invocDetails.setupTest(context);
		SampleResult result = invocDetails.runTest(context);

		InvocationDetailResult detailResult = (InvocationDetailResult) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat(detailResult, is(not(nullValue())));
		assertThat(result.isSuccessful(), is(true));
	}

	@Test(dataProvider = "ArgumentsWithAgent", dependsOnMethods = { "connectedAgents" })
	public void sql(Arguments arguments) {
		JavaSamplerContext context = new JavaSamplerContext(arguments);
		InspectITAggregatedSQL sql = new InspectITAggregatedSQL();
		sql.setupTest(context);
		SampleResult result = sql.runTest(context);
		sql.teardownTest(context);

		AggregatedSQLResult detailResult = (AggregatedSQLResult) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat(detailResult, is(not(nullValue())));
		assertThat(result.isSuccessful(), is(true));
	}

	@Test(dataProvider = "ArgumentsWithAgent", dependsOnMethods = { "connectedAgents" })
	public void timer(Arguments arguments) {
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		InspectITAggregatedTimer timer = new InspectITAggregatedTimer();
		timer.setupTest(context);
		SampleResult result = timer.runTest(context);
		timer.teardownTest(context);

		AggregatedTimerResult detailResult = (AggregatedTimerResult) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat(detailResult, is(not(nullValue())));
		assertThat(result.isSuccessful(), is(true));
	}

	@Test(dataProvider = "ArgumentsWithAgent", dependsOnMethods = { "connectedAgents" })
	public void exceptions(Arguments arguments) {
		arguments.addArgument(new Argument(Configuration.EXCEPTION_SORT.name, "TIMESTAMP"));
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		InspectITExceptionResult exception = new InspectITExceptionResult();
		exception.setupTest(context);
		SampleResult result = exception.runTest(context);
		exception.teardownTest(context);

		ExceptionResult detailResult = (ExceptionResult) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat(detailResult, is(not(nullValue())));
		assertThat(result.isSuccessful(), is(true));
	}

	@Test(dataProvider = "ArgumentsWithAgent", dependsOnMethods = { "connectedAgents" })
	public void httpAggregated(Arguments arguments) {
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		InspectITHttpAggregation http = new InspectITHttpAggregation();
		http.setupTest(context);
		SampleResult result = http.runTest(context);
		http.teardownTest(context);

		HttpAggregatedResult detailResult = (HttpAggregatedResult) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat(detailResult, is(not(nullValue())));
		assertThat(result.isSuccessful(), is(true));
	}

	@Test(dataProvider = "ArgumentsWithAgent", dependsOnMethods = { "connectedAgents" })
	public void httpUsecase(Arguments arguments) {
		JavaSamplerContext context = new JavaSamplerContext(arguments);

		InspectITHttpUsecaseAggregation http = new InspectITHttpUsecaseAggregation();
		http.setupTest(context);
		SampleResult result = http.runTest(context);
		http.teardownTest(context);

		HttpUsecaseResult detailResult = (HttpUsecaseResult) XStreamFactory.getXStream().fromXML(result.getResponseDataAsString());

		assertThat(detailResult, is(not(nullValue())));
		assertThat(result.isSuccessful(), is(true));
	}
}
