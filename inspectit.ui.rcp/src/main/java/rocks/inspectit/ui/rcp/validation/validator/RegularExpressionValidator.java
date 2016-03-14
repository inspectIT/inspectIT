package rocks.inspectit.ui.rcp.validation.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IInputValidator;

/**
 * {@link IInputValidator} that works with regular expressions for validations.
 *
 * @author Ivan Senic
 *
 */
public class RegularExpressionValidator implements IInputValidator {

	/**
	 * Message when entered value is empty.
	 */
	private static final String MESSAGE_VALUE_EMPTY = "Specified value must not be empty.";

	/**
	 * {@link Pattern} for the reg-ex matching.
	 */
	private final Pattern pattern;

	/**
	 * Error message to display when validation fails.
	 */
	private final String errorMessage;

	/**
	 * Flag to accept the empty strings and not validate them.
	 */
	private final boolean allowEmpty;

	/**
	 * Default constructor. Does not validate empty strings as correct. Same as calling
	 * {@link #RegularExpressionValidator(String, String, false)}.
	 *
	 * @param regEx
	 *            Regular expression used in validation. Must correctly compile against
	 *            {@link Pattern#compile(String)}.
	 * @param errorMessage
	 *            Error message to display when validation fails.
	 */
	public RegularExpressionValidator(String regEx, String errorMessage) {
		this(regEx, errorMessage, false);
	}

	/**
	 * Secondary constructor.
	 *
	 * @param regEx
	 *            Regular expression used in validation. Must correctly compile against
	 *            {@link Pattern#compile(String)}.
	 * @param errorMessage
	 *            Error message to display when validation fails.
	 * @param allowEmpty
	 *            Pass <code>true</code> to accept the empty strings and not validate them.
	 */
	public RegularExpressionValidator(String regEx, String errorMessage, boolean allowEmpty) {
		this.pattern = Pattern.compile(regEx);
		this.errorMessage = errorMessage;
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

		if (pattern.matcher(newText).matches()) {
			return null;
		}

		return errorMessage;
	}

}
