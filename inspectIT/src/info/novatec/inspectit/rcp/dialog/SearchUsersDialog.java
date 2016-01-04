package info.novatec.inspectit.rcp.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
/**
 * Dialog to search users.
 * 
 * @author Mario Rose
 *
 */
public class SearchUsersDialog extends TitleAreaDialog {
	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;
	/**
	 * email text box.
	 */
	private Text mailBox;
	/**
	 * Search user button.
	 */
	private Button searchButton;
	/**
	 * Reset button id.
	 */
	private static final int SEARCH_ID = 0; //IDialogConstants.OK_ID;
	/**
	 * Table to display users.
	 */
	private Table table;
	/**
	 * Array of all Roles.
	 */
	private List<Role> rolesList;
	/**
	 * List of all Users.
	 */
	private List<String> userList;
	/**
	 * Default constructor.
	 * @param parentShell
	 * 				Parent {@link Shell} to create Dialog on
	 * @param cmrRepositoryDefinition
	 * CmrRepositoryDefinition for easy access to security services.
	 */
	public SearchUsersDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
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
		this.setTitle("Search Users");
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
		textLabel.setText("Search Users");

		Label mailLabel = new Label(main, SWT.NONE);
		mailLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		mailLabel.setText("e-mail:");
		mailBox = new Text(main, SWT.BORDER);
		mailBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		table = new Table(parent, SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		  
		// columns
		final TableColumn emailColumn = new TableColumn(table, SWT.NONE);
		emailColumn.setText("E-Mail");
		emailColumn.pack();
		final TableColumn roleColumn = new TableColumn(table, SWT.NONE);
		roleColumn.setText("Role");
		roleColumn.pack();
		
		parent.pack();
		return main;
	}
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		searchButton = createButton(parent, SEARCH_ID, "Search", true);
		searchButton.setEnabled(true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (SEARCH_ID == buttonId) {
			searchPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}
	
	/**
	 * Searches for user with that email in database.
	 */
	private void searchPressed() {
		table.removeAll();
		if (userList.contains(mailBox.getText())) {
			User user = cmrRepositoryDefinition.getSecurityService().getUser(mailBox.getText());
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, user.getEmail());
			for (Role role : rolesList) {
		    	if (role.getId() == user.getRoleId()) {
		    		item.setText(1, role.getTitle());
		    	}
		    }
		} else {
			MessageDialog.openError(null, "No users found.", "We couldn't find a user with this email.");
			return;
		}
		resizeTable();
	}
	
	/**
	 * Resizes the table.
	 */
	private void resizeTable() {
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
		table.pack();
	}

}
