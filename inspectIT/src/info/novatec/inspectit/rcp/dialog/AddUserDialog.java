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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;

import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
/**
 * Dialog to add a new user.
 * 
 * @author Mario Rose
 * @author Thomas Sachs
 * @author Lucca Hellriegel
 *
 */
public class AddUserDialog extends TitleAreaDialog {

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
	
	public AddUserDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
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
		this.setTitle("Add User");
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
		textLabel.setText("Add new User");

		Label mailLabel = new Label(main, SWT.NONE);
		mailLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		mailLabel.setText("e-mail:");
		mailBox = new Text(main, SWT.BORDER);
		mailBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label passwordLabel = new Label(main, SWT.NONE);
		passwordLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		passwordLabel.setText("password:");
		passwordBox = new Text(main, SWT.BORDER);
		passwordBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label rolesLabel = new Label(main, SWT.NONE);
		rolesLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		rolesLabel.setText("role:");
		roles = new Combo(main, SWT.READ_ONLY);
	    for (Role role : rolesList) {
	    	roles.add(role.getTitle());
	    }
	    roles.setText("");
	    
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
		
		mailBox.addModifyListener(modifyListener);
		passwordBox.addModifyListener(modifyListener);
		roles.addModifyListener(modifyListener);

		return main;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		addButton = createButton(parent, ADD_ID, "Add User", true);
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
	 * Adds the new user to database.
	 */
	private void addPressed() {
		if (userList.contains(mailBox.getText())) {
			MessageDialog.openError(null, "Mail already exists!", "The mail you chose is already taken! ");
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
	    User user = new User(password, mail , id);
	    cmrRepositoryDefinition.getSecurityService().addUser(user);
		okPressed();
	}
	
	/**
	 * Checks if the input is not null.
	 * @return true if not null.
	 */
	private boolean isInputValid() {
		if (mailBox.getText().isEmpty()) {
			return false;
		}
		if (passwordBox.getText().isEmpty()) {
			return false;
		}
		if (roles.getText() == "") {
			return false;
		} 
		return true;
	}
	
}