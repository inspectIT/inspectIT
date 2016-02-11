package info.novatec.inspectit.rcp.validation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IMessageManager;

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
	 * Default constructor. Uses message manager if supplied to handle messages.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param messageManager
	 *            Message manager to report to. Can be <code>null</code>
	 * @param inputValidator
	 *            {@link IInputValidator} that performs validation.
	 * @see ControlDecoration
	 */
	public InputValidatorControlDecoration(Text control, IMessageManager messageManager, IInputValidator inputValidator) {
		this(control, messageManager, null, inputValidator);
	}

	/**
	 * Alternative constructor. Does not use message manager to report messages. Registers listener
	 * to the list of validation listeners.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param listener
	 *            {@link IControlValidationListener}.
	 * @param inputValidator
	 *            {@link IInputValidator} that performs validation.
	 */
	public InputValidatorControlDecoration(Text control, IControlValidationListener listener, IInputValidator inputValidator) {
		this(control, null, listener, inputValidator);
	}

	/**
	 * Constructor allowing to set all properties. Registers listener to the list of validation
	 * listeners.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param messageManager
	 *            Message manager to report to. Can be <code>null</code>
	 * @param listener
	 *            {@link IControlValidationListener}.
	 * @param inputValidator
	 *            {@link IInputValidator} that performs validation.
	 */
	public InputValidatorControlDecoration(Text control, IMessageManager messageManager, IControlValidationListener listener, IInputValidator inputValidator) {
		super(control, messageManager, listener);

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
