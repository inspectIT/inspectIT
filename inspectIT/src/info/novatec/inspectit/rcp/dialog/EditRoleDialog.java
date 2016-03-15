package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
 * Dialog for editing roles.
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
	 * Role-description text box.
	 */
	private Text roleDescriptionBox;

	/**
	 * Edit button.
	 */
	private Button editButton;
	
	/**
	 * Delete Role button.
	 */
	private Button deleteRoleButton;

	/**
	 * The role to edit.
	 */
	private Role roleOld;
	
	/**
	 * List of all Roles.
	 */
	private List<Role> rolesList;

	/**
	 * Edit button id.
	 */
	private static final int EDIT_ID = 0; //IDialogConstants.OK_ID;
	
	/**
	 * Delete role button id.
	 */
	private static final int DELETE_ROLE_ID = 2;
	/**
	 * List of permissions that the current user can give to the new role.
	 */
	private List<String> grantedPermissionsStrings = new ArrayList<String>(); 
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
		rolesList = cmrRepositoryDefinition.getSecurityService().getAllRoles();
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
		roleNameBoxLabel.setText("Name:");
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

		Label roleDescriptionBoxLabel = new Label(main, SWT.NONE);
		roleDescriptionBoxLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		roleDescriptionBoxLabel.setText("Description:");
		roleDescriptionBox = new Text(main, SWT.BORDER);
		roleDescriptionBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		deleteRoleButton = createButton(parent, DELETE_ROLE_ID, "Delete Role", true);
		deleteRoleButton.setEnabled(true);
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
		} else if (DELETE_ROLE_ID == buttonId) {
			deletePressed();
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
		List<Permission> rolePermissions = roleOld.getPermissions();
		for (Button but : grantedPermissionsButtons) {
			if (but.getSelection()) {
				for (Permission perm : allPermissions) {
					if (perm.getTitle().equals(but.getText())) {
						newPermissions.add(perm);
					}
				}
			}
		}
		boolean admin = false;
		for (Permission perm : rolePermissions) {
			if (perm.getTitle().equals("cmrAdministrationPermission")) { 
				admin = true;
			}
		}
		if (admin) {
			List<Role> adminRoles = new ArrayList<Role>();
			for	(Role role : rolesList) {
				List<Permission> permissions = role.getPermissions();
				for (Permission perm: permissions) {
					if (perm.getTitle().equals("cmrAdministrationPermission")) {
						adminRoles.add(role);
					}
				}
			}
			boolean stillAdmin = false;
			for (Permission perm : newPermissions) {
				if (perm.getTitle().equals("cmrAdministrationPermission")) {
					stillAdmin = true;
				}
			}
			if (!stillAdmin && adminRoles.size() < 2) {
				MessageDialog.openWarning(null, "Warning", "You are about to remove the last admin role. Please make sure there is at least one admin role remaining.");
				return;
			}
		}
		cmrRepositoryDefinition.getSecurityService().changeRoleAttribute(roleOld, name, roleDescriptionBox.getText(), newPermissions);
		okPressed();
	}
	
	/**
	 * Notifies that the delete role button has been pressed.
	 */
	private void deletePressed() {
		if (roleOld.getTitle().equals("guestRole")) {
			MessageDialog.openWarning(null, "Warning", "This role is required for guest access and can not be deleted.");
			return;
		}
		long id = roleOld.getId();
		List<String> userEmails = cmrRepositoryDefinition.getSecurityService().getUsersByRole(id);
		List<Permission> rolePermissions = roleOld.getPermissions();
		boolean admin = false;
		for (Permission perm : rolePermissions) {
			if (perm.getTitle().equals("cmrAdministrationPermission")) { 
				admin = true;
			}
		}
		if (admin) {
			List<Role> adminRoles = new ArrayList<Role>();
			for	(Role role : rolesList) {
				List<Permission> permissions = role.getPermissions();
				for (Permission perm: permissions) {
					if (perm.getTitle().equals("cmrAdministrationPermission")) {
						adminRoles.add(role);
					}
				}
			}
			if (adminRoles.size() < 2) {
				MessageDialog.openWarning(null, "Warning", "You are about to remove the last admin role. Please make sure there is at least one admin role remaining.");
				return;
			}
		}
		if (!userEmails.isEmpty()) {
			List<User> users = new ArrayList<User>();
			String warning = "By deleting this role the following users will also get deleted: ";
			for (String email : userEmails) {
				users.add(cmrRepositoryDefinition.getSecurityService().getUser(email));
				warning += " " + email;
			}
			Boolean confirm = MessageDialog.openConfirm(null, "Delete role", warning);
			if (!confirm) {
				return;
			}
			for (User user : users) {
				cmrRepositoryDefinition.getSecurityService().deleteUser(user);
			}
		}
		
		cmrRepositoryDefinition.getSecurityService().deleteRole(roleOld);
		okPressed();
	}
}
