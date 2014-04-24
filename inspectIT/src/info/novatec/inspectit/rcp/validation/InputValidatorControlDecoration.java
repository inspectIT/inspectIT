package info.novatec.inspectit.rcp.validation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Text;

/**
 * {@link ValidationControlDecoration} that uses {@link InputValidatorControlDecoration} for
 * determining if input is valid and for setting the correct error message.
 * 
 * @author Ivan Senic
 * 
 */
public class InputValidatorControlDecoration extends ValidationControlDecoration<Text> {

	/**
	 * {@link IInputValidator} that performs validation.
	 */
	private final IInputValidator inputValidator;

	/**
	 * Default constructor.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param inputValidator
	 *            {@link IInputValidator} that performs validation.
	 * @see ControlDecoration
	 */
	public InputValidatorControlDecoration(Text control, IInputValidator inputValidator) {
		super(control);

		Assert.isNotNull(inputValidator);
		this.inputValidator = inputValidator;

		startupValidation();
	}

	/**
	 * Secondary constructor. Registers listener to the list of validation listeners.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param listener
	 *            {@link IControlValidationListener}.
	 * @param inputValidator
	 *            {@link IInputValidator} that performs validation.
	 */
	public InputValidatorControlDecoration(Text control, IControlValidationListener listener, IInputValidator inputValidator) {
		super(control, listener);

		Assert.isNotNull(inputValidator);
		this.inputValidator = inputValidator;

		startupValidation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean validate(Text control) {
		if (null == inputValidator) {
			return true;
		}

		String errorMessage = inputValidator.isValid(control.getText());

		if (null != errorMessage) {
			setDescriptionText(errorMessage);
			return false;

		} else {
			return true;
		}
	}

}
