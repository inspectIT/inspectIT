package info.novatec.inspectit.ci.business;

import info.novatec.inspectit.cmr.configuration.business.IExpression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract class for a boolean expression definition.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ AndExpression.class, OrExpression.class, NotExpression.class, StringMatchingExpression.class, BooleanExpression.class })
public class Expression implements IExpression {

	/**
	 *
	 */
	private static final long serialVersionUID = -4682253749961564526L;
}
