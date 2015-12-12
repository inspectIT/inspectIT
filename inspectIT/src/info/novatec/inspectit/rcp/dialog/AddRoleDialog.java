package info.novatec.inspectit.rcp.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Permutation;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * Dialog to add a new role.
 * 
 * @author Mario Rose
 * @author Thomas Sachs
 *
 */
public class AddRoleDialog extends TitleAreaDialog {

	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Mail address text box.
	 */
	private Text roleNameBox;

	/**
	 * Add user button.
	 */
	private Button addButton;

	/**
	 * Reset button id.
	 */
	private static final int ADD_ID = 0; // IDialogConstants.OK_ID;

	/**
	 * Checkbutton for Recording Permission
	 */
	private Button cmrRecordingPermissionButton;

	/**
	 * Checkbutton for Storage Permission
	 */
	private Button cmrStoragePermissionButton;

	/**
	 * Checkbutton for Delete Agent Permission
	 */
	private Button cmrDeleteAgentPermissionButton;

	/**
	 * Checkbutton for Shutdown And Restart Permission
	 */
	private Button cmrShutdownAndRestartPermissionButton;

	/**
	 * Checkbutton for Administration Permission
	 */
	private Button cmrAdministrationPermissionButton;

	/**
	 * Array of all Roles.
	 */
	private List<Role> rolesList;

	/**
	 * Dropdown menu for roles.
	 */
	private Combo roles;

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
		roleNameBoxLabel.setText("name:");
		roleNameBox = new Text(main, SWT.BORDER);
		roleNameBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label textPermissionLabel = new Label(main, SWT.NONE);
		textPermissionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 5));
		textPermissionLabel.setText("Mark the permissions, that the new role should have:");

		cmrRecordingPermissionButton = new Button(parent, SWT.CHECK);
		cmrStoragePermissionButton = new Button(parent, SWT.CHECK);
		cmrDeleteAgentPermissionButton = new Button(parent, SWT.CHECK);
		cmrShutdownAndRestartPermissionButton = new Button(parent, SWT.CHECK);
		cmrAdministrationPermissionButton = new Button(parent, SWT.CHECK);

		cmrRecordingPermissionButton.setText("CMR Recording");
		cmrStoragePermissionButton.setText("CMR Storage");
		cmrDeleteAgentPermissionButton.setText("CMR Delete Agent");
		cmrShutdownAndRestartPermissionButton.setText("CMR Shutdown/Restart");
		cmrAdministrationPermissionButton.setText("Administration");

		
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

	private void addPressed() {
		System.out.println();
		if (rolesList.contains(roleNameBox.getText())) {
			MessageDialog.openError(null, "Role already exists!", "The Role you chose is already taken! ");
			return;
		}
		String name = roleNameBox.getText();
		List<Permission> rolePermissions = new ArrayList<Permission>();
		if (cmrRecordingPermissionButton.isEnabled()) {
			rolePermissions.add(new Permission("cmrRecordingPermission", "Permission start recording from Agent"));
		}
		if (cmrShutdownAndRestartPermissionButton.isEnabled()) {
			rolePermissions.add(new Permission("cmrShutdownAndRestartPermission", "Permission for shuting down and restarting the CMR"));
		}
		if (cmrStoragePermissionButton.isEnabled()) {
			rolePermissions.add(new Permission("cmrStoragePermission", "Permission for accessing basic storage options"));
		}
		if (cmrDeleteAgentPermissionButton.isEnabled()) {
			rolePermissions.add(new Permission("cmrDeleteAgentPermission", "Permission for deleting Agent"));
		}
		if (cmrAdministrationPermissionButton.isEnabled()){
			rolePermissions.add(new Permission("cmrAdministrationPermission", "Permission for accessing the CMR Administration"));
		}

		 Role role = new Role(name, rolePermissions);
		 cmrRepositoryDefinition.getSecurityService().addRole(role);
		okPressed();
	}

	private boolean isInputValid() {
		if (roleNameBox.getText().isEmpty()) {
			return false;
		}
		return true;
	}

}
