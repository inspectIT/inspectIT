package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing users.
 * 
 * @author Mario Rose
 *
 */

public class EditUserDialog extends TitleAreaDialog {
	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;
	/**
	 * Mail address text box.
	 */
	private Text mailBox;

	/**
	 * password text box.
	 */
	private Text passwordBox;
	
	/**
	 * Boolean to see if user is locked.
	 */
	private boolean isLocked;
	
	/**
	 * Edit button.
	 */
	private Button editButton;

	/**
	 * Delete user button.
	 */
	private Button deleteUserButton;

	/**
	 * Dropdown menu for roles.
	 */
	private Combo roles;

	/**
	 * List of all Roles.
	 */
	private List<Role> rolesList;

	/**
	 * The user to edit.
	 */
	private User userOld;
	/**
	 * Reset button id.
	 */
	private static final int EDIT_ID = 0; //IDialogConstants.OK_ID;

	/**
	 * Delete user button id.
	 */
	private static final int DELETE_USER_ID = 2;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell} to create Dialog on
	 * @param cmrRepositoryDefinition
	 * CmrRepositoryDefinition for easy access to security services.
	 * @param user
	 * the user to edit.
	 */
	public EditUserDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition, User user) {
		super(parentShell);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		rolesList = cmrRepositoryDefinition.getSecurityService().getAllRoles();
		userOld = user;
		isLocked = userOld.isLocked();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Edit User");
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

		Label mailLabel = new Label(main, SWT.NONE);
		mailLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		mailLabel.setText("E-Mail:");
		mailBox = new Text(main, SWT.BORDER);
		mailBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		mailBox.setText(userOld.getEmail());

		Label passwordLabel = new Label(main, SWT.NONE);
		passwordLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		passwordLabel.setText("Password:");
		passwordBox = new Text(main, SWT.BORDER | SWT.PASSWORD);
		passwordBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		passwordBox.setText("");

		Label rolesLabel = new Label(main, SWT.NONE);
		rolesLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		rolesLabel.setText("Role:");
		roles = new Combo(main, SWT.READ_ONLY);
		for (Role role : rolesList) {
			roles.add(role.getTitle());
		}
		for (Role role : rolesList) {
			if (role.getId() == userOld.getRoleId()) {
				roles.select(roles.indexOf(role.getTitle()));
			}
		}

		final Button checkBox = new Button(main, SWT.CHECK);
		checkBox.setText("Lock user?");
		checkBox.setSelection(isLocked);
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = checkBox.getSelection();
                if (selected) {
                	isLocked = true;
                } else {
                	isLocked = false;
                }
            }
		});
               
		
		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		deleteUserButton = createButton(parent, DELETE_USER_ID, "Delete User", true);
		deleteUserButton.setEnabled(true);
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
		} else if (DELETE_USER_ID == buttonId) {
			deletePressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}

	/**
	 * Notifies that the edit button has been pressed.
	 */
	private void editPressed() {
		long id = 0;
		boolean passwordChanged = true;
		int index = roles.getSelectionIndex();
		String mail = mailBox.getText();
		String password = passwordBox.getText();
		String role = roles.getItem(index);
		for (Role r : rolesList) {
			if (r.getTitle().equals(role)) {
				id = r.getId();
			}
		}
		if (passwordBox.getText().isEmpty()) {
			passwordChanged = false;
		}
		cmrRepositoryDefinition.getSecurityService().changeUserAttribute(userOld, mail, password, id, passwordChanged, isLocked, cmrRepositoryDefinition.getSessionId());
		okPressed();
	}

	/**
	 * Notifies that the delete user button has been pressed.
	 */
	private void deletePressed() {
		if (userOld.getEmail().equals("guest")) {
			MessageDialog.openWarning(null, "Warning", "This user is required for guest access and can not be deleted.");
			return;
		}
		Role userRole = cmrRepositoryDefinition.getSecurityService().getRoleOfUser(userOld.getEmail());
		List<Permission> userPermissions = userRole.getPermissions();
		boolean admin = false;
		for (Permission perm : userPermissions) {
			if (perm.getTitle().equals("cmrAdministrationPermission")) { 
				admin = true;
			}
		}
		if (admin) {
			List<Role> adminRoles = new ArrayList<Role>();
			List<String> adminUsers = new ArrayList<String>();
			for	(Role role : rolesList) {
				List<Permission> rolePermissions = role.getPermissions();
				for (Permission perm: rolePermissions) {
					if (perm.getTitle().equals("cmrAdministrationPermission")) {
						adminRoles.add(role);
					}
				}
			}
			for (Role role : adminRoles) {
				long id = role.getId();
				adminUsers.addAll(cmrRepositoryDefinition.getSecurityService().getUsersByRole(id));
			}
			if (adminUsers.size() < 2) {
				MessageDialog.openWarning(null, "Warning", "You are about to remove the last admin user. Please make sure there is at least one admin remaining.");
				return;
			}
		} 
		Boolean confirm = MessageDialog.openConfirm(null, "Delete user", "Do you really want to delete this user?");
		if (!confirm) {
			return;
		}
		cmrRepositoryDefinition.getSecurityService().deleteUser(userOld, cmrRepositoryDefinition.getSessionId());
		okPressed();

	}
}


