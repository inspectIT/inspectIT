package info.novatec.inspectit.cmr.property.configuration.validator.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.validator.AbstractSinglePropertyValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.ISinglePropertyValidator;

import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matchers;

/**
 * Is percentage validator.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
@XmlRootElement(name = "isPercentage")
public class PercentageValidator<T extends Number> extends AbstractSinglePropertyValidator<T> implements ISinglePropertyValidator<T> {

	/**
	 * {@inheritDoc}
	 */
	protected boolean prove(T value) {
		return Matchers.greaterThanOrEqualTo(0d).matches(value.doubleValue()) && Matchers.lessThanOrEqualTo(1d).matches(value.doubleValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getErrorMessage(SingleProperty<? extends T> property) {
		return "Value of property '" + property.getName() + "' must be a percentage value";
	}
}
