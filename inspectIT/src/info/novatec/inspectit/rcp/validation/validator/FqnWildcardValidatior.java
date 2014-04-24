package info.novatec.inspectit.rcp.validation.validator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IInputValidator;

/**
 * {@link IInputValidator} for checking the correctly specified FQN of a class with or without using
 * our '*' wild-card.
 */
public class FqnWildcardValidatior implements IInputValidator {

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
	public FqnWildcardValidatior(boolean allowEmpty, boolean allowPrimitives) {
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


		if (newText.matches("([a-zA-Z_$\\*][a-zA-Z\\d_$\\*]*\\.)*[a-zA-Z_$\\*][a-zA-Z\\d_$\\*]*")) {
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
