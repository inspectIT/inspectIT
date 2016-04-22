package rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matchers;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.AbstractSinglePropertyValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.ISinglePropertyValidator;

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
	@Override
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
