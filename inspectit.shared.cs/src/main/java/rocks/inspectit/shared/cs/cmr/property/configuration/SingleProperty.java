package rocks.inspectit.shared.cs.cmr.property.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.cs.cmr.property.configuration.impl.BooleanProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.impl.ByteProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.impl.LongProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.impl.PercentageProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.impl.StringProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidation;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidationException;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.ISinglePropertyValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.EMailListValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.EMailValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.FullyQualifiedClassNameValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.GreaterOrEqualValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.GreaterValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.LessOrEqualValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.LessValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.NegativeValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.NotEmptyValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.PercentageValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.PositiveValidator;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.IPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.impl.RestoreDefaultPropertyUpdate;

/**
 * Single property denotes one concrete configuration in the CMR with logical name and value.
 *
 * @author Ivan Senic
 *
 * @param <T>
 *            Type of the value property is defining.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ StringProperty.class, LongProperty.class, BooleanProperty.class, PercentageProperty.class, ByteProperty.class })
public abstract class SingleProperty<T> extends AbstractProperty {

	/**
	 * The logical name of the property that is used in the configuration.
	 */
	@XmlAttribute(name = "logical-name", required = true)
	private String logicalName;

	/**
	 * If the property is advanced, thus should be available only to expert users.
	 */
	@XmlAttribute(name = "advanced", required = true)
	private boolean advanced;

	/**
	 * If the change of this property should trigger server restart.
	 */
	@XmlAttribute(name = "server-restart-required", required = true)
	private boolean serverRestartRequired;

	/**
	 * Validators used for this property.
	 */
	@XmlElementWrapper(name = "validators")
	@XmlElementRefs({ @XmlElementRef(type = LessValidator.class), @XmlElementRef(type = LessOrEqualValidator.class), @XmlElementRef(type = GreaterValidator.class),
		@XmlElementRef(type = GreaterOrEqualValidator.class), @XmlElementRef(type = PercentageValidator.class), @XmlElementRef(type = PositiveValidator.class),
		@XmlElementRef(type = NegativeValidator.class), @XmlElementRef(type = FullyQualifiedClassNameValidator.class), @XmlElementRef(type = NotEmptyValidator.class),
			@XmlElementRef(type = EMailValidator.class), @XmlElementRef(type = EMailListValidator.class) })
	private List<ISinglePropertyValidator<? super T>> validators;

	/**
	 * No-arg constructor.
	 */
	public SingleProperty() {
	}

	/**
	 *
	 * @param name
	 *            Display name of the property. Can not be <code>null</code>.
	 * @param description
	 *            Description providing more information on property.
	 * @param logicalName
	 *            The logical name of the property that is used in the configuration.
	 * @param defaultValue
	 *            Default value.
	 * @param advanced
	 *            If the property is advanced, thus should be available only to expert users.
	 * @param serverRestartRequired
	 *            If the change of this property should trigger server restart.
	 * @throws IllegalArgumentException
	 *             If name, section, logical name or default value are <code>null</code>.
	 * @see {@link AbstractProperty#AbstractProperty(String, String)}
	 */
	public SingleProperty(String name, String description, String logicalName, T defaultValue, boolean advanced, boolean serverRestartRequired) throws IllegalArgumentException {
		super(name, description);
		if (null == logicalName) {
			throw new IllegalArgumentException("Logical name of the property can not be null.");
		}
		if (null == defaultValue) {
			throw new IllegalArgumentException("Default value of the property can not be null.");
		}
		this.logicalName = logicalName;
		this.advanced = advanced;
		this.serverRestartRequired = serverRestartRequired;
		this.setDefaultValue(defaultValue);
	}

	/**
	 * Gets the default value.
	 *
	 * @return Gets the default value.
	 */
	public abstract T getDefaultValue();

	/**
	 * Sets the default value.
	 *
	 * @param defaultValue
	 *            New value for the default value.
	 */
	protected abstract void setDefaultValue(T defaultValue);

	/**
	 * Gets the currently used value.
	 *
	 * @return Gets the currently used value.
	 */
	protected abstract T getUsedValue();

	/**
	 * Sets the currently used value.
	 *
	 * @param usedValue
	 *            New value for the currently used value.
	 */
	protected abstract void setUsedValue(T usedValue);

	/**
	 * Parses the given string literal to the correct type of the property. <br>
	 * Needed for property validation against literals.
	 *
	 * @param literal
	 *            String to parse.
	 * @return Object of the value type or <code>null</code> is parsing can not be done.
	 */
	public abstract T parseLiteral(String literal);

	/**
	 * Creates a {@link AbstractPropertyUpdate} of this property with the given update value.
	 *
	 * @param updateValue
	 *            Update value.
	 * @return {@link AbstractPropertyUpdate}.
	 */
	protected abstract AbstractPropertyUpdate<T> createPropertyUpdate(T updateValue);

	/**
	 * /** {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return advanced;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isServerRestartRequired() {
		return serverRestartRequired;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validate(PropertyValidation propertyValidation) {
		if (CollectionUtils.isNotEmpty(validators)) {
			for (ISinglePropertyValidator<? super T> validator : validators) {
				validator.validate(this, propertyValidation);
			}
		}
	}

	/**
	 * Check if the property would have validation errors if given value would be set.
	 *
	 * @param value
	 *            Value to check for.
	 * @return {@link PropertyValidation} with errors if ones exist.
	 */
	protected PropertyValidation validateForValue(T value) {
		PropertyValidation propertyValidation = PropertyValidation.createFor(this);
		if (CollectionUtils.isNotEmpty(validators)) {
			for (ISinglePropertyValidator<? super T> validator : validators) {
				validator.validateForValue(this, propertyValidation, value);
			}
		}
		return propertyValidation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(Properties properties) {
		properties.setProperty(logicalName, getValue().toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SingleProperty<?> forLogicalname(String propertyLogicalName) {
		if (logicalName.equals(propertyLogicalName)) {
			return this;
		} else {
			return null;
		}
	}

	/**
	 * Sets this property to use default value.
	 */
	public void setToDefaultValue() {
		setUsedValue(null);
	}

	/**
	 * Validates the update value against the property and if validation passes returns the
	 * {@link AbstractPropertyUpdate} object.
	 *
	 * @param updateValue
	 *            Update value
	 * @return {@link AbstractPropertyUpdate}
	 * @throws PropertyValidationException
	 *             If validation fails.
	 */
	public AbstractPropertyUpdate<T> createAndValidatePropertyUpdate(T updateValue) throws PropertyValidationException {
		if (getValue().equals(updateValue)) {
			throw new PropertyValidationException("Update value for creating property update can not be same as current property value.");
		}

		PropertyValidation propertyValidation = this.validateForValue(updateValue);
		// if has errors raise exception, otherwise create property update
		if (propertyValidation.hasErrors()) {
			throw new PropertyValidationException(propertyValidation);
		} else {
			return this.createPropertyUpdate(updateValue);
		}
	}

	/**
	 * Creates a {@link RestoreDefaultPropertyUpdate} for this property.
	 *
	 * @return {@link RestoreDefaultPropertyUpdate}
	 */
	public RestoreDefaultPropertyUpdate<T> createRestoreDefaultPropertyUpdate() {
		return new RestoreDefaultPropertyUpdate<>(this);
	}

	/**
	 * Checks if the update of the property can be done with the given
	 * {@link AbstractPropertyUpdate}. For the property to be update-able following must be met:
	 * <ul>
	 * <li>Logical name of this property must match the logical name in the
	 * {@link AbstractPropertyUpdate}
	 * <li>Value and update value must be of same class
	 * <li>Value and update value must not be equal
	 * <li>Update value must pass all validations
	 * </ul>
	 *
	 * @param propertyUpdate
	 *            {@link AbstractPropertyUpdate}.
	 * @return True if this property can be update with given {@link AbstractPropertyUpdate}.
	 */
	@SuppressWarnings("unchecked")
	public boolean canUpdate(IPropertyUpdate<?> propertyUpdate) {
		if (propertyUpdate.isRestoreDefault()) {
			return true;
		}
		if (!logicalName.equals(propertyUpdate.getPropertyLogicalName())) {
			return false;
		}
		Object updateValue = propertyUpdate.getUpdateValue();
		if (!getValue().getClass().equals(updateValue.getClass())) {
			return false;
		}
		if (getValue().equals(updateValue)) {
			return false;
		}

		PropertyValidation propertyValidation = this.validateForValue((T) updateValue);
		return !propertyValidation.hasErrors();
	}

	/**
	 * Adds {@link ISinglePropertyValidator} to be used for validating this property value.
	 *
	 * @param validator
	 *            Validator.
	 */
	public void addValidator(ISinglePropertyValidator<? super T> validator) {
		if (null == validators) {
			validators = new ArrayList<>();
		}
		validators.add(validator);
	}

	/**
	 * If default value is used for this property.
	 *
	 * @return If default value is used for this property.
	 */
	public boolean isDefaultValueUsed() {
		if (null != getUsedValue()) {
			return getUsedValue().equals(getDefaultValue());
		} else {
			return true;
		}
	}

	/**
	 * Gets {@link #value}.
	 *
	 * @return {@link #value}
	 */
	public T getValue() {
		if (null != getUsedValue()) {
			return getUsedValue();
		} else {
			return getDefaultValue();
		}
	}

	/**
	 * Sets {@link #value}.
	 *
	 * @param value
	 *            New value for {@link #value}
	 */
	public void setValue(T value) {
		setUsedValue(value);
	}

	/**
	 * Gets {@link #logicalName}.
	 *
	 * @return {@link #logicalName}
	 */
	public String getLogicalName() {
		return logicalName;
	}

	/**
	 * Returns property value as human-readable string .
	 *
	 * @return Returns property value as human-readable string .
	 */
	public String getFormattedValue() {
		return getValue().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((getDefaultValue() == null) ? 0 : getDefaultValue().hashCode());
		result = (prime * result) + ((logicalName == null) ? 0 : logicalName.hashCode());
		result = (prime * result) + ((getUsedValue() == null) ? 0 : getUsedValue().hashCode());
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
		SingleProperty<?> other = (SingleProperty<?>) obj;
		if (getDefaultValue() == null) {
			if (other.getDefaultValue() != null) {
				return false;
			}
		} else if (!getDefaultValue().equals(other.getDefaultValue())) {
			return false;
		}
		if (logicalName == null) {
			if (other.logicalName != null) {
				return false;
			}
		} else if (!logicalName.equals(other.logicalName)) {
			return false;
		}
		if (getUsedValue() == null) {
			if (other.getUsedValue() != null) {
				return false;
			}
		} else if (!getUsedValue().equals(other.getUsedValue())) {
			return false;
		}
		return true;
	}

}
