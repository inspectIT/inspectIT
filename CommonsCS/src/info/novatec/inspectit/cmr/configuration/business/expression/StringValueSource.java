package info.novatec.inspectit.cmr.configuration.business.expression;

import info.novatec.inspectit.cmr.configuration.business.expression.impl.HostValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.HttpParameterValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.HttpUriValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.MethodSignatureValueSource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract class for sources of string values within an invocation sequence.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ HttpUriValueSource.class, HttpParameterValueSource.class, MethodSignatureValueSource.class, HostValueSource.class })
public class StringValueSource implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5893657607226909316L;

}
