package rocks.inspectit.shared.cs.cmr.property.configuration.validator;

import java.util.Collection;

import rocks.inspectit.shared.cs.cmr.property.configuration.GroupedProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidation;
import rocks.inspectit.shared.cs.cmr.property.update.IPropertyUpdate;

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
