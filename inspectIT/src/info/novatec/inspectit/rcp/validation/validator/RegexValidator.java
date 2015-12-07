/**
 *
 */
package info.novatec.inspectit.rcp.validation.validator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IInputValidator;

/**
 * {@link IInputValidator} for checking the correctly specified regular expression.
 *
 * @author Alexander Wert
 *
 */
public class RegexValidator implements IInputValidator {

	/**
	 * Message when entered value is empty.
	 */
	private static final String MESSAGE_VALUE_EMPTY = "Specified value must not be empty.";

	/**
	 * Message when entered value is not matching.
	 */
	private static final String MESSAGE_ERROR = "Specified value is not a valid regular expression.";

	/**
	 * Allow empty string.
	 */
	private final boolean allowEmpty;

	/**
	 * @param allowEmpty
	 *            If empty strings are valid.
	 */
	public RegexValidator(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String isValid(String newText) {
		if (StringUtils.isEmpty(newText)) {
			if (!allowEmpty) {
				return MESSAGE_VALUE_EMPTY;
			} else if (allowEmpty) {
				return null;
			}
		}
		try {
			Pattern.compile(newText);
		} catch (PatternSyntaxException e) {
			return MESSAGE_ERROR;
		}

		return null;
	}
}
