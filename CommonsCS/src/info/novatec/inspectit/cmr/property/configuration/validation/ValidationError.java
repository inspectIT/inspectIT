package info.novatec.inspectit.cmr.property.configuration.validation;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;

import java.util.Collection;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Single validation error containing the message describing the validation.
 * 
 * @author Ivan Senic
 * 
 */
@XmlTransient
public class ValidationError {

	/**
	 * Validation error message.
	 */
	private String message;

	/**
	 * One or more {@link SingleProperty}s that have been involved in the validation.
	 */
	private Collection<SingleProperty<?>> involvedProperties;

	/**
	 * No-arg constructor.
	 */
	public ValidationError() {
	}

	/**
	 * @param involvedProperties
	 *            One or more {@link SingleProperty}s that have been involved in the validation.
	 * @param message
	 *            Validation error message.
	 */
	public ValidationError(Collection<SingleProperty<?>> involvedProperties, String message) {
		this.involvedProperties = involvedProperties;
		this.message = message;
	}

	/**
	 * Gets {@link #message}.
	 * 
	 * @return {@link #message}
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets {@link #message}.
	 * 
	 * @param message
	 *            New value for {@link #message}
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets {@link #involvedProperties}.
	 * 
	 * @return {@link #involvedProperties}
	 */
	public Collection<SingleProperty<?>> getInvolvedProperties() {
		return involvedProperties;
	}

	/**
	 * Sets {@link #involvedProperties}.
	 * 
	 * @param involvedProperties
	 *            New value for {@link #involvedProperties}
	 */
	public void setInvolvedProperties(Collection<SingleProperty<?>> involvedProperties) {
		this.involvedProperties = involvedProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((involvedProperties == null) ? 0 : involvedProperties.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		ValidationError other = (ValidationError) obj;
		if (involvedProperties == null) {
			if (other.involvedProperties != null) {
				return false;
			}
		} else if (!involvedProperties.equals(other.involvedProperties)) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("involvedProperties", involvedProperties).append("message", message).toString();
	}

}
