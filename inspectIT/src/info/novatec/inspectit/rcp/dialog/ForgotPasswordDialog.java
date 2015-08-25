package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.rcp.wizard.page.CmrLoginWizardPage;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for resetting password in case Button in {@link CmrLoginWizardPage} is pressed.
 * 
 * @author Clemens Geibel
 *
 */

public class ForgotPasswordDialog extends TitleAreaDialog {

	/**
	 * Mail address text box.
	 */
	private Text mailBox;

	/**
	 * Reset button.
	 */
	private Button resetButton;
	
	/**
	 * Reset button id.
	 */
	private static final int RESET_ID = 0; //IDialogConstants.OK_ID;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell} to create Dialog on
	 */
	public ForgotPasswordDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Reset Password");
		this.setMessage("Send a new password to your e-mail address", IMessageProvider.WARNING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 400;
		gd.heightHint = 100;
		main.setLayoutData(gd);

		Label textLabel = new Label(main, SWT.NONE);
		textLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 5));
		textLabel.setText("In case you have forgotten your password, you can reset it below.\nYour new password will be sent to your e-mail address.\n\n\n");

		Label mailLabel = new Label(main, SWT.NONE);
		mailLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		mailLabel.setText("Enter your e-mail:");
		mailBox = new Text(main, SWT.BORDER);
		mailBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		resetButton = createButton(parent, RESET_ID, "Reset", true);
		resetButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (RESET_ID == buttonId) {
			resetPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}
	
	/**
	 * Notifies that the reset button has been pressed.
	 */
	private void resetPressed() {
		okPressed();
	}
}
