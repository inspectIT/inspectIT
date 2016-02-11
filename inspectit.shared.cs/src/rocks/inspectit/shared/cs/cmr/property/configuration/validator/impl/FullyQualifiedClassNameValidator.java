package rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.AbstractSinglePropertyValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.ISinglePropertyValidator;

/**
 * Is FQN validator.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "isFullyQualifiedClassName")
public class FullyQualifiedClassNameValidator extends AbstractSinglePropertyValidator<String> implements ISinglePropertyValidator<String> {

	/**
	 * {@inheritDoc}
	 */
	protected boolean prove(String value) {
		return StringUtils.isNotEmpty(value) && value.matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getErrorMessage(SingleProperty<? extends String> property) {
		return "Value of property '" + property.getName() + "' must be a fully qualified class name";
	}
}
