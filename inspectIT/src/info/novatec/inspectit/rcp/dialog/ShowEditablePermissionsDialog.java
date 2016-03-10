package info.novatec.inspectit.rcp.dialog;

import java.util.List;

import info.novatec.inspectit.communication.data.cmr.Permission;
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
 * @author Lucca Hellriegel
 */
public class ShowEditablePermissionsDialog extends TitleAreaDialog {
	/**
	 * CmrRepositoryDefinition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;
	
	/**
	 * List of all permissions.
	 */
	private List<Permission> allPermissions;

	/**
	 * Table to display permissions.
	 */
	private Table table;

	/**
	 * {@link EditPermissionDialog}.
	 */
	private EditPermissionDialog editPermissionDialog;
	/**
	 * Default constructor.
	 * @param parentShell
	 * 				Parent {@link Shell} to create Dialog on
	 * @param cmrRepositoryDefinition
	 * CmrRepositoryDefinition for easy access to security services.
	 */
	public ShowEditablePermissionsDialog(Shell parentShell, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(parentShell);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		allPermissions = cmrRepositoryDefinition.getSecurityService().getAllPermissions();
			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Edit parameters for permissions");
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
		textLabel.setText("All permissions are shown below:");

		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTable.heightHint = 200;
		table.setLayoutData(gdTable);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// columns
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		TableColumn column3 = new TableColumn(table, SWT.NONE);
		column1.setText("Permissions");
		column1.pack();
		column2.setText("Parameters");
		column2.pack();
		column3.setText("Description");
		column3.pack();
		updateTable();
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionIndex() != -1) {
					Permission permission = allPermissions.get(table.getSelectionIndex());
					permissionsDialog(main.getShell(), permission);
					allPermissions = cmrRepositoryDefinition.getSecurityService().getAllPermissions();
					updateTable();
				}
			}
		});
		parent.pack();

		return main;
	}	

	/**
	 * Dialog in case a permission is clicked in the table.
	 * 
	 * @param parentShell
	 *            parent shell for the {@link EditPermissionDialog}
	 * @param permission
	 * 		 	  the permission to edit.
	 */
	private void permissionsDialog(Shell parentShell, Permission permission) {
		editPermissionDialog = new EditPermissionDialog(parentShell, cmrRepositoryDefinition, permission);
		editPermissionDialog.open();
	}

	/**
	 * updates the table.
	 */
	private void updateTable() {
		table.removeAll();
		// content for rows
		for (int i = 0; i < allPermissions.size(); i++) {
			TableItem item = new TableItem(table, SWT.NONE);

			item.setText(0, allPermissions.get(i).getTitle());
			item.setText(1, allPermissions.get(i).getParameter());	
			item.setText(2, allPermissions.get(i).getDescription());
			
		}
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
	}

}
