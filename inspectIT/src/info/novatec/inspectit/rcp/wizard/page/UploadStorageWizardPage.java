package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.composite.StorageInfoComposite;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Page for selecting CMR to upload storage to.
 * 
 * @author Ivan Senic
 * 
 */
public class UploadStorageWizardPage extends WizardPage {

	/**
	 * Default page message.
	 */
	private static final String DEFAULT_MESSAGE = "Select CMR (Central Management Repository) to upload storage to";

	/**
	 * Local data to upload.
	 */
	private LocalStorageData localStorageData;

	/**
	 * List of CMR repositories.
	 */
	private List<CmrRepositoryDefinition> cmrRepositories;

	/**
	 * Combo that displays the CMRs.
	 */
	private Combo cmrRepositoryCombo;

	/**
	 * If storage is already available on the CMR.
	 */
	private boolean alreadyAvailable;

	/**
	 * Is there enough space on the CMR.
	 */
	private boolean enoughSpace;

	/**
	 * Storages on selected repository.
	 */
	private List<StorageData> storagesOnRepository;

	/**
	 * Space left on the selected repository.
	 */
	private long spaceLeftOnCmr;

	/**
	 * Default constructor.
	 * 
	 * @param localStorageData
	 *            Local data to upload. Must not be null.
	 */
	public UploadStorageWizardPage(LocalStorageData localStorageData) {
		super("Upload Storage");
		Assert.isNotNull(localStorageData);
		this.localStorageData = localStorageData;
		this.cmrRepositories = new ArrayList<CmrRepositoryDefinition>();
		this.cmrRepositories.addAll(InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions());
		this.setTitle("Upload Storage");
		this.setMessage(DEFAULT_MESSAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		Label cmrSelectLabel = new Label(main, SWT.LEFT);
		cmrSelectLabel.setText("Upload CMR:");
		cmrRepositoryCombo = new Combo(main, SWT.READ_ONLY);
		cmrRepositoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositories) {
			cmrRepositoryCombo.add(cmrRepositoryDefinition.getName() + " (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")");
		}

		StorageInfoComposite storageInfoComposite = new StorageInfoComposite(main, SWT.NONE, false, localStorageData);
		storageInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		final Listener pageCompleteListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				boolean isPageComplete = isPageComplete();
				setPageComplete(isPageComplete);
				if (event.widget.equals(cmrRepositoryCombo) && cmrRepositoryCombo.getSelectionIndex() == -1) {
					setMessage("Repository must be selected", IMessageProvider.ERROR);
					return;
				} else if (event.widget.equals(cmrRepositoryCombo) && getCmrRepositoryDefinition().getOnlineStatus() == OnlineStatus.OFFLINE) {
					setMessage("The selected repository is currently unavailable", IMessageProvider.ERROR);
				} else if (alreadyAvailable) {
					setMessage("The selected storage to upload is already available on the selected repository", IMessageProvider.ERROR);
				} else if (!enoughSpace) {
					setMessage("Insufficient storage space left on the selected repository", ERROR);
				} else {
					String cmrVersion = getCmrRepositoryDefinition().getVersion();
					if (null == localStorageData.getCmrVersion()) {
						setMessage("Selected storage does not define CMR version. The storage might be unstable on the CMR version " + cmrVersion + ".", WARNING);
					} else if (!ObjectUtils.equals(localStorageData.getCmrVersion(), cmrVersion)) {
						setMessage("Selected storage has different CMR version than the current CMR version " + cmrVersion + ". The storage might be unstable.", WARNING);
					} else {
						setMessage(DEFAULT_MESSAGE);
					}
				}
			}
		};

		Listener cmrComboListener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				final CmrRepositoryDefinition cmrRepositoryDefinition = getCmrRepositoryDefinition();
				Job updateCmrData = new Job("Loading CMR Storage Data") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						OnlineStatus onlineStatus = cmrRepositoryDefinition.getOnlineStatus();
						if (onlineStatus != OnlineStatus.OFFLINE) {
							storagesOnRepository = cmrRepositoryDefinition.getStorageService().getExistingStorages();
							spaceLeftOnCmr = cmrRepositoryDefinition.getCmrManagementService().getCmrStatusData().getStorageDataSpaceLeft();
						} else {
							storagesOnRepository = Collections.emptyList();
							spaceLeftOnCmr = 0;
						}

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								pageCompleteListener.handleEvent(event);
							}
						});
						return Status.OK_STATUS;
					}
				};
				updateCmrData.schedule();
			}
		};

		cmrRepositoryCombo.addListener(SWT.Selection, cmrComboListener);

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (cmrRepositoryCombo.getSelectionIndex() == -1) {
			return false;
		} else if (getCmrRepositoryDefinition().getOnlineStatus() == OnlineStatus.OFFLINE) {
			return false;
		}
		for (StorageData storageData : storagesOnRepository) {
			if (Objects.equals(storageData.getId(), localStorageData.getId())) {
				alreadyAvailable = true;
				return false;
			}
		}
		alreadyAvailable = false;

		enoughSpace = spaceLeftOnCmr > localStorageData.getDiskSize();
		if (!enoughSpace) {
			return false;
		}

		return true;
	}

	/**
	 * @return Return selected CMR repository.
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		if (cmrRepositoryCombo.getSelectionIndex() != -1) {
			return cmrRepositories.get(cmrRepositoryCombo.getSelectionIndex());
		}
		return null;
	}

}
