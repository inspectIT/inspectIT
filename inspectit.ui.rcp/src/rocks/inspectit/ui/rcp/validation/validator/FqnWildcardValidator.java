package info.novatec.inspectit.rcp.validation.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IInputValidator;

/**
 * {@link IInputValidator} for checking the correctly specified FQN of a class with or without using
 * our '*' wild-card.
 */
public class FqnWildcardValidator implements IInputValidator {

	/**
	 * Message when entered value is empty.
	 */
	private static final String MESSAGE_VALUE_EMPTY = "Specified value must not be empty.";

	/**
	 * Message when entered value is not matching (no primitives allowed).
	 */
	private static final String MESSAGE_ERROR_NO_PRIMITIVES = "Specified value is not valid because it will match no class";

	/**
	 * Message when entered value is not matching (primitives allowed).
	 */
	private static final String MESSAGE_ERROR_PRIMITIVES = "Specified value is not valid because it will match no class nor primitive type.";

	/**
	 * Pattern to use when matching.
	 */
	private static final Pattern PATTERN = Pattern.compile("([a-zA-Z_$\\*][a-zA-Z\\d_$\\*]*\\.)*[a-zA-Z_$\\*][a-zA-Z\\d_$\\*]*");

	/**
	 * Allow empty string.
	 */
	private final boolean allowEmpty;

	/**
	 * If primitives should be allowed.
	 */
	private final boolean allowPrimitives;

	/**
	 * @param allowEmpty
	 *            If empty strings are valid.
	 * @param allowPrimitives
	 *            If primitives should be allowed.
	 */
	public FqnWildcardValidator(boolean allowEmpty, boolean allowPrimitives) {
		this.allowEmpty = allowEmpty;
		this.allowPrimitives = allowPrimitives;
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

		if (allowPrimitives) {
			return MESSAGE_ERROR_PRIMITIVES;
		} else {
			return MESSAGE_ERROR_NO_PRIMITIVES;
		}
	}

}
