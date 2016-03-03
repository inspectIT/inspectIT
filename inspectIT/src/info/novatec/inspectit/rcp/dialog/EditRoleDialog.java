package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.page.CmrLoginWizardPage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
 * @author Mario Rose
 *
 */

public class EditRoleDialog extends TitleAreaDialog {
	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;
	/**
	 * role name text box.
	 */
	private Text roleNameBox;

	/**
	 * Edit button.
	 */
	private Button editButton;

	/**
	 * The role to edit.
	 */
	private Role roleOld;

	/**
	 * Reset button id.
	 */
	private static final int EDIT_ID = 0; //IDialogConstants.OK_ID;
	/**
	 * List of permissions that the current user can give to the new role.
	 */
	private List<String> grantedPermissionsStrings; 
	/**
	 * Array of buttons to display the permissions that can be granted.
	 */
	private Button[] grantedPermissionsButtons; 
	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell} to create Dialog on
	 * @param cmrRepositoryDefinition
	 * CmrRepositoryDefinition for easy access to security services.
	 * @param role
	 * the role to edit.
	 */
	public EditRoleDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition, Role role) {
		super(parentShell);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		roleOld = role;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Edit Role");
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

		Label roleNameBoxLabel = new Label(main, SWT.NONE);
		roleNameBoxLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		roleNameBoxLabel.setText("name:");
		roleNameBox = new Text(main, SWT.BORDER);
		roleNameBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		roleNameBox.setText(roleOld.getTitle());

		Label textPermissionLabel = new Label(main, SWT.NONE);
		textPermissionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 5));
		textPermissionLabel.setText("Mark the permissions, that the new role should have:");
		List<Permission> permissionList = roleOld.getPermissions();
		List<Permission> grantedPermissions = cmrRepositoryDefinition.getGrantedPermissions();
		for (int i = 0; i < grantedPermissions.size(); i++) {
			this.grantedPermissionsStrings.add(grantedPermissions.get(i).getTitle());
			}
		this.grantedPermissionsButtons = new Button[grantedPermissionsStrings.size()];
		for (int i = 0; i < grantedPermissionsStrings.size(); i++) {
			grantedPermissionsButtons[i] = new Button(parent, SWT.CHECK);
			grantedPermissionsButtons[i].setText(grantedPermissionsStrings.get(i));
			for (Permission perm : permissionList) {
				if (perm.getTitle().equals(grantedPermissionsStrings.get(i))) { 
					grantedPermissionsButtons[i].setSelection(true);
				}
			}
		}


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
		String name = roleNameBox.getText();
		List<Permission> allPermissions = cmrRepositoryDefinition.getSecurityService().getAllPermissions();
		List<Permission> newPermissions = new ArrayList<Permission>();
		for (Button but : grantedPermissionsButtons) {
			if (but.getSelection()) {
				for (Permission perm : allPermissions) {
					if (perm.getTitle().equals(but.getText())) {
						newPermissions.add(perm);
					}
				}
			}
		}
		cmrRepositoryDefinition.getSecurityService().changeRoleAttribute(roleOld, name, newPermissions);
		okPressed();
	}
}
