package rocks.inspectit.ui.rcp.property;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidation;
import rocks.inspectit.shared.cs.cmr.property.update.IPropertyUpdate;

/**
 * Update listener that property controls will report the updates.
 * 
 * @author Ivan Senic
 * 
 */
public interface IPropertyUpdateListener {

	/**
	 * Signals that the property has been updated.
	 * 
	 * @param property
	 *            {@link SingleProperty}
	 * @param propertyUpdate
	 *            {@link IPropertyUpdate}
	 */
	void propertyUpdated(SingleProperty<?> property, IPropertyUpdate<?> propertyUpdate);

	/**
	 * Signals that the update has been canceled.
	 * 
	 * @param property
	 *            {@link SingleProperty}
	 */
	void propertyUpdateCanceled(SingleProperty<?> property);

	/**
	 * Signals that the property validation failed.
	 * 
	 * @param property
	 *            {@link SingleProperty}
	 * @param propertyValidation
	 *            {@link PropertyValidation}.
	 */
	void propertyValidationFailed(SingleProperty<?> property, PropertyValidation propertyValidation);
}
