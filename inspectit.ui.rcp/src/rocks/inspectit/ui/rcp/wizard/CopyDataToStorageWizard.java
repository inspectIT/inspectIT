package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IIdsAwareAggregatedData;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.AggregatedSqlStatementData;
import info.novatec.inspectit.communication.data.AggregatedTimerData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.page.AddStorageLabelWizardPage;
import info.novatec.inspectit.rcp.wizard.page.DefineDataProcessorsWizardPage;
import info.novatec.inspectit.rcp.wizard.page.DefineNewStorageWizzardPage;
import info.novatec.inspectit.rcp.wizard.page.NewOrExistsingStorageWizardPage;
import info.novatec.inspectit.rcp.wizard.page.SelectExistingStorageWizardPage;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

/**
 * Wizard for copying the selected data to one storage.
 * 
 * @author Ivan Senic
 * 
 */
public class CopyDataToStorageWizard extends Wizard implements INewWizard {

	/**
	 * Collection of data to be copied.
	 */
	private Collection<DefaultData> copyDataList;

	/**
	 * CMR for the action.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Page for selecting if new or existing storage page should be used.
	 */
	private NewOrExistsingStorageWizardPage newOrExistsingStorageWizardPage;

	/**
	 * Page for new storage.
	 */
	private DefineNewStorageWizzardPage defineNewStorageWizzardPage;

	/**
	 * Page for selecting the existing storage.
	 */
	private SelectExistingStorageWizardPage selectExistingStorageWizardPage;

	/**
	 * Selection of data to be saved.
	 */
	private DefineDataProcessorsWizardPage defineDataProcessorsWizardPage;

	/**
	 * Add label wizard page.
	 */
	private AddStorageLabelWizardPage addLabelWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to execute action on.
	 * @param copyDataList
	 *            Collection of data to be copied.
	 */
	public CopyDataToStorageWizard(CmrRepositoryDefinition cmrRepositoryDefinition, Collection<DefaultData> copyDataList) {
		this.copyDataList = copyDataList;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setWindowTitle("Save Data to Storage Wizard");
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
		int style = 0;
		for (DefaultData copyData : copyDataList) {
			Class<?> clazz = copyData.getClass();
			if (HttpTimerData.class.equals(clazz) || AggregatedHttpTimerData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.ONLY_HTTP_TIMERS;
			} else if (SqlStatementData.class.equals(clazz) || AggregatedSqlStatementData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.ONLY_SQL_STATEMENTS;
			} else if (ExceptionSensorData.class.equals(clazz) || AggregatedExceptionSensorData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.ONLY_EXCEPTIONS;
			} else if (TimerData.class.equals(clazz) || AggregatedTimerData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.ONLY_TIMERS;
			} else if (InvocationSequenceData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.ONLY_INVOCATIONS | DefineDataProcessorsWizardPage.EXTRACT_INVOCATIONS;
			}
		}
		newOrExistsingStorageWizardPage = new NewOrExistsingStorageWizardPage();
		addPage(newOrExistsingStorageWizardPage);
		defineNewStorageWizzardPage = new DefineNewStorageWizzardPage(cmrRepositoryDefinition);
		addPage(defineNewStorageWizzardPage);
		selectExistingStorageWizardPage = new SelectExistingStorageWizardPage(cmrRepositoryDefinition, false);
		addPage(selectExistingStorageWizardPage);
		defineDataProcessorsWizardPage = new DefineDataProcessorsWizardPage(style);
		addPage(defineDataProcessorsWizardPage);
		addLabelWizardPage = new AddStorageLabelWizardPage(cmrRepositoryDefinition);
		addPage(addLabelWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final StorageData storageData;
		final boolean autoFinalize;
		if (newOrExistsingStorageWizardPage.useNewStorage()) {
			storageData = defineNewStorageWizzardPage.getStorageData();
			autoFinalize = defineNewStorageWizzardPage.isAutoFinalize();
		} else {
			storageData = selectExistingStorageWizardPage.getSelectedStorageData();
			autoFinalize = selectExistingStorageWizardPage.isAutoFinalize();
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			// prepare for save
			final Collection<AbstractDataProcessor> processors = defineDataProcessorsWizardPage.getProcessorList();
			final Set<Long> idSet = new HashSet<Long>();
			Set<Long> platformIdents = new HashSet<Long>();
			for (DefaultData template : copyDataList) {
				if (template instanceof IIdsAwareAggregatedData<?>) {
					// if we have aggregated data add all objects that were included in the
					// aggregation
					idSet.addAll(((IIdsAwareAggregatedData<?>) template).getAggregatedIds());
				} else if (0 != template.getId()) {
					idSet.add(template.getId());
				}
				if (template instanceof InvocationAwareData) {
					// if we have invocation aware object, add also all invocations
					// data processor will filter the correct data to save
					idSet.addAll(((InvocationAwareData) template).getInvocationParentsIdSet());
				}
				platformIdents.add(template.getPlatformIdent());
			}
			final long platformIdent = (platformIdents.size() == 1) ? platformIdents.iterator().next() : 0;

			// create and execute job
			Job copyDataJob = new Job("Copy Data to Buffer") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						StorageData updatedStorageData = cmrRepositoryDefinition.getStorageService().copyDataToStorage(storageData, idSet, platformIdent, processors, autoFinalize);
						List<AbstractStorageLabel<?>> labels = addLabelWizardPage.getLabelsToAdd();
						if (!labels.isEmpty()) {
							cmrRepositoryDefinition.getStorageService().addLabelsToStorage(updatedStorageData, labels, true);
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
						return new Status(Status.ERROR, InspectIT.ID, "Copy data to buffer failed.", e);
					}
					return Status.OK_STATUS;
				}
			};
			copyDataJob.setUser(true);
			copyDataJob.schedule();
			return true;
		}

		return false;
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
			addLabelWizardPage.setStorageData(defineNewStorageWizzardPage.getStorageData());
			return defineDataProcessorsWizardPage;
		} else if (ObjectUtils.equals(page, selectExistingStorageWizardPage)) {
			addLabelWizardPage.setStorageData(selectExistingStorageWizardPage.getSelectedStorageData());
			return defineDataProcessorsWizardPage;
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
		} else if (ObjectUtils.equals(page, defineDataProcessorsWizardPage)) {
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
		} else if (!defineDataProcessorsWizardPage.isPageComplete()) {
			return false;
		}
		return true;
	}

}
