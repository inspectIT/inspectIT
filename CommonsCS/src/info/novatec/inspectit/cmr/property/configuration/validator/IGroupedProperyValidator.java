package info.novatec.inspectit.cmr.property.configuration.validator;

import info.novatec.inspectit.cmr.property.configuration.GroupedProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;

import java.util.Collection;

/**
 * Validator interface for validating {@link GroupedProperty}.
 * 
 * @author Ivan Senic
 * 
 */
public interface IGroupedProperyValidator {

	/**
	 * Validates {@link GroupedProperty}.
	 * 
	 * @param groupProperty
	 *            Property to be validated.
	 * @param propertyValidation
	 *            {@link PropertyValidation} for reporting validation errors.
	 */
	void validate(GroupedProperty groupProperty, PropertyValidation propertyValidation);

	/**
	 * Validates {@link GroupedProperty} for given property updates.
	 * 
	 * @param groupProperty
	 *            Property to be validated.
	 * @param propertyUpdates
	 *            All updates.
	 * @param propertyValidation
	 *            {@link PropertyValidation} for reporting validation errors.
	 */
	void validateForPropertyUpdates(GroupedProperty groupProperty, Collection<IPropertyUpdate<?>> propertyUpdates, PropertyValidation propertyValidation);

}
