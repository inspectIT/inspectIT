package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.SafeExecutor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.forms.widgets.BusyIndicator;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Dialog for add repository definition action.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("restriction")
public class AddCmrRepositoryDefinitionDialog extends TitleAreaDialog {

	/**
	 * Name text box.
	 */
	private Text nameBox;

	/**
	 * IP text box.
	 */
	private Text ipBox;

	/**
	 * Port text box.
	 */
	private Text portBox;

	/**
	 * Description box.
	 */
	private Text descriptionBox;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition = null;

	/**
	 * OK button.
	 */
	private Button okButton;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Shell.
	 */
	public AddCmrRepositoryDefinitionDialog(Shell parentShell) {
		super(parentShell);
		setDefaultImage(InspectIT.getDefault().getImage(InspectITImages.IMG_WIZBAN_SERVER));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add CMR Repository Definition");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Add CMR Repository Definition");
		this.setMessage("Define information for the repository to add", IMessageProvider.INFORMATION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(4, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumWidth = 400;
		gd.minimumHeight = 250;
		main.setLayoutData(gd);

		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Server name:");
		nameBox = new Text(main, SWT.BORDER);
		nameBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		Label ipLabel = new Label(main, SWT.LEFT);
		ipLabel.setText("IP Address:");
		ipBox = new Text(main, SWT.BORDER);
		ipBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ipBox.setText(CmrRepositoryDefinition.DEFAULT_IP);

		Label portLabel = new Label(main, SWT.LEFT);
		portLabel.setText("Port:");
		portBox = new Text(main, SWT.BORDER);
		portBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		portBox.setText(String.valueOf(CmrRepositoryDefinition.DEFAULT_PORT));

		Label descLabel = new Label(main, SWT.LEFT);
		descLabel.setText("Description:");
		descLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		descriptionBox = new Text(main, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		descriptionBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		new Label(main, SWT.LEFT);
		final Button testConnection = new Button(main, SWT.PUSH);
		testConnection.setText("Test connection");
		testConnection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final BusyIndicator busyIndicator = new BusyIndicator(main, SWT.NONE);
		busyIndicator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Label testLabel = new Label(main, SWT.LEFT);
		testLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		testConnection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				testConnection.setEnabled(false);
				testLabel.setText("Testing..");
				busyIndicator.setBusy(true);
				final String ip = ipBox.getText().trim();
				final int port = Integer.parseInt(portBox.getText());
				Job checkCmr = new Job("Checking online status..") {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						CmrRepositoryDefinition cmr = new CmrRepositoryDefinition(ip, port);
						boolean testOk = false;
						try {
							cmr.refreshOnlineStatus();
							testOk = cmr.getOnlineStatus() == OnlineStatus.ONLINE;
						} catch (Exception exception) {
							testOk = false;
						}
						final boolean testOkFinal = testOk;
						SafeExecutor.asyncExec(new Runnable() {
							public void run() {
								if (!busyIndicator.isDisposed() && !testLabel.isDisposed()) {
									if (busyIndicator.isBusy()) {
										busyIndicator.setBusy(false);
									}
									if (testOkFinal) {
										testLabel.setText("Succeeded");
										busyIndicator.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK));
									} else {
										testLabel.setText("Failed");
										busyIndicator.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CLOSE));
									}
								}
							}
						}, busyIndicator, testLabel);
						return Status.OK_STATUS;
					}
				};
				checkCmr.setUser(false);
				checkCmr.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_SERVER_REFRESH_SMALL));
				checkCmr.schedule();
			}
		});

		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isInputValid()) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
			}
		};
		nameBox.addModifyListener(modifyListener);
		ipBox.addModifyListener(modifyListener);
		portBox.addModifyListener(modifyListener);

		ModifyListener testModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!ipBox.getText().isEmpty() && !portBox.getText().isEmpty()) {
					testConnection.setEnabled(true);
				} else {
					testConnection.setEnabled(false);
				}
				testLabel.setText("");
				busyIndicator.setImage(null);
				if (busyIndicator.isBusy()) {
					busyIndicator.setBusy(false);
				}
			}
		};
		ipBox.addModifyListener(testModifyListener);
		portBox.addModifyListener(testModifyListener);

		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			cmrRepositoryDefinition = new CmrRepositoryDefinition(ipBox.getText().trim(), Integer.parseInt(portBox.getText()), nameBox.getText().trim());
			if (!descriptionBox.getText().trim().isEmpty()) {
				cmrRepositoryDefinition.setDescription(descriptionBox.getText().trim());
			} else {
				cmrRepositoryDefinition.setDescription("");
			}
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * @return the cmrRepositoryDefinition
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * Is input in textual boxes valid.
	 * 
	 * @return Is input in textual boxes valid.
	 */
	private boolean isInputValid() {
		if (nameBox.getText().isEmpty()) {
			return false;
		}
		if (ipBox.getText().isEmpty()) {
			return false;
		}
		if (portBox.getText().isEmpty()) {
			return false;
		} else {
			try {
				Integer.parseInt(portBox.getText());
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
}
