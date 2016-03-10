package info.novatec.inspectit.rcp.dialog;

import java.util.List;

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
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * 
 * @author Phil Szalay
 * @author Mario Rose
 *
 */

public class ShowAllRolesDialog extends TitleAreaDialog {
	
	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;
	
	/**
	 * List of all Roles.
	 */
	private List<Role> roles;
	/**
	 * Table to display roles.
	 */
	private Table table;
	/**
	 * {@link EditRoleDialog}.
	 */
	private EditRoleDialog editRoleDialog;
	/**
	 * Default constructor.
	 * @param parentShell
	 * 				Parent {@link Shell} to create Dialog on
	 * @param cmrRepositoryDefinition
	 * CmrRepositoryDefinition for easy access to security services.
	 */
	public ShowAllRolesDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(parentShell);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		roles = cmrRepositoryDefinition.getSecurityService().getAllRoles();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Show all roles");
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
		textLabel.setText("All roles are shown below.");
		
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTable.heightHint = 200;
		table.setLayoutData(gdTable);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText("Role");
		column1.pack();
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText("Permissions");
		column2.pack();
		updateTable();
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionIndex() != -1) {
					TableItem[] tableItems = table.getItems();
					long id = 0;
					for (Role r : roles) {
				    	if (r.getTitle().equals(tableItems[table.getSelectionIndex()].getText(0))) {
				    		id = r.getId();
				    	}
				    }
					Role role = cmrRepositoryDefinition.getSecurityService().getRoleByID(id);
					roleDialog(main.getShell(), role);
					roles = cmrRepositoryDefinition.getSecurityService().getAllRoles();
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
	 * @param role
	 * 		 	  the role to edit.
	 */
	private void roleDialog(Shell parentShell, Role role) {
		editRoleDialog = new EditRoleDialog(parentShell, cmrRepositoryDefinition, role);
		editRoleDialog.open();
	}
	/**
	 * updates the table.
	 */
	private void updateTable() {
		table.removeAll();
		
		// content for rows
		for (int i = 0; i < roles.size(); i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, roles.get(i).getTitle());
			
			List<Permission> permissions = roles.get(i).getPermissions();
			String perm = "";
			for (int k = 0; k < permissions.size(); k++) {
				perm += permissions.get(k).getTitle() + ", ";
			}
			
			item.setText(1, perm);
		}
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
	}

}