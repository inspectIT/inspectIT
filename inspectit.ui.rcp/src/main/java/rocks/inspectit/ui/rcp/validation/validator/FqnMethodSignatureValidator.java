package rocks.inspectit.ui.rcp.validation.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IInputValidator;

/**
 * {@link IInputValidator} for checking the correctly specified FQN of a method signature with or
 * without using our '*' wild-card.
 *
 * @author Alexander Wert
 *
 */
public class FqnMethodSignatureValidator implements IInputValidator {

	/**
	 * Message when entered value is empty.
	 */
	private static final String MESSAGE_VALUE_EMPTY = "Specified value must not be empty.";

	/**
	 * Message when entered value is not matching (no primitives allowed).
	 */
	private static final String MESSAGE_ERROR = "Specified value is not valid because it will match no method signature";

	/**
	 * Pattern to use when matching.
	 */
	private static final Pattern PATTERN = Pattern
			.compile("([A-Za-z_$][a-zA-Z\\d_$]*\\.)+[A-Za-z_$][a-zA-Z\\d_$]*\\(((([A-Za-z_][a-zA-Z\\d_$]*\\.)*[A-Za-z_][a-zA-Z\\d_$]*,)*([A-Za-z_][a-zA-Z\\d_$]*\\.)*[A-Za-z_][a-zA-Z\\d_$]*)?\\)");

	/**
	 * Allow empty string.
	 */
	private final boolean allowEmpty;

	/**
	 * @param allowEmpty
	 *            If empty strings are valid.
	 */
	public FqnMethodSignatureValidator(boolean allowEmpty) {
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

		if (PATTERN.matcher(newText).matches()) {
			return null;
		}

		return MESSAGE_ERROR;
	}
}
