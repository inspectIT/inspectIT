package rocks.inspectit.ui.rcp.wizard;

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

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.IIdsAwareAggregatedData;
import rocks.inspectit.shared.all.communication.data.AggregatedExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.AggregatedHttpTimerData;
import rocks.inspectit.shared.all.communication.data.AggregatedSqlStatementData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ISpanIdentAware;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.view.impl.StorageManagerView;
import rocks.inspectit.ui.rcp.wizard.page.AddStorageLabelWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineDataProcessorsWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineNewStorageWizzardPage;
import rocks.inspectit.ui.rcp.wizard.page.NewOrExistsingStorageWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.SelectExistingStorageWizardPage;

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
				style |= DefineDataProcessorsWizardPage.HTTP_TIMERS;
			} else if (SqlStatementData.class.equals(clazz) || AggregatedSqlStatementData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.SQL_STATEMENTS;
			} else if (ExceptionSensorData.class.equals(clazz) || AggregatedExceptionSensorData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.EXCEPTIONS;
			} else if (TimerData.class.equals(clazz) || AggregatedTimerData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.TIMERS;
			} else if (InvocationSequenceData.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.INVOCATIONS | DefineDataProcessorsWizardPage.EXTRACT_INVOCATIONS;
			} else if (ClientSpan.class.equals(clazz) || ServerSpan.class.equals(clazz)) {
				style |= DefineDataProcessorsWizardPage.SPANS;
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
			final boolean includeTraces = defineDataProcessorsWizardPage.isTraceDataSelected();
			final Set<Long> idSet = new HashSet<>();
			final Set<Long> traceIdSet = new HashSet<>();
			Set<Long> platformIdents = new HashSet<>();

			for (DefaultData template : copyDataList) {
				if (template instanceof IIdsAwareAggregatedData<?>) {
					// if we have aggregated data add all objects that were included in the
					// aggregation
					idSet.addAll(((IIdsAwareAggregatedData<?>) template).getAggregatedIds());
				} else if (0 != template.getId()) {
					idSet.add(template.getId());
				}
				if (includeTraces && (template instanceof ISpanIdentAware)) {
					ISpanIdentAware spanIdentaware = (ISpanIdentAware) template;
					SpanIdent spanIdent = spanIdentaware.getSpanIdent();
					if (null != spanIdent) {
						traceIdSet.add(spanIdent.getTraceId());
					}
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
						StorageData updatedStorageData = cmrRepositoryDefinition.getStorageService().copyDataToStorage(storageData, idSet, platformIdent, traceIdSet, processors, autoFinalize);
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
						return new Status(IStatus.ERROR, InspectIT.ID, "Copy data to buffer failed.", e);
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
