package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.composite.StorageInfoComposite;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * The page that displays the data about storage to be loaded after the file for import has been
 * selected.
 * 
 * @author Ivan Senic
 * 
 */
public class ImportStorageInfoPage extends WizardPage {

	/**
	 * Default wizard page message.
	 */
	private static final String DEFAULT_MESSAGE = "Preview storage data before import";

	/**
	 * Should import be local.
	 */
	private boolean importLocally;

	/**
	 * File name of the storage zip file.
	 */
	private String fileName;

	/**
	 * {@link CmrRepositoryDefinition} if import is remote.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Boolean that denotes if import is possible.
	 */
	private boolean canImport = false;

	/**
	 * Label to display file name.
	 */
	private Label file;

	/**
	 * Label to display where to import.
	 */
	private Label importTo;

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * {@link StorageInfoComposite}.
	 */
	private StorageInfoComposite storageInfoComposite;

	/**
	 * Default constructor.
	 */
	public ImportStorageInfoPage() {
		super("Import Storage");
		this.setTitle("Import Storage");
		this.setMessage(DEFAULT_MESSAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		new Label(main, SWT.NONE).setText("Selected file:");
		file = new Label(main, SWT.WRAP);

		new Label(main, SWT.NONE).setText("Import to:");
		importTo = new Label(main, SWT.WRAP);

		storageInfoComposite = new StorageInfoComposite(main, SWT.NONE, false);
		storageInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		setControl(main);
	}

	/**
	 * Updates the page content by loading the storage information from file.
	 */
	public void update() {
		// first reset all
		reset();

		// then load info in job
		Job updateImportStoragePage = new Job("Update Import Storage Info Page") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean callServices = !importLocally && cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE;
				final List<StorageData> existingStorages = callServices ? cmrRepositoryDefinition.getStorageService().getExistingStorages() : Collections.<StorageData> emptyList();
				final long spaceLeftOnCmr = callServices ? cmrRepositoryDefinition.getCmrManagementService().getCmrStatusData().getStorageDataSpaceLeft() : 0;

				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (null == fileName) {
							canImport = false;
						} else {
							file.setText(fileName);
							if (importLocally) {
								importTo.setText("Import locally");
							} else {
								importTo.setText(cmrRepositoryDefinition.getName());
							}
							InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
							IStorageData storageData = storageManager.getStorageDataFromZip(fileName);
							if (null != storageData) {
								storageInfoComposite.displayStorageData(storageData);
								if (importLocally) {
									boolean notImportedYet = true;
									for (LocalStorageData localStorageData : storageManager.getDownloadedStorages()) {
										if (ObjectUtils.equals(localStorageData.getId(), storageData.getId())) {
											notImportedYet = false;
											break;
										}
									}
									if (notImportedYet) {
										canImport = true;
										setMessage(DEFAULT_MESSAGE);
									} else {
										canImport = false;
										setMessage("Selected storage to import is already available locally", ERROR);
									}
								} else if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
									boolean notImportedYet = true;
									for (StorageData storageDataOnRepository : existingStorages) {
										if (ObjectUtils.equals(storageDataOnRepository.getId(), storageData.getId())) {
											notImportedYet = false;
											break;
										}
									}

									boolean enoughSpace = spaceLeftOnCmr > storageData.getDiskSize();

									if (notImportedYet && enoughSpace) {
										canImport = true;

										// check the CMR version
										String cmrVersion = cmrRepositoryDefinition.getVersion();
										if (null == storageData.getCmrVersion()) {
											setMessage("Selected storage does not define CMR version. The storage might be unstable on the CMR version " + cmrVersion + ".", WARNING);
										} else if (!ObjectUtils.equals(storageData.getCmrVersion(), cmrVersion)) {
											setMessage("Selected storage has different CMR version than the current CMR version " + cmrVersion + ". The storage might be unstable.", WARNING);
										} else {
											setMessage(DEFAULT_MESSAGE);
										}
									} else if (!notImportedYet) {
										canImport = false;
										setMessage("Selected storage to import is already available on selected CMR", ERROR);
									} else if (!enoughSpace) {
										canImport = false;
										setMessage("Insufficient storage space of the selected repository (" + NumberFormatter.humanReadableByteCount(spaceLeftOnCmr) + " left)", ERROR);
									}
								} else {
									canImport = false;
									setMessage("Can not import storage to selected CMR because the CMR is offline", ERROR);
								}
							} else {
								storageInfoComposite.showDataUnavailable();
								canImport = false;
								setMessage("Provided file is not valid inspectIT compressed storage file", ERROR);
							}

							main.layout();
							main.update();
						}
						setPageComplete(isPageComplete());
					}
				}, file, importTo, storageInfoComposite, main);

				return Status.OK_STATUS;
			}
		};
		updateImportStoragePage.schedule();
	}

	/**
	 * Resets the page.
	 */
	public void reset() {
		canImport = false;
		setPageComplete(false);
	}

	@Override
	public boolean isPageComplete() {
		return canImport;
	}

	/**
	 * Sets {@link #importLocally}.
	 * 
	 * @param importLocally
	 *            New value for {@link #importLocally}
	 */
	public void setImportLocally(boolean importLocally) {
		this.importLocally = importLocally;
	}

	/**
	 * Sets {@link #fileName}.
	 * 
	 * @param fileName
	 *            New value for {@link #fileName}
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Sets {@link #cmrRepositoryDefinition}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            New value for {@link #cmrRepositoryDefinition}
	 */
	public void setCmrRepositoryDefinition(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

}
