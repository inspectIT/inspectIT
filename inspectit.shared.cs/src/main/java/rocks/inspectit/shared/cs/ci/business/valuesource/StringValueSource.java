package rocks.inspectit.shared.cs.ci.business.valuesource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HostValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpUriValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodSignatureValueSource;

/**
 * Abstract class for sources of string values within an invocation sequence.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ HttpUriValueSource.class, HttpParameterValueSource.class, MethodSignatureValueSource.class, MethodParameterValueSource.class, HostValueSource.class })
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
}
