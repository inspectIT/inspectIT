package info.novatec.inspectit.rcp.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * Dialog to add a new role.
 * 
 * @author Mario Rose
 * @author Thomas Sachs
 * @author Lucca Hellriegel
 *
 */
public class AddRoleDialog extends TitleAreaDialog {

	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Role-name text box.
	 */
	private Text roleNameBox;


	/**
	 * Role-description text box.
	 */
	private Text roleDescriptionBox;

	/**
	 * Add role button.
	 */
	private Button addButton;

	/**
	 * Reset button id.
	 */
	private static final int ADD_ID = 0; // IDialogConstants.OK_ID;

	/**
	 * Array of all Roles.
	 */
	private List<Role> rolesList;

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
	 *            CmrRepositoryDefinition for easy access to security services.
	 */

	public AddRoleDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(parentShell);
		rolesList = cmrRepositoryDefinition.getSecurityService().getAllRoles();
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Add Role");
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
		textLabel.setText("Add new Role");

		Label roleNameBoxLabel = new Label(main, SWT.NONE);
		roleNameBoxLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		roleNameBoxLabel.setText("Name:");
		roleNameBox = new Text(main, SWT.BORDER);
		roleNameBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label roleDescriptionBoxLabel = new Label(main, SWT.NONE);
		roleDescriptionBoxLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		roleDescriptionBoxLabel.setText("Description:");
		roleDescriptionBox = new Text(main, SWT.BORDER);
		roleDescriptionBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label textPermissionLabel = new Label(main, SWT.NONE);
		textPermissionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 5));
		textPermissionLabel.setText("Mark the permissions, that the new role should have:");
		List<Permission> grantedPermissions = cmrRepositoryDefinition.getGrantedPermissions();
		for (int i = 0; i < grantedPermissions.size(); i++) {
			this.grantedPermissionsStrings.add(grantedPermissions.get(i).getTitle());
			}
		this.grantedPermissionsButtons = new Button[grantedPermissionsStrings.size()];
		for (int i = 0; i < grantedPermissionsStrings.size(); i++) {
			grantedPermissionsButtons[i] = new Button(parent, SWT.CHECK);
			grantedPermissionsButtons[i].setText(grantedPermissionsStrings.get(i));
		}

		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isInputValid()) {
					addButton.setEnabled(true);
				} else {
					addButton.setEnabled(false);
				}
			}
		};

		roleNameBox.addModifyListener(modifyListener);
		
		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		addButton = createButton(parent, ADD_ID, "Add Role", true);
		addButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (ADD_ID == buttonId) {
			addPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}

	/**
	 * Finishes the role-creation.
	 */
	private void addPressed() {
		for (Role list : rolesList) {
			if (list.getTitle().equals(roleNameBox.getText())) {
				MessageDialog.openError(null, "Title already exists!", "The Title you chose is already taken! ");
				return;
			}
		}

		String name = roleNameBox.getText();
		List<String> rolePermissions = new ArrayList<String>();
		for (int i = 0; i < grantedPermissionsStrings.size(); i++) {
			if (grantedPermissionsButtons[i].getSelection()) {
				rolePermissions.add(grantedPermissionsStrings.get(i));
			}
		}
		String similarRoles = "";
		for (Role list : rolesList) {
			Set<String> permissionSetExisting = new HashSet<String>();
			for (Permission permission : list.getPermissions()) {
				permissionSetExisting.add(permission.getTitle());
			}
			Set<String> permissionSetNew = new HashSet<String>(rolePermissions);
			if (permissionSetExisting.equals(permissionSetNew)) {
				similarRoles += "\"" + list.getTitle() + "\", ";
			}
		}

		if (!"".equals(similarRoles)) {
			String warning = "One or more roles with the same set of permissions already exists." + "\n" + "\nDetected roles: " + similarRoles.substring(0, similarRoles.length() - 2) + "\n" 
					+ "\nDo you really want to add the role \"" + name + "\"?";
			Boolean confirm = MessageDialog.openConfirm(null, "Similar role already exists!", warning);
			if (!confirm) {
				return;
					
			}
		}
		
		cmrRepositoryDefinition.getSecurityService().addRole(name, rolePermissions, roleDescriptionBox.getText());
		okPressed();
	}

	/**
	 * Checks if the name-box is not empty.
	 * 
	 * @return true if not empty
	 */
	private boolean isInputValid() {
		return !roleNameBox.getText().isEmpty();
	}

}
