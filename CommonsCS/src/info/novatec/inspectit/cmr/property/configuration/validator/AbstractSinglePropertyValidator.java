package info.novatec.inspectit.cmr.property.configuration.validator;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.configuration.validation.ValidationError;

import java.util.Collections;

/**
 * Abstract class for property validation.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
public abstract class AbstractSinglePropertyValidator<T> {

	/**
	 * Proves that the given value is valid.
	 * 
	 * @param value
	 *            Value to check.
	 * @return True if value is valid to the validator, false otherwise.
	 */
	protected abstract boolean prove(T value);

	/**
	 * Returns error message to put to the {@link ValidationError} when creating one.
	 * 
	 * @param property
	 *            {@link SingleProperty} validation failed for.
	 * @return Returns error message.
	 */
	protected abstract String getErrorMessage(SingleProperty<? extends T> property);

	/**
	 * {@inheritDoc}
	 */
	public void validate(SingleProperty<? extends T> property, PropertyValidation propertyValidation) {
		if (!prove(property.getValue())) {
			addValidationError(property, propertyValidation, getErrorMessage(property));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void validateForValue(SingleProperty<? extends T> property, PropertyValidation propertyValidation, T value) {
		if (!prove(value)) {
			addValidationError(property, propertyValidation, getErrorMessage(property));
		}

	}

	/**
	 * Adds {@link ValidationError} to the {@link PropertyValidation}.
	 * 
	 * @param property
	 *            {@link SingleProperty} being validated.
	 * @param propertyValidation
	 *            {@link PropertyValidation} to add error to.
	 * @param message
	 *            Message to add to {@link ValidationError}.
	 */
	private void addValidationError(SingleProperty<? extends T> property, PropertyValidation propertyValidation, String message) {
		ValidationError validationError = new ValidationError(Collections.<SingleProperty<?>> singletonList(property), message);
		propertyValidation.addValidationError(validationError);
	}
}
