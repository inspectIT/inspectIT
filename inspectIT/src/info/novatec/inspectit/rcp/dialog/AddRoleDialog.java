package info.novatec.inspectit.rcp.dialog;

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

import info.novatec.inspectit.communication.data.cmr.Permutation;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

public class AddRoleDialog extends TitleAreaDialog{


	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Mail address text box.
	 */
	private Text roleName;

	/**
	 * password text box.
	 */
	private Text passwordBox;

	/**
	 * Add user button.
	 */
	private Button addButton;
	
	/**
	 * Reset button id.
	 */
	private static final int ADD_ID = 0; //IDialogConstants.OK_ID;

	/**
	 * Array of all Users.
	 */
	private List<String> userList;
	
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
	 * @param parentShell
	 * 				Parent {@link Shell} to create Dialog on
	 * @param cmrRepositoryDefinition
	 * CmrRepositoryDefinition for easy access to security services.
	 */
	
	public AddRoleDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(parentShell);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		rolesList = cmrRepositoryDefinition.getSecurityService().getAllRoles();
		userList = cmrRepositoryDefinition.getSecurityService().getAllUsers();

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

		Label roleNameLabel = new Label(main, SWT.NONE);
		roleNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		roleNameLabel.setText("name:");
		roleName = new Text(main, SWT.BORDER);
		roleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label textPermissionLabel = new Label(main, SWT.NONE);
		textPermissionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 5));
		textPermissionLabel.setText("Mark the permissions, that the new role should have:");
		
	    Button cmrRecordingPermission = new Button(parent, SWT.CHECK);
	    Button cmrStoragePermission = new Button(parent, SWT.CHECK);
	    Button cmrDeleteAgentPermission = new Button(parent, SWT.CHECK);
	    Button cmrShutdownAndRestartPermission = new Button(parent, SWT.CHECK);
	    Button cmrAdministrationPermission = new Button(parent, SWT.CHECK);
	    
	    cmrRecordingPermission.setText("CMR Recording");
	    cmrStoragePermission.setText("CMR Storage");
	    cmrDeleteAgentPermission.setText("CMR Delete Agent");
	    cmrShutdownAndRestartPermission.setText("CMR Shutdown/Restart");
	    cmrAdministrationPermission.setText("Administration");
//	    (cmrRecordingPermission, cmrStoragePermission , cmrDeleteAgentPermission ,
//	    cmrShutdownAndRestartPermission, cmrAdministrationPermission
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
			okPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}

/*	
	
	
	private void addPressed() {
		if (userList.contains(mailBox.getText())){
			MessageDialog.openError(null, "Mail already exists!", "The Mail you chose is already taken! ");
			return;
		}
		long id = 0;
		int index = roles.getSelectionIndex();
	    String mail = mailBox.getText();
	    String password = passwordBox.getText();
	    String role = roles.getItem(index);
	    for (Role r : rolesList) {
	    	if (r.getTitle().equals(role)) {
	    		id = r.getId();
	    	}
	    }
	    User user = new User(Permutation.hashString(password), mail , id);
	    cmrRepositoryDefinition.getSecurityService().addUser(user);
		okPressed();
	}
	
	private boolean isInputValid() {
		if (mailBox.getText().isEmpty()) {
			return false;
		}
		if (passwordBox.getText().isEmpty()) {
			return false;
		}
		if (roles.getText() == "" ) {
			return false;
		} 
		return true;
	}
*/
}
