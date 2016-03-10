package info.novatec.inspectit.rcp.dialog;

import java.util.List;

import info.novatec.inspectit.communication.data.cmr.User;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * 
 * @author Phil Szalay
 * @author Mario Rose
 */

public class ShowAllUsersDialog extends TitleAreaDialog {

	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * List of all Emails.
	 */
	private List<String> users;

	/**
	 * Table to display users.
	 */
	private Table table;

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
	public ShowAllUsersDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(parentShell);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		users = cmrRepositoryDefinition.getSecurityService().getAllUsers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Show all Users");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, true));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 400;
		gd.heightHint = 100;
		main.setLayoutData(gd);

		Label textLabel = new Label(main, SWT.NONE);
		textLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 5));
		textLabel.setText("All users are shown below.");

		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTable.heightHint = 200;
		table.setLayoutData(gdTable);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// columns
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column1.setText("Name");
		column1.pack();
		column2.setText("Role");
		column2.pack();
		updateTable();
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionIndex() != -1) {
					TableItem[] tableItems = table.getItems();
					User user = cmrRepositoryDefinition.getSecurityService().getUser(tableItems[table.getSelectionIndex()].getText(0));
					userDialog(main.getShell(), user);
					users = cmrRepositoryDefinition.getSecurityService().getAllUsers();
					updateTable();
				}
			}
		});
		parent.pack();

		return main;
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
	}

	/**
	 * updates the table.
	 */
	private void updateTable() {
		table.removeAll();
		// content for rows
		for (int i = 0; i < users.size(); i++) {
			TableItem item = new TableItem(table, SWT.NONE);

			String role = cmrRepositoryDefinition.getSecurityService().getRoleOfUser(users.get(i)).getTitle();
			
			item.setText(0, users.get(i));
			item.setText(1, role);	
			}
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
	}

}