package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Selection of existing storage.
 * 
 * @author Ivan Senic
 * 
 */
public class SelectExistingStorageWizardPage extends WizardPage {

	/**
	 * Default page message.
	 */
	private static final String DEFAULT_MESSAGE = "Select storage and repository where data will be stored";

	/**
	 * List of available repositories.
	 */
	private List<CmrRepositoryDefinition> cmrRepositories;

	/**
	 * {@link CmrRepositoryDefinition} that should be initially selected.
	 */
	private CmrRepositoryDefinition proposedCmrRepositoryDefinition;

	/**
	 * List of storages to be selected.
	 */
	private List<StorageData> storageList = new ArrayList<StorageData>();

	/**
	 * Repository combo.
	 */
	private Combo cmrRepositoryCombo;

	/**
	 * Storage selection.
	 */
	private org.eclipse.swt.widgets.List storageSelection;

	/**
	 * If the recording check should be performed on the selected CMR.
	 */
	private boolean checkRecording;

	/**
	 * Button for choosing if storage should be auto finalized.
	 */
	private Button autoFinalize;

	/**
	 * Default constructor.
	 */
	public SelectExistingStorageWizardPage() {
		super("Select Storage");
		this.setTitle("Select Storage");
		this.setMessage(DEFAULT_MESSAGE);
		cmrRepositories = new ArrayList<CmrRepositoryDefinition>();
		cmrRepositories.addAll(InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions());
	}

	/**
	 * Additional constructor to specify the {@link #checkRecording} value.
	 * 
	 * @param checkRecording
	 *            If the recording check should be performed on the selected CMR.
	 */
	public SelectExistingStorageWizardPage(boolean checkRecording) {
		this();
		this.checkRecording = checkRecording;
	}

	/**
	 * With this constructor the passed {@link CmrRepositoryDefinition} will be initially selected
	 * on the page.
	 * 
	 * @param proposedCmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} that should be initially selected.
	 * @param checkRecording
	 *            If the recording check should be performed on the selected CMR.
	 */
	public SelectExistingStorageWizardPage(CmrRepositoryDefinition proposedCmrRepositoryDefinition, boolean checkRecording) {
		this();
		this.proposedCmrRepositoryDefinition = proposedCmrRepositoryDefinition;
		this.checkRecording = checkRecording;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		Label cmrSelectLabel = new Label(main, SWT.LEFT);
		cmrSelectLabel.setText("Repository:");
		cmrRepositoryCombo = new Combo(main, SWT.READ_ONLY);
		cmrRepositoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label storageLabel = new Label(main, SWT.TOP);
		storageLabel.setText("Storage:");
		storageLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		storageSelection = new org.eclipse.swt.widgets.List(main, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		storageSelection.setEnabled(false);
		storageSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		new Label(main, SWT.LEFT);
		autoFinalize = new Button(main, SWT.CHECK);
		autoFinalize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		autoFinalize.setText("Auto-finalize storage");
		autoFinalize.setToolTipText("If selected the storage will be automatically finalized after the action completes");
		autoFinalize.setSelection(true);

		final Listener pageCompletedListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				if (getSelectedRepository() == null) {
					setMessage("Repository must be selected.", IMessageProvider.ERROR);
				} else if (getSelectedRepository().getOnlineStatus() == OnlineStatus.OFFLINE) {
					setMessage("Selected repository is currently offline.", IMessageProvider.ERROR);
				} else if (checkRecording && getSelectedRepository().getStorageService().getRecordingState() != RecordingState.OFF) {
					setMessage("Recording is already active on selected repository.", IMessageProvider.ERROR);
				} else if (getSelectedStorageData() == null) {
					setMessage("Storage must be selected.", IMessageProvider.ERROR);
				} else {
					setMessage(DEFAULT_MESSAGE);
				}
			}
		};
		cmrRepositoryCombo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateStorageList();
				pageCompletedListener.handleEvent(event);
			}
		});

		storageSelection.addListener(SWT.Selection, pageCompletedListener);

		int i = 0;
		int index = -1;
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositories) {
			cmrRepositoryCombo.add(cmrRepositoryDefinition.getName() + " (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getName() + ")");
			if (cmrRepositoryDefinition.equals(proposedCmrRepositoryDefinition)) {
				index = i;
			}
			i++;
		}
		if (index != -1) {
			cmrRepositoryCombo.select(index);
			cmrRepositoryCombo.setEnabled(false);
			updateStorageList();
		}

		setControl(main);
	}

	/**
	 * @return Returns selected repository or null.
	 */
	public CmrRepositoryDefinition getSelectedRepository() {
		if (cmrRepositoryCombo.getSelectionIndex() != -1) {
			return cmrRepositories.get(cmrRepositoryCombo.getSelectionIndex());
		}
		return null;
	}

	/**
	 * @return Returns selected {@link StorageData}.
	 */
	public StorageData getSelectedStorageData() {
		if (storageSelection.getSelectionIndex() != -1) {
			return storageList.get(storageSelection.getSelectionIndex());
		}
		return null;
	}

	/**
	 * Returns if auto-finalize options is selected.
	 * 
	 * @return Returns if auto-finalize options is selected.
	 */
	public boolean isAutoFinalize() {
		return autoFinalize.getSelection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (getSelectedRepository() == null) {
			return false;
		} else if (getSelectedRepository().getOnlineStatus() == OnlineStatus.OFFLINE) {
			return false;
		} else if (checkRecording && getSelectedRepository().getStorageService().getRecordingState() != RecordingState.OFF) {
			return false;
		}
		if (getSelectedStorageData() == null) {
			return false;
		}
		return true;
	}

	/**
	 * Updates the storage list based on selected repository.
	 */
	private void updateStorageList() {
		final CmrRepositoryDefinition cmrRepositoryDefinition = getSelectedRepository();
		Job updateStoragesJob = new Job("Updating Storages") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (null != cmrRepositoryDefinition && cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					storageList = cmrRepositoryDefinition.getStorageService().getOpenedStorages();
				} else {
					storageList = Collections.emptyList();
				}
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						storageSelection.removeAll();
						if (null != cmrRepositoryDefinition && cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
							if (storageList.isEmpty()) {
								storageSelection.add("No open storage available for writing");
								storageSelection.setEnabled(false);
							} else {
								for (StorageData storageData : storageList) {
									storageSelection.add(storageData.getName());
								}
								storageSelection.setEnabled(true);
							}
						} else {
							storageSelection.setEnabled(false);
						}
					}
				}, storageSelection);
				return Status.OK_STATUS;
			}
		};
		updateStoragesJob.schedule();
	}
}
