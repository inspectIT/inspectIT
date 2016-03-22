package info.novatec.inspectit.rcp.form;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.recording.RecordingState;
import info.novatec.inspectit.util.ObjectUtils;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.progress.UIJob;

/**
 * Class having a form for displaying the properties of a {@link CmrRepositoryDefinition}.
 * 
 * @author Ivan Senic
 * 
 */
public class CmrRepositoryPropertyForm implements ISelectionChangedListener {

	/**
	 * Number of max characters displayed for CMR description.
	 */
	private static final int MAX_DESCRIPTION_LENGTH = 150;

	/**
	 * {@link CmrRepositoryDefinition} to be displayed.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Current recording data.
	 */
	private RecordingData recordingData;

	/**
	 * Job for recording end count-down.
	 */
	private RecordCountdownJob recordCountdownJob = new RecordCountdownJob();

	/**
	 * Job for updating the CMR properties.
	 */
	private UpdateCmrPropertiesJob updateCmrPropertiesJob = new UpdateCmrPropertiesJob();

	private Composite mainComposite; // NOCHK
	private FormToolkit toolkit; // NOCHK
	private ManagedForm managedForm; // NOCHK
	private ScrolledForm form; // NOCHK
	private Label address; // NOCHK
	private FormText description; // NOCHK
	private Label recordingIcon; // NOCHK
	private Label recordingStorage; // NOCHK
	private Label status; // NOCHK
	private Label bufferDate; // NOCHK
	private ProgressBar bufferBar; // NOCHK
	private ProgressBar recTimeBar; // NOCHK
	private Label recordingStatusIcon; // NOCHK
	private Label recordingLabel; // NOCHK
	private Label version; // NOCHK
	private Label bufferSize; // NOCHK
	private Label recTime; // NOCHK
	private Label spaceLeftLabel; // NOCHK
	private ProgressBar spaceLeftBar; // NOCHK
	private Label uptimeLabel; // NOCHK
	private Label databaseSizeLabel; // NOCHK

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent composite.
	 */
	public CmrRepositoryPropertyForm(Composite parent) {
		this(parent, null);
	}

	/**
	 * Secondary constructor. Set the displayed {@link CmrRepositoryDefinition}.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @param cmrRepositoryDefinition
	 *            Displayed CMR.
	 */
	public CmrRepositoryPropertyForm(Composite parent, CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.managedForm = new ManagedForm(parent);
		this.toolkit = managedForm.getToolkit();
		this.form = managedForm.getForm();
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		initWidget();
	}

	/**
	 * Instantiate the widgets.
	 */
	private void initWidget() {
		Composite body = form.getBody();
		body.setLayout(new TableWrapLayout());
		managedForm.getToolkit().decorateFormHeading(form.getForm());
		mainComposite = toolkit.createComposite(body, SWT.NONE);
		mainComposite.setLayout(new TableWrapLayout());
		mainComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// START - General section
		Section generalSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		generalSection.setText("General information");

		Composite generalComposite = toolkit.createComposite(generalSection, SWT.NONE);
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		generalComposite.setLayout(tableWrapLayout);
		generalComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		toolkit.createLabel(generalComposite, "Address:");
		address = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "Version:");
		version = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "Description:");
		description = toolkit.createFormText(generalComposite, true);
		description.setLayoutData(new TableWrapData(TableWrapData.FILL));
		description.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showCmrDescriptionBox();
			}
		});

		toolkit.createLabel(generalComposite, "Status:");
		status = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "Uptime:");
		uptimeLabel = toolkit.createLabel(generalComposite, null, SWT.WRAP);
		uptimeLabel.setToolTipText("Date started represents date/time on machine where CMR has been launched");

		toolkit.createLabel(generalComposite, "Database size:");
		databaseSizeLabel = toolkit.createLabel(generalComposite, null, SWT.WRAP);
		databaseSizeLabel.setToolTipText("Current size of the database on the CMR");

		generalSection.setClient(generalComposite);
		generalSection.setLayout(new TableWrapLayout());
		generalSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - General section

		// START - Buffer section
		Section bufferSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		bufferSection.setText("Buffer status");

		Composite bufferSectionComposite = toolkit.createComposite(bufferSection, SWT.NONE);
		tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		bufferSectionComposite.setLayout(tableWrapLayout);
		bufferSectionComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		toolkit.createLabel(bufferSectionComposite, "Data in buffer since:");
		bufferDate = toolkit.createLabel(bufferSectionComposite, null, SWT.WRAP);

		toolkit.createLabel(bufferSectionComposite, "Buffer occupancy:");
		bufferBar = new ProgressBar(bufferSectionComposite, SWT.SMOOTH);
		bufferBar.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// help label
		toolkit.createLabel(bufferSectionComposite, null);

		bufferSize = toolkit.createLabel(bufferSectionComposite, null, SWT.CENTER);
		bufferSize.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		bufferSection.setClient(bufferSectionComposite);
		bufferSection.setLayout(new TableWrapLayout());
		bufferSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - Buffer section

		// START - Storage section
		Section storageSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		storageSection.setText("Storage status");

		Composite storageSectionComposite = toolkit.createComposite(storageSection, SWT.NONE);
		tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		storageSectionComposite.setLayout(tableWrapLayout);
		storageSectionComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		toolkit.createLabel(storageSectionComposite, "Storage space left:");

		spaceLeftBar = new ProgressBar(storageSectionComposite, SWT.SMOOTH);
		spaceLeftBar.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// help label
		toolkit.createLabel(storageSectionComposite, null);

		spaceLeftLabel = toolkit.createLabel(storageSectionComposite, null, SWT.CENTER);
		spaceLeftLabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		toolkit.createLabel(storageSectionComposite, "Recording:");
		Composite recordingHelpComposite = toolkit.createComposite(storageSectionComposite);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		recordingHelpComposite.setLayout(gl);
		recordingIcon = toolkit.createLabel(recordingHelpComposite, null, SWT.WRAP);
		recordingLabel = toolkit.createLabel(recordingHelpComposite, null, SWT.WRAP);

		toolkit.createLabel(storageSectionComposite, "Recording status:");
		recordingStatusIcon = toolkit.createLabel(storageSectionComposite, null, SWT.NONE);

		toolkit.createLabel(storageSectionComposite, "Recording storage:");
		recordingStorage = toolkit.createLabel(storageSectionComposite, null, SWT.WRAP);

		toolkit.createLabel(storageSectionComposite, "Recording time left:");

		recTimeBar = new ProgressBar(storageSectionComposite, SWT.SMOOTH);
		recTimeBar.setVisible(false);
		recTimeBar.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// help label
		toolkit.createLabel(storageSectionComposite, null);

		//
		recTime = toolkit.createLabel(storageSectionComposite, null, SWT.CENTER);
		recTime.setVisible(false);
		recTime.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		storageSection.setClient(storageSectionComposite);
		tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		storageSection.setLayout(tableWrapLayout);
		storageSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - Storage section

		refreshData();
	}

	/**
	 * Sets layout data for the form.
	 * 
	 * @param layoutData
	 *            LayoutData.
	 */
	public void setLayoutData(Object layoutData) {
		form.setLayoutData(layoutData);
	}

	/**
	 * Refreshes the property form.
	 */
	public void refresh() {
		refreshData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (!selection.isEmpty() && selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (!(firstElement instanceof Component)) {
				// it is possible that the PendingAdapterUpdate is in the selection because it
				// is still loading the agents
				return;
			}

			while (firstElement != null) {
				if (firstElement instanceof ICmrRepositoryProvider) { // NOPMD
					if (!ObjectUtils.equals(cmrRepositoryDefinition, ((ICmrRepositoryProvider) firstElement).getCmrRepositoryDefinition())) {
						cmrRepositoryDefinition = ((ICmrRepositoryProvider) firstElement).getCmrRepositoryDefinition();
						refreshData();
					}
					return;
				}
				firstElement = ((Component) firstElement).getParent();
			}
		}
		if (null != cmrRepositoryDefinition) {
			cmrRepositoryDefinition = null; // NOPMD
			refreshData();
		}

	}

	/**
	 * Shows the description box.
	 */
	private void showCmrDescriptionBox() {
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		PopupDialog popupDialog = new PopupDialog(form.getShell(), shellStyle, true, false, false, false, false, "CMR description", "CMR description") {
			private static final int CURSOR_SIZE = 15;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				Text text = toolkit.createText(parent, null, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = 3;
				gd.verticalIndent = 3;
				text.setLayoutData(gd);
				text.setText(cmrRepositoryDefinition.getDescription());
				return composite;
			}

			@Override
			protected Point getInitialLocation(Point initialSize) {
				// show popup relative to cursor
				Display display = getShell().getDisplay();
				Point location = display.getCursorLocation();
				location.x += CURSOR_SIZE;
				location.y += CURSOR_SIZE;
				return location;
			}

			@Override
			protected Point getInitialSize() {
				return new Point(400, 200);
			}
		};
		popupDialog.open();

	}

	/**
	 * Refreshes the data on the view.
	 */
	private void refreshData() {
		// we only schedule if the cancel returns true
		// because cancel fails when job is currently in process
		if (updateCmrPropertiesJob.cancel()) {
			updateCmrPropertiesJob.schedule();
		}
	}

	/**
	 * Updates buffer data.
	 * 
	 * @param cmrStatusData
	 *            Status data.
	 */
	private void updateCmrManagementData(CmrStatusData cmrStatusData) {
		boolean dataLoaded = false;
		if (null != cmrStatusData) {
			dataLoaded = true;
			// Transfer to MB right away
			double bufferMaxOccupancy = (double) cmrStatusData.getMaxBufferSize() / (1024 * 1024);
			double bufferCurrentOccupancy = (double) cmrStatusData.getCurrentBufferSize() / (1024 * 1024);
			bufferBar.setMaximum((int) Math.round(bufferMaxOccupancy));
			bufferBar.setSelection((int) Math.round(bufferCurrentOccupancy));
			int occupancy = (int) (100 * Math.round(bufferCurrentOccupancy) / Math.round(bufferMaxOccupancy));

			String occMb = NumberFormatter.humanReadableByteCount(cmrStatusData.getCurrentBufferSize());
			String maxMb = NumberFormatter.humanReadableByteCount(cmrStatusData.getMaxBufferSize());
			String string = occupancy + "% (" + occMb + " / " + maxMb + ")";
			bufferSize.setText(string);

			DefaultData oldestData = cmrStatusData.getBufferOldestElement();
			if (null != oldestData) {
				bufferDate.setText(NumberFormatter.formatTime(oldestData.getTimeStamp().getTime()));
			} else {
				bufferDate.setText("-");
			}

			// hard drive space data
			int spaceOccupancy = (int) (100 * (double) cmrStatusData.getStorageDataSpaceLeft() / cmrStatusData.getStorageMaxDataSpace());
			StringBuilder spaceLeftStringBuilder = new StringBuilder(String.valueOf(spaceOccupancy));
			spaceLeftStringBuilder.append("% (");
			spaceLeftStringBuilder.append(NumberFormatter.humanReadableByteCount(cmrStatusData.getStorageDataSpaceLeft()));
			spaceLeftStringBuilder.append(" / ");
			spaceLeftStringBuilder.append(NumberFormatter.humanReadableByteCount(cmrStatusData.getStorageMaxDataSpace()));
			spaceLeftStringBuilder.append(')');
			spaceLeftLabel.setText(spaceLeftStringBuilder.toString());
			spaceLeftBar.setMaximum((int) (cmrStatusData.getStorageMaxDataSpace() / 1024 / 1024));
			spaceLeftBar.setSelection((int) (cmrStatusData.getStorageDataSpaceLeft() / 1024 / 1024));
			if (!cmrStatusData.isCanWriteMore()) {
				spaceLeftBar.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				spaceLeftBar.setToolTipText("Space left is critically low and no write is possible anymore");
			} else if (cmrStatusData.isWarnSpaceLeftActive()) {
				spaceLeftBar.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW));
				spaceLeftBar.setToolTipText("Space left is reaching critical level");
			} else {
				spaceLeftBar.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
				spaceLeftBar.setToolTipText("Enough space left");
			}

			// uptime info
			long uptimeMillis = cmrStatusData.getUpTime();
			Date started = cmrStatusData.getDateStarted();
			StringBuilder uptimeText = new StringBuilder(NumberFormatter.humanReadableMillisCount(uptimeMillis, true));
			uptimeText.append(" (started ");
			uptimeText.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(started));
			uptimeText.append(')');
			uptimeLabel.setText(uptimeText.toString());

			// database info
			Long databaseSize = cmrStatusData.getDatabaseSize();
			if (null != databaseSize) {
				databaseSizeLabel.setText(NumberFormatter.humanReadableByteCount(databaseSize.longValue()));
			} else {
				databaseSizeLabel.setText("n/a");
			}
		}

		if (!dataLoaded) {
			bufferDate.setText("");
			bufferBar.setMaximum(Integer.MAX_VALUE);
			bufferBar.setSelection(0);
			bufferSize.setText("");
			spaceLeftBar.setMaximum(Integer.MAX_VALUE);
			spaceLeftBar.setSelection(0);
			spaceLeftBar.setToolTipText("");
			spaceLeftLabel.setText("");
			uptimeLabel.setText("");
			databaseSizeLabel.setText("");
		}
	}

	/**
	 * Updates recording data.
	 * 
	 * @param recordingData
	 *            Recording information.
	 */
	private void updateRecordingData(RecordingData recordingData) {
		boolean countdownJobActive = false;
		boolean dataLoaded = false;
		recordingIcon.setImage(null);
		// recording information
		if (null != recordingData) {
			RecordingState recordingState = cmrRepositoryDefinition.getStorageService().getRecordingState();
			if (recordingState == RecordingState.ON) {
				recordingIcon.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_RECORD));
				recordingLabel.setText("Active");
			} else if (recordingState == RecordingState.SCHEDULED) {
				recordingIcon.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_RECORD_SCHEDULED));
				recordingLabel.setText("Scheduled @ " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(recordingData.getRecordStartDate()));
			}
			// get the storage name
			StorageData storage = recordingData.getRecordingStorage();
			if (null != storage) {
				recordingStorage.setText(storage.getName());
			} else {
				recordingStorage.setText("");
			}

			// check if the recording time is limited
			if (null != recordingData.getRecordEndDate()) {
				countdownJobActive = true;
			} else {
				recTimeBar.setVisible(false);
				recTime.setVisible(false);
			}

			// recording status stuff
			recordingStatusIcon.setImage(ImageFormatter.getWritingStatusImage(recordingData.getRecordingWritingStatus()));
			recordingStatusIcon.setToolTipText(TextFormatter.getWritingStatusText(recordingData.getRecordingWritingStatus()));

			dataLoaded = true;
		} else {
			recordingIcon.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_RECORD_GRAY));
		}

		if (!dataLoaded) {
			recordingStorage.setText("");
			recTimeBar.setVisible(false);
			recTime.setVisible(false);
			recordingStatusIcon.setImage(null);
			recordingStatusIcon.setToolTipText("");
			recordingLabel.setText("Not Active");
		}

		if (countdownJobActive) {
			recordCountdownJob.schedule();
		} else {
			recordCountdownJob.cancel();
		}
	}

	/**
	 * 
	 * @return Returns if the form is disposed.
	 */
	public boolean isDisposed() {
		return form.isDisposed();
	}

	/**
	 * Disposes the for.
	 */
	public void dispose() {
		form.dispose();
		recordCountdownJob.cancel();
	}

	/**
	 * Job for updating the information about the CMR. Job will perform all UI related work in UI
	 * thread asynchronously.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class UpdateCmrPropertiesJob extends Job {

		/**
		 * Default constructor.
		 */
		public UpdateCmrPropertiesJob() {
			super("Updating CMR Properties..");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final CmrRepositoryDefinition cmrRepositoryDefinition = CmrRepositoryPropertyForm.this.cmrRepositoryDefinition;
			if (cmrRepositoryDefinition != null) {
				final OnlineStatus onlineStatus = cmrRepositoryDefinition.getOnlineStatus();
				final CmrStatusData cmrStatusData = (onlineStatus == OnlineStatus.ONLINE) ? cmrRepositoryDefinition.getCmrManagementService().getCmrStatusData() : null; // NOPMD
				recordingData = (onlineStatus == OnlineStatus.ONLINE) ? cmrRepositoryDefinition.getStorageService().getRecordingData() : null; // NOPMD
				SafeExecutor.asyncExec(new Runnable() {

					@Override
					public void run() {
						form.setBusy(true);
						form.setText(cmrRepositoryDefinition.getName());
						form.setMessage(null, IMessageProvider.NONE);
						address.setText(cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort());
						version.setText(cmrRepositoryDefinition.getVersion());
						String desc = cmrRepositoryDefinition.getDescription();
						if (null != desc) {
							if (desc.length() > MAX_DESCRIPTION_LENGTH) {
								description.setText("<form><p>" + desc.substring(0, MAX_DESCRIPTION_LENGTH) + ".. <a href=\"More\">[More]</a></p></form>", true, false);
							} else {
								description.setText(desc, false, false);
							}
						} else {
							description.setText("", false, false);
						}
						status.setText(onlineStatus.toString());
						if (onlineStatus == OnlineStatus.ONLINE) {
							form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE_SMALL));
						} else if (onlineStatus == OnlineStatus.CHECKING) {
							form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_REFRESH_SMALL));
						} else {
							form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_OFFLINE_SMALL));
						}

						updateRecordingData(recordingData);
						updateCmrManagementData(cmrStatusData);

						mainComposite.setVisible(true);
						form.getBody().layout(true, true);
						form.setBusy(false);
					}
				}, form, mainComposite);
			} else {

				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						form.setBusy(true);

						form.setText(null);
						form.setMessage("Please select a CMR to see its properties.", IMessageProvider.INFORMATION);
						mainComposite.setVisible(false);

						updateRecordingData(null);
						updateCmrManagementData(null);

						mainComposite.setVisible(true);
						form.getBody().layout(true, true);
						form.setBusy(false);
					}
				});
			}
			return Status.OK_STATUS;
		}
	}

	/**
	 * Class for updating the recording count down.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class RecordCountdownJob extends UIJob {

		/**
		 * Default constructor.
		 */
		public RecordCountdownJob() {
			super("Update Recording Countdown");
			setUser(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (null != recordingData && !form.isDisposed()) {
				Date endDate = recordingData.getRecordEndDate();
				Date startDate = recordingData.getRecordStartDate();
				if (null != endDate && null != startDate && startDate.before(new Date())) {
					Date now = new Date();
					long millisMore = endDate.getTime() - now.getTime();
					if (millisMore > 0) {
						if (!recTimeBar.isVisible()) {
							recTimeBar.setVisible(true);
						}
						recTimeBar.setMaximum((int) (endDate.getTime() - startDate.getTime()));
						recTimeBar.setSelection((int) (recTimeBar.getMaximum() - (now.getTime() - startDate.getTime())));

						if (!recTime.isVisible()) {
							recTime.setVisible(true);
						}
						String string;
						if (millisMore > 0) {
							string = NumberFormatter.humanReadableMillisCount(millisMore, false);
						} else {
							string = "";
						}
						recTime.setText(string);

					} else {
						if (recTimeBar.isVisible()) {
							recTimeBar.setVisible(false);
						}
						if (recTime.isVisible()) {
							recTime.setVisible(false);
						}
						this.cancel();
						refreshData();
					}
				}
			}
			this.schedule(1000);
			return Status.OK_STATUS;
		}
	}

}
