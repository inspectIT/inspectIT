package info.novatec.inspectit.cmr.property.configuration.validator.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.validator.AbstractSinglePropertyValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.ISinglePropertyValidator;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Is not empty validator. Works on strings, collections and maps.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
@XmlRootElement(name = "isNotEmpty")
public class NotEmptyValidator<T extends Object> extends AbstractSinglePropertyValidator<T> implements ISinglePropertyValidator<T> {

	/**
	 * {@inheritDoc}
	 */
	protected boolean prove(T value) {
		if (value instanceof String) {
			return StringUtils.isNotEmpty((String) value);
		} else if (value instanceof Collection) {
			return CollectionUtils.isNotEmpty((Collection<?>) value);
		} else if (value instanceof Map) {
			return MapUtils.isNotEmpty((Map<?, ?>) value);
		} else {
			throw new RuntimeException("The isNotEmpty Validator not used with String, Collection or Map object. Passed object is " + value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getErrorMessage(SingleProperty<? extends T> property) {
		return "Value of property '" + property.getName() + "' must not be empty String/Collection/Map";
	}
}
