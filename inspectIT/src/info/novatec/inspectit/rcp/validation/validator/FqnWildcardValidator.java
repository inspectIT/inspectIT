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
				return "Specified value must not be empty.";
			} else if (allowEmpty) {
				return null;
			}
		}

		if (PATTERN.matcher(newText).matches()) {
			return null;
		}

		String error = "Specified value is not valid because it will match no class";
		if (allowPrimitives) {
			error += " nor primitive type.";
		} else {
			error += ".";
		}

		return error;
	}

}
