package info.novatec.inspectit.cmr.property.configuration.validation;

import info.novatec.inspectit.cmr.property.configuration.AbstractProperty;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class containing result of {@link AbstractProperty} validation.
 * 
 * @author Ivan Senic
 * 
 */
@XmlTransient
public class PropertyValidation {

	/**
	 * Property validation errors are concerned for.
	 */
	private AbstractProperty property;

	/**
	 * All errors.
	 */
	private List<ValidationError> errors = new ArrayList<ValidationError>();

	/**
	 * No-arg constructor.
	 */
	protected PropertyValidation() {
	}

	/**
	 * Default protected constructor. Clients should use {@link #createFor(AbstractProperty)} for
	 * getting an instance.
	 * 
	 * @param property
	 *            {@link AbstractProperty} for validation.
	 */
	protected PropertyValidation(AbstractProperty property) {
		this.property = property;
	}

	/**
	 * Creates new instance of {@link PropertyValidation} for given {@link AbstractProperty}.
	 * 
	 * @param property
	 *            {@link AbstractProperty} for validation.
	 * @return New instance of {@link PropertyValidation}. Note that returned object will not
	 *         contain any validation errors.
	 */
	public static PropertyValidation createFor(AbstractProperty property) {
		return new PropertyValidation(property);
	}

	/**
	 * Gets {@link #property}.
	 * 
	 * @return {@link #property}
	 */
	public AbstractProperty getProperty() {
		return property;
	}

	/**
	 * Gets {@link #errors}.
	 * 
	 * @return {@link #errors}
	 */
	public List<ValidationError> getErrors() {
		return errors;
	}

	/**
	 * Adds {@link ValidationError}.
	 * 
	 * @param error
	 *            {@link ValidationError} to add.
	 */
	public void addValidationError(ValidationError error) {
		errors.add(error);
	}

	/**
	 * Returns if the validation has any error.
	 * 
	 * @return Returns <code>true</code> if at least one {@link ValidationError} is available.
	 */
	public boolean hasErrors() {
		return CollectionUtils.isNotEmpty(errors);
	}

	/**
	 * Returns number of error in validation.
	 * 
	 * @return Number of error in validation.
	 */
	public int getErrorCount() {
		if (CollectionUtils.isNotEmpty(errors)) {
			return errors.size();
		} else {
			return 0;
		}
	}

	/**
	 * Returns message for the {@link PropertyValidation}.
	 * 
	 * @return Returns message for the {@link PropertyValidation}.
	 */
	public String getMessage() {
		StringBuilder stringBuilder = new StringBuilder();
		if (this.hasErrors()) {
			stringBuilder.append("Property ");
			stringBuilder.append(this.getProperty().getName());
			stringBuilder.append(" can not be validated. Validation test failed because of the following errors:\n");
			for (ValidationError validationError : this.getErrors()) {
				stringBuilder.append("||- ");
				stringBuilder.append(validationError.getMessage());
			}
		} else {
			stringBuilder.append("Property ");
			stringBuilder.append(this.getProperty().getName());
			stringBuilder.append("validated without errors.");
		}
		return stringBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errors == null) ? 0 : errors.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PropertyValidation other = (PropertyValidation) obj;
		if (errors == null) {
			if (other.errors != null) {
				return false;
			}
		} else if (!errors.equals(other.errors)) {
			return false;
		}
		if (property == null) {
			if (other.property != null) {
				return false;
			}
		} else if (!property.equals(other.property)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("property", property).append("errors", errors).toString();
	}
}
