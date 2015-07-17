package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.forms.widgets.BusyIndicator;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * The wizard page that displays the CMR info and checks for the connection status.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("restriction")
public class PreviewCmrDataWizardPage extends WizardPage {

	/**
	 * Name label.
	 */
	private Label name;

	/**
	 * IP label.
	 */
	private Label ip;

	/**
	 * Description label.
	 */
	private Label description;

	/**
	 * Connection test result label.
	 */
	private Label connectionTest;

	/**
	 * Busy indicator shown while test is executed.
	 */
	private BusyIndicator busyIndicator;

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * Job for checking the CMR status.
	 */
	private Job checkCmrJob;

	/**
	 * Version label.
	 */
	private Label version;

	/**
	 * Default constructor.
	 */
	public PreviewCmrDataWizardPage() {
		super("Preview CMR Data");
		this.setTitle("Preview CMR Data");
		this.setMessage("Preview the entered CMR Repository data and confirm");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(3, false));

		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Server name:");
		name = new Label(main, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Label ipLabel = new Label(main, SWT.LEFT);
		ipLabel.setText("IP Address:");
		ip = new Label(main, SWT.NONE);
		ip.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		ip.setText(CmrRepositoryDefinition.DEFAULT_IP);

		Label descLabel = new Label(main, SWT.LEFT);
		descLabel.setText("Description:");
		descLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		description = new Label(main, SWT.WRAP);
		description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		new Label(main, SWT.LEFT).setText("Connection test:");
		connectionTest = new Label(main, SWT.NONE);
		connectionTest.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		busyIndicator = new BusyIndicator(main, SWT.NONE);
		busyIndicator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		new Label(main, SWT.LEFT).setText("Version:");
		version = new Label(main, SWT.NONE);
		version.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		setControl(main);
	}

	/**
	 * Updates the representation with a given {@link CmrRepositoryDefinition}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public void update(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		name.setText(cmrRepositoryDefinition.getName());
		ip.setText(cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort());
		description.setText(cmrRepositoryDefinition.getDescription());
		connectionTest.setText("Checking..");
		busyIndicator.setBusy(true);
		version.setText("");
		main.layout();
		checkCmrJob = new CheckCmrJob(cmrRepositoryDefinition);
		checkCmrJob.schedule();
	}

	/**
	 * Cancels the checking if it is active.
	 */
	public void cancel() {
		if (null != checkCmrJob) {
			checkCmrJob.cancel();
		}
		busyIndicator.setBusy(false);
		main.layout();
	}

	/**
	 * Checking of the CRM job.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CheckCmrJob extends Job {

		/**
		 * Is job canceled.
		 */
		private boolean isCanceled = false;

		/**
		 * CMR to check.
		 */
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * Default constructor.
		 * 
		 * @param cmrRepositoryDefinition
		 *            CMR to check.
		 */
		public CheckCmrJob(CmrRepositoryDefinition cmrRepositoryDefinition) {
			super("Checking online status..");
			setUser(false);
			setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_SERVER_REFRESH_SMALL));
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IStatus run(IProgressMonitor monitor) {
			if (null == cmrRepositoryDefinition) {
				return Status.CANCEL_STATUS;
			}
			boolean testOk = false;
			try {
				cmrRepositoryDefinition.refreshOnlineStatus();
				testOk = cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE;
			} catch (Exception exception) {
				testOk = false;
			}

			String ver = null;
			if (isCanceled) {
				return Status.CANCEL_STATUS;
			} else if (testOk) {
				ver = cmrRepositoryDefinition.getVersion();
			}

			final boolean testOkFinal = testOk;
			final String verFinal = ver;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!main.isDisposed()) {
						busyIndicator.setBusy(false);
						if (testOkFinal) {
							connectionTest.setText("Succeeded");
							version.setText(verFinal);
						} else {
							connectionTest.setText("Failed");
							version.setText("n/a");
						}
						main.layout();
					}
				}
			});
			return Status.OK_STATUS;
		}

		/**
		 * {@inheritDoc}
		 */
		protected void canceling() {
			isCanceled = true;
		};

	}

}
