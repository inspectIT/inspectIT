package info.novatec.inspectit.rcp.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
	private Text searchBox;
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
	 * Dropdown menu for search options.
	 */
	private Combo searchOptions;
	/**
	 * Array of all Roles.
	 */
	private List<Role> rolesList;
	/**
	 * List of all Users.
	 */
	private List<String> userList;
	/**
	 * {@link EditUserDialog}.
	 */
	private EditUserDialog editUserDialog;
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

		Label searchBoxLabel = new Label(main, SWT.NONE);
		searchBoxLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		searchBoxLabel.setText("Search:");
		searchBox = new Text(main, SWT.BORDER);
		searchBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label searchOptionsLabel = new Label(main, SWT.NONE);
		searchOptionsLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		searchOptionsLabel.setText("Search for:");
		searchOptions = new Combo(main, SWT.READ_ONLY);
		searchOptions.add("E-Mail");
		searchOptions.add("Role");
		searchOptions.setText("");

		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTable.heightHint = 100;
		table.setLayoutData(gdTable);
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

		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionIndex() != -1) {
					TableItem[] tableItems = table.getItems();
					User user = cmrRepositoryDefinition.getSecurityService().getUser(tableItems[table.getSelectionIndex()].getText(0));
					userDialog(main.getShell(), user);
				}
			}
		});

		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isInputValid()) {
					searchButton.setEnabled(true);
				} else {
					searchButton.setEnabled(false);
				}
			}
		};

		searchBox.addModifyListener(modifyListener);
		searchOptions.addModifyListener(modifyListener);



		return main;
	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		searchButton = createButton(parent, SEARCH_ID, "Search", true);
		searchButton.setEnabled(false);
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
		userList = cmrRepositoryDefinition.getSecurityService().getAllUsers();
		table.removeAll();
		if (searchOptions.getText().equals("E-Mail")) {
			if (userList.contains(searchBox.getText())) {
				User user = cmrRepositoryDefinition.getSecurityService().getUser(searchBox.getText());
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, user.getEmail());
				for (Role role : rolesList) {
					if (role.getId() == user.getRoleId()) {
						item.setText(1, role.getTitle());
					}
				}
			}
		} else if (searchOptions.getText().equals("Role")) {
			long id = 0; //Role IDs start at 1.
			for (Role role : rolesList) {
				if (role.getTitle().equals(searchBox.getText())) {
					id = role.getId();
				}
			}
			if (id != 0) {
				List<String> users = cmrRepositoryDefinition.getSecurityService().getUsersByRole(id);
				for (String user : users) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, user);
					item.setText(1, searchBox.getText());
				}
			}
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
	}

	/**
	 * Checks if the input is not null.
	 * @return true if not null.
	 */
	private boolean isInputValid() {
		if (searchBox.getText().isEmpty()) {
			return false;
		}
		if ("".equals(searchOptions.getText())) {
			return false;
		} 
		return true;
	}
	
	/**
	 * Dialog in case a user is clicked in the table.
	 * 
	 * @param parentShell
	 *            parent shell for the {@link EditUserDialog}
	 * @param user
	 * 		 	  the user to edit.
	 */
	private void userDialog(Shell parentShell, User user) {
		editUserDialog = new EditUserDialog(parentShell, cmrRepositoryDefinition, user);
		editUserDialog.open();
		parentShell.close();
	}

}
