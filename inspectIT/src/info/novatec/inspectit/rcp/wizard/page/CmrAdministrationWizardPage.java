package info.novatec.inspectit.rcp.wizard.page;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import info.novatec.inspectit.rcp.dialog.ForgotPasswordDialog;
import info.novatec.inspectit.rcp.dialog.ManageRolesDialog;
import info.novatec.inspectit.rcp.dialog.ShowPermissionsDialog;

/**
 * Wizard Page for managing users on the CMR.
 * 
 * @author Lucca Hellriegel
 *
 */
public class CmrAdministrationWizardPage extends WizardPage {
	/**
	 * Default page message.
	 */
	private static final String DEFAULT_MESSAGE = "Managing users and roles.";

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *            title for the CMR Administration Page
	 */
	public CmrAdministrationWizardPage(String title) {
		super(title);
		this.setTitle(title);
		this.setMessage(DEFAULT_MESSAGE);
	}

	/**
	 * The dialog to show available permissions.
	 */
	private ShowPermissionsDialog showPermissionsDialog;
	
	
	/**
	 * The dialog to manage available permissions.
	 */
	private ManageRolesDialog manageRolesDialog;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		Button roles = new Button(main, SWT.RIGHT);
		roles.setLayoutData(new GridData(SWT.BEGINNING, SWT.RIGHT, false, false));
		roles.setText("Roles");
		roles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showRolesDialog(main.getShell());
			}
		});
		
		
		Button permissions = new Button(main, SWT.CENTER);
		permissions.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		permissions.setText("Permissions");
		permissions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showPermDialog(main.getShell());
			}
		});

		

		setControl(main);
	}

	
	/**
	 * Dialog to show available permissions .
	 * 
	 * @param parentShell
	 *            parent shell for the {@link ShowPermissionsDialog}
	 */
	private void showPermDialog(Shell parentShell) {
		showPermissionsDialog = new ShowPermissionsDialog(parentShell);
		showPermissionsDialog.open();
	}
	
	/**
	 * Dialog to show available permissions .
	 * 
	 * @param parentShell
	 *            parent shell for the {@link ShowPermissionsDialog}
	 */
	private void showRolesDialog(Shell parentShell) {
		manageRolesDialog = new ManageRolesDialog(parentShell);
		manageRolesDialog.open();
	}
}
