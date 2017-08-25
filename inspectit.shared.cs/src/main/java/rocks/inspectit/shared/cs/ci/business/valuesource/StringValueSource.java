package rocks.inspectit.shared.cs.ci.business.valuesource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.AgentNameValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HostValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpQueryStringValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpRequestMethodValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpSchemeValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpServerNameValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpServerPortValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpUriValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpUrlValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodSignatureValueSource;

/**
 * Abstract class for sources of string values within an invocation sequence.
 *
 * @author Alexander Wert
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ HttpUrlValueSource.class, HttpParameterValueSource.class, MethodSignatureValueSource.class, MethodParameterValueSource.class, HostValueSource.class, HttpRequestMethodValueSource.class,
		HttpServerPortValueSource.class, HttpQueryStringValueSource.class, HttpSchemeValueSource.class, HttpServerNameValueSource.class, HttpUriValueSource.class, AgentNameValueSource.class })
public abstract class StringValueSource {
	/**
	 * Retrieves the string values for the given {@link InvocationSequenceData} instance according
	 * to the specific string values source type.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} to retrieve the string values from
	 * @param cachedDataService
	 *            {@link ICachedDataService} used to retrieve additional information
	 * @return Returns the string values for the given {@link InvocationSequenceData} instance.
	 *         Returns an empty array if no strings can be retrieved.
	 */
	public abstract String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService);

	/**
	 * Indicates whether the value source has a set of options to be compared to.
	 *
	 * @return true, if there is a fix set of options to select from.
	 */
	public boolean hasOptions() {
		return false;
	}

	/**
	 * Returns an array of options to select from.
	 *
	 * @return Returns an array of options to select from.
	 */
	@JsonIgnore
	public String[] getOptions() {
		return new String[0];
	}
}
