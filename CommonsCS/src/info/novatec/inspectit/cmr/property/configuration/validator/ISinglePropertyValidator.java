package info.novatec.inspectit.cmr.property.configuration.validator;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;

/**
 * Validator interface working on the {@link SingleProperty}.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type of the value validator can validate on.
 */
public interface ISinglePropertyValidator<T> {

	/**
	 * Performs validation of the property and adds any found error to the
	 * {@link PropertyValidation}.
	 * 
	 * @param property
	 *            {@link SingleProperty} to validate.
	 * @param propertyValidation
	 *            {@link PropertyValidation} to report errors to.
	 */
	void validate(SingleProperty<? extends T> property, PropertyValidation propertyValidation);

	/**
	 * Performs the validation of the property but takes the given value as the value to validate
	 * against.
	 * 
	 * @param property
	 *            {@link SingleProperty} to validate.
	 * @param propertyValidation
	 *            {@link PropertyValidation} to report errors to.
	 * @param value
	 *            Value to validate against.
	 */
	void validateForValue(SingleProperty<? extends T> property, PropertyValidation propertyValidation, T value);
}
