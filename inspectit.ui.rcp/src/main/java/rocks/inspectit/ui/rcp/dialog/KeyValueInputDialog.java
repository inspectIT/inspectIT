package rocks.inspectit.ui.rcp.dialog;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for key/value pair definition.
 *
 * @author Ivan Senic
 *
 */
public class KeyValueInputDialog extends Dialog {

	/**
	 * Name box.
	 */
	private Text keyBox;

	/**
	 * Description box.
	 */
	private Text valueBox;

	/**
	 * OK button.
	 */
	private Control okButton;
	/**
	 * New name.
	 */
	private String key;

	/**
	 * New description.
	 */
	private String value;

	/**
	 * Dialog title to display.
	 */
	private final String dialogTitle;

	/**
	 * Dialog message to display.
	 */
	private final String dialogMessage;

	/**
	 * The key validator, or <code>null</code> if none.
	 */
	private final IInputValidator keyValidator;

	/**
	 * The key validator, or <code>null</code> if none.
	 */
	private final IInputValidator valueValidator;

	/**
	 * Text for the error message.
	 */
	private Text errorMessageText;

	/**
	 * Displayed error message.
	 */
	private String errorMessage;

	/**
	 * Default constructor.
	 *
	 * @param parentShell
	 *            Parent shell.
	 * @param dialogTitle
	 *            title for the dialog
	 * @param dialogMessage
	 *            message message for the dialog
	 * @param keyInitialValue
	 *            Initial value for the key, can be <code>null</code>
	 * @param keyValidator
	 *            Key validator, can be <code>null</code>
	 * @param valueInitialValue
	 *            Initial value for the value, can be <code>null</code>
	 * @param valueValidator
	 *            value validator, can be <code>null</code>
	 */
	public KeyValueInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String keyInitialValue, IInputValidator keyValidator, String valueInitialValue,
			IInputValidator valueValidator) {
		super(parentShell);

		Assert.isNotNull(dialogTitle);
		Assert.isNotNull(dialogMessage);

		this.dialogTitle = dialogTitle;
		this.dialogMessage = dialogMessage;

		if (null != keyInitialValue) {
			this.key = keyInitialValue;
		} else {
			this.key = "";
		}
		if (null != valueInitialValue) {
			this.value = valueInitialValue;
		} else {
			this.value = "";
		}

		this.keyValidator = keyValidator;
		this.valueValidator = valueValidator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (null != dialogTitle) {
			newShell.setText(dialogTitle);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);
		((GridLayout) main.getLayout()).numColumns = 2;

		if (null != dialogMessage) {
			Label messageLabel = new Label(main, SWT.NONE);
			GridData data = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			messageLabel.setLayoutData(data);
			messageLabel.setText(dialogMessage);
		}

		Label keyLabel = new Label(main, SWT.LEFT);
		keyLabel.setText("Key:");
		keyBox = new Text(main, SWT.BORDER);
		keyBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label valueLabel = new Label(main, SWT.LEFT);
		valueLabel.setText("Value:");
		valueBox = new Text(main, SWT.BORDER);
		valueBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// error message copied from input dialog
		errorMessageText = new Text(main, SWT.READ_ONLY | SWT.WRAP);
		GridData errorGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		errorGridData.horizontalSpan = 2;
		errorMessageText.setLayoutData(errorGridData);
		errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		// Set the error message text
		setErrorMessage(errorMessage);

		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isInputValid()) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
			}
		};

		keyBox.addModifyListener(modifyListener);
		valueBox.addModifyListener(modifyListener);

		applyDialogFont(main);
		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);

		keyBox.setFocus();
		if (key != null) {
			keyBox.setText(key);
			keyBox.selectAll();
		}
		if (value != null) {
			valueBox.setText(value);
		}
		isInputValid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			key = keyBox.getText();
			value = valueBox.getText();
		} else {
			key = null; // NOPMD
			value = null; // NOPMD
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Is input in textual boxes valid.
	 *
	 * @return Is input in textual boxes valid.
	 */
	private boolean isInputValid() {
		if (null != keyValidator) {
			String errorMessage = keyValidator.isValid(keyBox.getText());
			if (null != errorMessage) {
				setErrorMessage(errorMessage);
				return false;
			}
		}

		if (null != valueValidator) {
			String errorMessage = valueValidator.isValid(valueBox.getText());
			if (null != errorMessage) {
				setErrorMessage(errorMessage);
				return false;
			}
		}

		setErrorMessage(null);
		return true;
	}

	/**
	 * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
	 * <P>
	 * Copied from {@link org.eclipse.jface.dialogs.InputDialog}
	 *
	 * @param errorMessage
	 *            the error message, or <code>null</code> to clear
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		if ((errorMessageText != null) && !errorMessageText.isDisposed()) {
			errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
			// Disable the error message text control if there is no error, or
			// no error text (empty or whitespace only). Hide it also to avoid
			// color change.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
			boolean hasError = (errorMessage != null) && ((StringConverter.removeWhiteSpaces(errorMessage)).length() > 0);
			errorMessageText.setEnabled(hasError);
			errorMessageText.setVisible(hasError);
			errorMessageText.getParent().update();
			// Access the ok button by id, in case clients have overridden button creation.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
			Control button = getButton(IDialogConstants.OK_ID);
			if (button != null) {
				button.setEnabled(errorMessage == null);
			}
		}
	}

	/**
	 * Gets {@link #key}.
	 *
	 * @return {@link #key}
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets {@link #value}.
	 *
	 * @return {@link #value}
	 */
	public String getValue() {
		return value;
	}

}
