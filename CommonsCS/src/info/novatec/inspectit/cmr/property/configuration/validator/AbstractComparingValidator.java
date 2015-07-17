package info.novatec.inspectit.cmr.property.configuration.validator;

import info.novatec.inspectit.cmr.property.configuration.GroupedProperty;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.configuration.validation.ValidationError;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract validator for all comparing validators.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractComparingValidator<T> implements IGroupedProperyValidator, ISinglePropertyValidator<T> {

	/**
	 * Name of the property to be tested. Can be <code>null</code> in case of
	 * {@link ISinglePropertyValidator} validator.
	 */
	@XmlAttribute(name = "property", required = false)
	private String property;

	/**
	 * What to compare the property to. It can be logical name of the property in case of group
	 * validation or literal in case of single validation.
	 */
	@XmlAttribute(name = "than", required = true)
	private String than;

	/**
	 * Compares property against another property. If comparing proves to be wrong the implementing
	 * class is responsible of adding the correct {@link ValidationError} to the
	 * {@link PropertyValidation}.
	 * 
	 * @param property
	 *            {@link SingleProperty}
	 * @param againstProperty
	 *            {@link SingleProperty} to compare against.
	 * @param value
	 *            Value to use for property
	 * @param against
	 *            value to use for against property.
	 * @param propertyValidation
	 *            {@link PropertyValidation}
	 */
	protected abstract void compare(SingleProperty<? extends T> property, SingleProperty<? extends T> againstProperty, T value, T against, PropertyValidation propertyValidation);

	/**
	 * Compares property against value. If comparing proves to be wrong the implementing class is
	 * responsible of adding the correct {@link ValidationError} to the {@link PropertyValidation}.
	 * 
	 * @param property
	 *            {@link SingleProperty}
	 * @param value
	 *            Value to use for validation.
	 * @param against
	 *            Value to compare against.
	 * @param propertyValidation
	 *            {@link PropertyValidation}
	 */
	protected abstract void compare(SingleProperty<? extends T> property, T value, T against, PropertyValidation propertyValidation);

	/**
	 * {@inheritDoc}
	 */
	public void validate(SingleProperty<? extends T> property, PropertyValidation propertyValidation) {
		T against = getAgainstValue(property, propertyValidation);

		if (null != against) {
			compare(property, property.getValue(), against, propertyValidation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void validateForValue(SingleProperty<? extends T> property, PropertyValidation propertyValidation, T value) {
		T against = getAgainstValue(property, propertyValidation);
		if (null != against) {
			compare(property, value, against, propertyValidation);
		}
	}

	/**
	 * Retrieves against value from {@link #than} for given property. If parsing fails the
	 * {@link PropertyValidation} will be filled with error and <code>null</code> will be returned
	 * as value.
	 * 
	 * @param property
	 *            Property to get against value for.
	 * @param propertyValidation
	 *            Validation to report errors.
	 * @return Value or <code>null</code> if parsing fails.
	 */
	private T getAgainstValue(SingleProperty<? extends T> property, PropertyValidation propertyValidation) {
		T against = property.parseLiteral(than);

		if (null == against) {
			ValidationError validationError = new ValidationError(Collections.<SingleProperty<?>> singletonList(property), "Validation of  property " + property.getName()
					+ " failed because literal (" + than + ") to compare against can not be parsed.");
			propertyValidation.addValidationError(validationError);
		}

		return against;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void validate(GroupedProperty groupProperty, PropertyValidation propertyValidation) {
		SingleProperty<?> compare = getProperty(groupProperty, propertyValidation);
		SingleProperty<?> against = getAgainstProperty(groupProperty, propertyValidation);

		if (null == compare || null == against) {
			return;
		}

		try {
			compare((SingleProperty<T>) compare, (SingleProperty<T>) against, (T) compare.getValue(), (T) against.getValue(), propertyValidation);
		} catch (Exception e) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because exception occurred during validation. Exception message: " + e.getMessage());
			propertyValidation.addValidationError(validationError);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void validateForPropertyUpdates(GroupedProperty groupProperty, Collection<IPropertyUpdate<?>> propertyUpdates, PropertyValidation propertyValidation) {
		SingleProperty<?> compareProperty = getProperty(groupProperty, propertyValidation);
		SingleProperty<?> againstProperty = getAgainstProperty(groupProperty, propertyValidation);

		if (null == compareProperty || null == againstProperty) {
			return;
		}

		T value = (T) compareProperty.getValue();
		T against = (T) againstProperty.getValue();

		for (IPropertyUpdate<?> propertyUpdate : propertyUpdates) {
			if (Objects.equals(compareProperty.getLogicalName(), propertyUpdate.getPropertyLogicalName())) {
				value = (T) propertyUpdate.getUpdateValue();
			}
			if (Objects.equals(againstProperty.getLogicalName(), propertyUpdate.getPropertyLogicalName())) {
				against = (T) propertyUpdate.getUpdateValue();
			}
		}

		// no validation if nothing changed
		if (Objects.equals(value, compareProperty.getValue()) && Objects.equals(against, againstProperty.getValue())) {
			return;
		}

		try {
			compare((SingleProperty<T>) compareProperty, (SingleProperty<T>) againstProperty, value, against, propertyValidation);
		} catch (Exception e) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because exception occurred during validation. Exception message: " + e.getMessage());
			propertyValidation.addValidationError(validationError);
		}
	}

	/**
	 * Returns property defined by {@link #property} from the grouped property. If for some reason
	 * property can not be found, {@link PropertyValidation} will be filled with error(s) and
	 * <code>null</code> returned.
	 * 
	 * @param groupProperty
	 *            {@link GroupedProperty} to search in.
	 * @param propertyValidation
	 *            {@link PropertyValidation} to report errors.
	 * @return {@link SingleProperty} or <code>null</code> if finding fails.
	 */
	private SingleProperty<?> getProperty(GroupedProperty groupProperty, PropertyValidation propertyValidation) {
		if (StringUtils.isEmpty(property)) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because property logical name is not set.");
			propertyValidation.addValidationError(validationError);
			return null;
		}
		SingleProperty<?> p = groupProperty.forLogicalname(property);
		if (null == p) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because property with logical name '" + property + "' does not exist.");
			propertyValidation.addValidationError(validationError);
		}
		return p;
	}

	/**
	 * Returns property defined by {@link #this} from the grouped property. If for some reason
	 * property can not be found, {@link PropertyValidation} will be filled with error(s) and
	 * <code>null</code> returned.
	 * 
	 * @param groupProperty
	 *            {@link GroupedProperty} to search in.
	 * @param propertyValidation
	 *            {@link PropertyValidation} to report errors.
	 * @return {@link SingleProperty} or <code>null</code> if finding fails.
	 */
	private SingleProperty<?> getAgainstProperty(GroupedProperty groupProperty, PropertyValidation propertyValidation) {
		if (StringUtils.isEmpty(than)) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because logical name of the property to compare against is not set.");
			propertyValidation.addValidationError(validationError);
			return null;
		}
		SingleProperty<?> p = groupProperty.forLogicalname(than);
		if (null == p) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because property with logical name '" + than + "' does not exist.");
			propertyValidation.addValidationError(validationError);
		}
		return p;
	}

	/**
	 * Gets {@link #property}.
	 * 
	 * @return {@link #property}
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Sets {@link #property}.
	 * 
	 * @param property
	 *            New value for {@link #property}
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * Gets {@link #than}.
	 * 
	 * @return {@link #than}
	 */
	public String getThan() {
		return than;
	}

	/**
	 * Sets {@link #than}.
	 * 
	 * @param than
	 *            New value for {@link #than}
	 */
	public void setThan(String than) {
		this.than = than;
	}

}