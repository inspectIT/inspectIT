package info.novatec.inspectit.jmeter.util;

import info.novatec.inspectit.jmeter.data.AggregatedSQLResult;
import info.novatec.inspectit.jmeter.data.AggregatedTimerResult;
import info.novatec.inspectit.jmeter.data.ConnectedAgent;
import info.novatec.inspectit.jmeter.data.ConnectedAgents;
import info.novatec.inspectit.jmeter.data.ExceptionResult;
import info.novatec.inspectit.jmeter.data.HttpAggregatedResult;
import info.novatec.inspectit.jmeter.data.HttpUsecaseResult;
import info.novatec.inspectit.jmeter.data.InvocationDetailResult;
import info.novatec.inspectit.jmeter.data.InvocationOverviewResult;
import info.novatec.inspectit.jmeter.data.ResultBase;

import com.thoughtworks.xstream.XStream;

/**
 * Factory to create our XStream serializer.
 * 
 * @author Stefan Siegl
 */
public final class XStreamFactory {

	/**
	 * Hidden constructor.
	 */
	private XStreamFactory() {
	}

	/**
	 * Returns the XStream serializer.
	 * 
	 * @return the XStream serializer.
	 */
	public static XStream getXStream() {

		XStream xStream = new XStream();
		xStream.useAttributeFor(String.class);
		xStream.useAttributeFor(Long.class);
		xStream.useAttributeFor(Integer.class);
		xStream.useAttributeFor(Boolean.class);
		xStream.useAttributeFor(Integer.class);

		// strange enough, useAttributeFor(Long.class) only works for the attributes defined in the
		// same! class
		// but here platformId is inherited.
		xStream.useAttributeFor(ResultBase.class, "platformId");

		xStream.alias("sqls", AggregatedSQLResult.class);
		xStream.alias("timers", AggregatedTimerResult.class);
		xStream.alias("agents", ConnectedAgents.class);
		xStream.alias("agent", ConnectedAgent.class);
		xStream.alias("invocation-detail", InvocationDetailResult.class);
		xStream.alias("invocations", InvocationOverviewResult.class);
		xStream.alias("exceptions", ExceptionResult.class);
		xStream.alias("http-usecase", HttpUsecaseResult.class);
		xStream.alias("http-aggregation", HttpAggregatedResult.class);

		return xStream;
	}

}
