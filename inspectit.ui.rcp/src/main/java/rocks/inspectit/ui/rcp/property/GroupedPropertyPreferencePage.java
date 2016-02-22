package rocks.inspectit.ui.rcp.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rocks.inspectit.shared.cs.cmr.property.configuration.GroupedProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidation;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidationException;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.ValidationError;
import rocks.inspectit.shared.cs.cmr.property.update.IPropertyUpdate;

/**
 * Special {@link PropertyPreferencePage} that can handle the {@link GroupedProperty}.
 * 
 * @author Ivan Senic
 * 
 */
public class GroupedPropertyPreferencePage extends PropertyPreferencePage {

	/**
	 * {@link GroupedProperty} to display.
	 */
	private GroupedProperty groupedProperty;

	/**
	 * Validation of complete grouped property.
	 */
	private PropertyValidation propertyValidation;

	/**
	 * Default constructor.
	 * 
	 * @param groupedProperty
	 *            {@link GroupedProperty} to display.
	 * @see PropertyPreferencePage#PropertyPreferencePage(String, java.util.Collection)
	 */
	public GroupedPropertyPreferencePage(GroupedProperty groupedProperty) {
		super(groupedProperty.getName(), groupedProperty.getSingleProperties());
		this.setDescription(groupedProperty.getDescription());
		this.groupedProperty = groupedProperty;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyUpdated(SingleProperty<?> property, IPropertyUpdate<?> propertyUpdate) {
		super.propertyUpdated(property, propertyUpdate);
		executeGroupValidation();
		updatePage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyUpdateCanceled(SingleProperty<?> property) {
		super.propertyUpdateCanceled(property);
		executeGroupValidation();
		updatePage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyValidationFailed(SingleProperty<?> property, PropertyValidation propertyValidation) {
		super.propertyValidationFailed(property, propertyValidation);
		executeGroupValidation();
		updatePage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getValidationErrorsCount() {
		int count = super.getValidationErrorsCount();
		if (null != propertyValidation) {
			count += propertyValidation.getErrorCount();
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ValidationError> getValidationErrors() {
		List<ValidationError> errors = new ArrayList<>();
		errors.addAll(super.getValidationErrors());
		if (null != propertyValidation) {
			errors.addAll(propertyValidation.getErrors());
		}
		return errors;
	}

	/**
	 * Executes group validation.
	 */
	private void executeGroupValidation() {
		try {
			groupedProperty.validateForPropertiesUpdate(correctUpdateMap.values());
			propertyValidation = null; // NOPMD
		} catch (PropertyValidationException exception) {
			propertyValidation = exception.getPropertyValidation();
		}
	}
}
