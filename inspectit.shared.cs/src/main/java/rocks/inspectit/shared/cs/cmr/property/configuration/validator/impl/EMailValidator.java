package rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.util.EMailUtils;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.AbstractSinglePropertyValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.ISinglePropertyValidator;

/**
 * Checks whether a given String value is a valid e-mail address.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "isValidEMail")
public class EMailValidator extends AbstractSinglePropertyValidator<String> implements ISinglePropertyValidator<String> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean prove(String value) {
		if (value.isEmpty()) {
			return true;
		}
		return EMailUtils.isValidEmailAddress(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getErrorMessage(SingleProperty<? extends String> property) {
		return "Value of property '" + property.getName() + "' is not a valid e-mail address";
	}
}
