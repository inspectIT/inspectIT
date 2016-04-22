package rocks.inspectit.ui.rcp.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.TimeFrameDataProcessor;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.view.impl.StorageManagerView;
import rocks.inspectit.ui.rcp.wizard.page.AddStorageLabelWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineDataProcessorsWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineNewStorageWizzardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineTimelineWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.NewOrExistsingStorageWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.SelectAgentsWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.SelectExistingStorageWizardPage;

/**
 * Wizard for copying the buffer content of the {@link CmrRepositoryDefinition} to Storage.
 * 
 * @author Ivan Senic
 * 
 */
public class CopyBufferToStorageWizard extends Wizard implements INewWizard {

	/**
	 * {@link CmrRepositoryDefinition} to perform operation on.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * The collection of agents that will be automatically selected in the
	 * {@link SelectExistingStorageWizardPage}.
	 */
	private Collection<PlatformIdent> autoSelectedAgents;

	/**
	 * Should new storage be used, or an existing one.
	 */
	private NewOrExistsingStorageWizardPage newOrExistsingStorageWizardPage;

	/**
	 * New storage wizard page.
	 */
	private DefineNewStorageWizzardPage defineNewStorageWizzardPage;

	/**
	 * Select existing storage wizard page.
	 */
	private SelectExistingStorageWizardPage selectExistingStorageWizardPage;

	/**
	 * Page to selection options.
	 */
	private SelectAgentsWizardPage selectAgentsPage;

	/**
	 * Page for defining the processors.
	 */
	private DefineDataProcessorsWizardPage defineProcessorsPage;

	/**
	 * Page for selecting the time frame.
	 */
	private DefineTimelineWizardPage timelineWizardPage;

	/**
	 * Add new label wizard page.
	 */
	private AddStorageLabelWizardPage addLabelWizardPage;

	/**
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to perform operation on.
	 */
	public CopyBufferToStorageWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this(cmrRepositoryDefinition, Collections.<PlatformIdent> emptyList());
	}

	/**
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to perform operation on.
	 * @param autoSelectedAgents
	 *            The collection of agents that will be automatically selected in the
	 *            {@link SelectExistingStorageWizardPage}.
	 */
	public CopyBufferToStorageWizard(CmrRepositoryDefinition cmrRepositoryDefinition, Collection<PlatformIdent> autoSelectedAgents) {
		super();
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.autoSelectedAgents = autoSelectedAgents;
		this.setWindowTitle("Copy Buffer to Storage Wizard");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_STORAGE));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		newOrExistsingStorageWizardPage = new NewOrExistsingStorageWizardPage();
		addPage(newOrExistsingStorageWizardPage);
		defineNewStorageWizzardPage = new DefineNewStorageWizzardPage(cmrRepositoryDefinition);
		addPage(defineNewStorageWizzardPage);
		selectExistingStorageWizardPage = new SelectExistingStorageWizardPage(cmrRepositoryDefinition, false);
		addPage(selectExistingStorageWizardPage);
		selectAgentsPage = new SelectAgentsWizardPage("Select Agent(s) to be copied", autoSelectedAgents);
		addPage(selectAgentsPage);
		defineProcessorsPage = new DefineDataProcessorsWizardPage(DefineDataProcessorsWizardPage.BUFFER_DATA | DefineDataProcessorsWizardPage.SYSTEM_DATA);
		addPage(defineProcessorsPage);
		timelineWizardPage = new DefineTimelineWizardPage("Limit Data", "Optionally select set of data to be copied by defining time frame",
				DefineTimelineWizardPage.PAST | DefineTimelineWizardPage.BOTH_DATES);
		addPage(timelineWizardPage);
		addLabelWizardPage = new AddStorageLabelWizardPage(cmrRepositoryDefinition);
		addPage(addLabelWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final StorageData storageData;
		final CmrRepositoryDefinition cmrRepositoryDefinition;
		final boolean autoFinalize;

		if (newOrExistsingStorageWizardPage.useNewStorage()) {
			storageData = defineNewStorageWizzardPage.getStorageData();
			cmrRepositoryDefinition = defineNewStorageWizzardPage.getSelectedRepository();
			autoFinalize = defineNewStorageWizzardPage.isAutoFinalize();
		} else {
			storageData = selectExistingStorageWizardPage.getSelectedStorageData();
			cmrRepositoryDefinition = selectExistingStorageWizardPage.getSelectedRepository();
			autoFinalize = selectExistingStorageWizardPage.isAutoFinalize();
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			final List<Long> agents = selectAgentsPage.getSelectedAgents();
			List<AbstractDataProcessor> processors = defineProcessorsPage.getProcessorList();
			if (timelineWizardPage.isTimerframeUsed()) {
				TimeFrameDataProcessor timeFrameDataProcessor = timelineWizardPage.getTimeFrameDataProcessor(processors);
				processors = new ArrayList<AbstractDataProcessor>(1);
				processors.add(timeFrameDataProcessor);
			}

			final List<AbstractDataProcessor> finalProcessors = processors;
			Job copyBufferJob = new Job("Copy Buffer to Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						monitor.beginTask("Copying the content of repository buffer to storage.", IProgressMonitor.UNKNOWN);
						StorageData copiedStorage = cmrRepositoryDefinition.getStorageService().copyBufferToStorage(storageData, agents, finalProcessors, autoFinalize);
						List<AbstractStorageLabel<?>> labels = addLabelWizardPage.getLabelsToAdd();
						if (!labels.isEmpty()) {
							cmrRepositoryDefinition.getStorageService().addLabelsToStorage(copiedStorage, labels, true);
						}
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
								if (storageManagerView instanceof StorageManagerView) {
									((StorageManagerView) storageManagerView).refresh(cmrRepositoryDefinition);
								}
							}
						});
					} catch (BusinessException e) {
						return new Status(IStatus.ERROR, InspectIT.ID, "Copy of the buffer data to storage failed.", e);
					}
					return Status.OK_STATUS;
				}
			};
			copyBufferJob.setUser(true);
			copyBufferJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_BUFFER_COPY));
			copyBufferJob.schedule();
		} else {
			InspectIT.getDefault().createErrorDialog("Copy of the buffer data to storage failed. Selected CMR repository is currently not available.", -1);
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (ObjectUtils.equals(page, newOrExistsingStorageWizardPage)) {
			if (newOrExistsingStorageWizardPage.useNewStorage()) {
				return defineNewStorageWizzardPage;
			} else {
				return selectExistingStorageWizardPage;
			}
		} else if (ObjectUtils.equals(page, defineNewStorageWizzardPage)) {
			selectAgentsPage.setCmrRepositoryDefinition(cmrRepositoryDefinition);
			addLabelWizardPage.setStorageData(defineNewStorageWizzardPage.getStorageData());
			return selectAgentsPage;
		} else if (ObjectUtils.equals(page, selectExistingStorageWizardPage)) {
			selectAgentsPage.setCmrRepositoryDefinition(cmrRepositoryDefinition);
			addLabelWizardPage.setStorageData(selectExistingStorageWizardPage.getSelectedStorageData());
			return selectAgentsPage;
		} else {
			return super.getNextPage(page);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (ObjectUtils.equals(page, defineNewStorageWizzardPage) || ObjectUtils.equals(page, selectExistingStorageWizardPage)) {
			return newOrExistsingStorageWizardPage;
		} else if (ObjectUtils.equals(page, selectAgentsPage)) {
			if (newOrExistsingStorageWizardPage.useNewStorage()) {
				return defineNewStorageWizzardPage;
			} else {
				return selectExistingStorageWizardPage;
			}
		} else {
			return super.getPreviousPage(page);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canFinish() {
		if (!newOrExistsingStorageWizardPage.isPageComplete()) {
			return false;
		} else if (newOrExistsingStorageWizardPage.useNewStorage() && !defineNewStorageWizzardPage.isPageComplete()) {
			return false;
		} else if (!newOrExistsingStorageWizardPage.useNewStorage() && !selectExistingStorageWizardPage.isPageComplete()) {
			return false;
		} else if (!selectAgentsPage.isPageComplete()) {
			return false;
		} else if (!defineProcessorsPage.isPageComplete()) {
			return false;
		} else if (!timelineWizardPage.isPageComplete()) {
			return false;
		}
		return true;
	}

}
