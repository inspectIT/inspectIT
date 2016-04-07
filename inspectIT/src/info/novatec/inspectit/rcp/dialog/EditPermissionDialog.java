package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing permissions with parameters.
 * 
 * @author Lucca Hellriegel
 *
 */
public class EditPermissionDialog extends TitleAreaDialog {

	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Edit button.
	 */
	private Button editButton;

	/**
	 * The permission to edit.
	 */
	private Permission permissionOld;

	/**
	 * Permission-parameter text box.
	 */
	private Text permissionParameterBox;

	/**
	 * Description-parameter text box.
	 */
	private Text permissionDescriptionBox;

	/**
	 * Reset button id.
	 */
	private static final int EDIT_ID = 0; // IDialogConstants.OK_ID;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell} to create Dialog on
	 * @param cmrRepositoryDefinition
	 *            CmrRepositoryDefinition for easy access to security services.
	 * @param permission
	 *            The permission that is edited.
	 */
	public EditPermissionDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition,
			Permission permission) {
		super(parentShell);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		permissionOld = permission;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle(permissionOld.getTitle());
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

		Label permissionParameterLabel = new Label(main, SWT.NONE);
		permissionParameterLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		permissionParameterLabel.setText("Parameter:");
		permissionParameterBox = new Text(main, SWT.BORDER);
		permissionParameterBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		permissionParameterBox.setText(permissionOld.getParameter());

		Label permissionDescriptionLabel = new Label(main, SWT.NONE);
		permissionDescriptionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		permissionDescriptionLabel.setText("Description:");
		permissionDescriptionBox = new Text(main, SWT.BORDER);
		permissionDescriptionBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		permissionDescriptionBox.setText(permissionOld.getDescription());

		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		editButton = createButton(parent, EDIT_ID, "Edit", true);
		editButton.setEnabled(true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (EDIT_ID == buttonId) {
			editPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}

	/**
	 * Notifies that the edit button has been pressed.
	 */
	private void editPressed() {
		cmrRepositoryDefinition.getSecurityService().changePermissionAttributes(permissionOld, permissionOld.getTitle(),
				permissionDescriptionBox.getText(), permissionParameterBox.getText());
		okPressed();
	}

}
