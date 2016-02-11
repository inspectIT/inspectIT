package info.novatec.inspectit.cmr.property.configuration.validation;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Property validation exception.
 * 
 * @author Ivan Senic
 * 
 */
@XmlTransient
public class PropertyValidationException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5854338607726327743L;

	/**
	 * {@link PropertyValidation} object that contains validation errors that caused exception to be
	 * raised.
	 */
	private PropertyValidation propertyValidation;

	/**
	 * No-arg constructor.
	 */
	public PropertyValidationException() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param propertyValidation
	 *            {@link PropertyValidation} object that contains validation errors that caused
	 *            exception to be raised.
	 */
	public PropertyValidationException(PropertyValidation propertyValidation) {
		super(propertyValidation.getMessage());
		this.propertyValidation = propertyValidation;
	}

	/**
	 * @param message
	 *            Message.
	 */
	public PropertyValidationException(String message) {
		super(message);
	}

	/**
	 * Gets {@link #propertyValidation}.
	 * 
	 * @return {@link #propertyValidation}
	 */
	public PropertyValidation getPropertyValidation() {
		return propertyValidation;
	}

}
