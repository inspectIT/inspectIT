package rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.shared.all.util.EMailUtils;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.AbstractSinglePropertyValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.ISinglePropertyValidator;

/**
 * Checks a comma separated list (in a string) whether it contains an invalid e-mail address.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "isValidEMailList")
public class EMailListValidator extends AbstractSinglePropertyValidator<String> implements ISinglePropertyValidator<String> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean prove(String value) {
		return value.isEmpty() || (getInvalidEmailAddress(value) == null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getErrorMessage(SingleProperty<? extends String> property) {
		return "At least one entry in property '" + property.getName() + "' is not a valid e-mail address";
	}

	/**
	 * Checks whether the given comma separated list string contains an invalid e-mail address.
	 *
	 * @param commaSeparatedList
	 *            String to check.
	 * @return Returns the invalid e-mail address or null if all e-mail addresses are valid.
	 */
	private String getInvalidEmailAddress(String commaSeparatedList) {
		String[] strArray = StringUtils.splitPreserveAllTokens(commaSeparatedList, ',');
		for (int i = 0; i < strArray.length; i++) {
			if (!EMailUtils.isValidEmailAddress(strArray[i])) {
				return strArray[i];
			}
		}
		return null;
	}
}
