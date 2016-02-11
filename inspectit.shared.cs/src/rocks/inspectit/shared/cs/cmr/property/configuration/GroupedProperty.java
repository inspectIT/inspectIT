package info.novatec.inspectit.cmr.property.configuration;

import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidationException;
import info.novatec.inspectit.cmr.property.configuration.validator.IGroupedProperyValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.GreaterOrEqualValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.GreaterValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.LessOrEqualValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.LessValidator;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;

/**
 * Property that is consisted out of several {@link SingleProperty}ies.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "group-property")
public class GroupedProperty extends AbstractProperty {

	/**
	 * {@link SingleProperty} set that are forming this group.
	 */
	@XmlElementWrapper(name = "properties", required = true)
	@XmlElementRef
	private Set<SingleProperty<?>> singleProperties = new LinkedHashSet<SingleProperty<?>>();

	/**
	 * Validators for relations in the group.
	 */
	@XmlElementWrapper(name = "group-validators")
	@XmlElementRefs({ @XmlElementRef(type = LessValidator.class), @XmlElementRef(type = LessOrEqualValidator.class), @XmlElementRef(type = GreaterValidator.class),
			@XmlElementRef(type = GreaterOrEqualValidator.class) })
	private List<IGroupedProperyValidator> validators;

	/**
	 * No-arg constructors.
	 */
	public GroupedProperty() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            Display name of the property. Can not be <code>null</code>.
	 * @param description
	 *            Description providing more information on property.
	 * @throws IllegalArgumentException
	 *             If name or section are <code>null</code>.
	 * @see AbstractProperty#AbstractProperty(String, String)
	 */
	public GroupedProperty(String name, String description) throws IllegalArgumentException {
		super(name, description);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		if (CollectionUtils.isNotEmpty(singleProperties)) {
			for (SingleProperty<?> singleProperty : singleProperties) {
				if (singleProperty.isAdvanced()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isServerRestartRequired() {
		if (CollectionUtils.isNotEmpty(singleProperties)) {
			for (SingleProperty<?> singleProperty : singleProperties) {
				if (singleProperty.isServerRestartRequired()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validate(PropertyValidation propertyValidation) {
		// first execute validation on all single properties
		if (CollectionUtils.isNotEmpty(singleProperties)) {
			for (SingleProperty<?> property : singleProperties) {
				property.validate(propertyValidation);
			}
		}

		// then execute all grouped property validators
		if (CollectionUtils.isNotEmpty(validators)) {
			for (IGroupedProperyValidator validator : validators) {
				validator.validate(this, propertyValidation);
			}
		}
	}

	/**
	 * Validates with the group validators this property, based on the changes of the single
	 * properties reported by update list.
	 * 
	 * @param propertyUpdates
	 *            Information about updates.
	 * @throws PropertyValidationException
	 *             If validation fails.
	 */
	public void validateForPropertiesUpdate(Collection<IPropertyUpdate<?>> propertyUpdates) throws PropertyValidationException {
		PropertyValidation propertyValidation = PropertyValidation.createFor(this);

		for (IGroupedProperyValidator groupedProperyValidator : validators) {
			groupedProperyValidator.validateForPropertyUpdates(this, propertyUpdates, propertyValidation);
		}

		// if has errors raise exception, otherwise create property update
		if (propertyValidation.hasErrors()) {
			throw new PropertyValidationException(propertyValidation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(Properties properties) {
		if (CollectionUtils.isNotEmpty(singleProperties)) {
			for (SingleProperty<?> property : singleProperties) {
				property.register(properties);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SingleProperty<?> forLogicalname(String propertyLogicalName) {
		for (AbstractProperty property : singleProperties) {
			SingleProperty<?> returnProperty = property.forLogicalname(propertyLogicalName);
			if (null != returnProperty) {
				return returnProperty;
			}
		}
		return null;
	}

	/**
	 * Gets {@link #singleProperties}.
	 * 
	 * @return {@link #singleProperties}
	 */
	public Set<SingleProperty<?>> getSingleProperties() {
		return singleProperties;
	}

	/**
	 * Adds {@link SingleProperty} to this group.
	 * 
	 * @param property
	 *            {@link SingleProperty} to add.
	 */
	public void addSingleProperty(SingleProperty<?> property) {
		singleProperties.add(property);
	}

	/**
	 * Adds {@link IGroupedProperyValidator} to be used for validating this group.
	 * 
	 * @param validator
	 *            Validator.
	 */
	public void addValidator(IGroupedProperyValidator validator) {
		if (null == validators) {
			validators = new ArrayList<IGroupedProperyValidator>();
		}
		validators.add(validator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((singleProperties == null) ? 0 : singleProperties.hashCode());
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
		GroupedProperty other = (GroupedProperty) obj;
		if (singleProperties == null) {
			if (other.singleProperties != null) {
				return false;
			}
		} else if (!singleProperties.equals(other.singleProperties)) {
			return false;
		}
		return true;
	}

}
