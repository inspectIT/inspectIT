package rocks.inspectit.ui.rcp.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.AgentFilterDataProcessor;
import rocks.inspectit.shared.cs.storage.recording.RecordingProperties;
import rocks.inspectit.shared.cs.storage.recording.RecordingState;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.provider.IStorageDataProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.wizard.page.AddStorageLabelWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineDataProcessorsWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineNewStorageWizzardPage;
import rocks.inspectit.ui.rcp.wizard.page.DefineTimelineWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.NewOrExistsingStorageWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.SelectAgentsWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.SelectExistingStorageWizardPage;

/**
 * Wizard for starting a recording.
 *
 * @author Ivan Senic
 *
 */
public class StartRecordingWizard extends Wizard implements INewWizard {

	/**
	 * {@link NewOrExistsingStorageWizardPage}.
	 */
	private NewOrExistsingStorageWizardPage newOrExistsingStorageWizardPage;

	/**
	 * Define data page.
	 */
	private DefineDataProcessorsWizardPage defineDataPage;

	/**
	 * Recording storage selection page.
	 */
	private SelectExistingStorageWizardPage selectStorageWizardPage;

	/**
	 * New storage page.
	 */
	private DefineNewStorageWizzardPage defineNewStorageWizzardPage;

	/**
	 * Select agents wizard page.
	 */
	private SelectAgentsWizardPage selectAgentsWizardPage;

	/**
	 * Time-line wizard page.
	 */
	private DefineTimelineWizardPage timelineWizardPage;

	/**
	 * Add new label wizard page.
	 */
	private AddStorageLabelWizardPage addLabelWizardPage;

	/**
	 * Initially selected CMR.
	 */
	private CmrRepositoryDefinition selectedCmr;

	/**
	 * The collection of agents that will be automatically selected in the
	 * {@link SelectExistingStorageWizardPage}.
	 */
	private Collection<PlatformIdent> autoSelectedAgents;

	/**
	 * Recording properties defined in the wizard.
	 */
	private RecordingProperties recordingProperties;

	/**
	 * Public constructor.
	 */
	public StartRecordingWizard() {
		this.setWindowTitle("Start Recording Wizard");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_RECORD));
	}

	/**
	 * This constructor will extract the {@link CmrRepositoryDefinition} out of
	 * {@link IStorageDataProvider}.
	 *
	 * @param storageDataProvider
	 *            {@link IStorageDataProvider}.
	 */
	public StartRecordingWizard(IStorageDataProvider storageDataProvider) {
		this();
		this.selectedCmr = storageDataProvider.getCmrRepositoryDefinition();
	}

	/**
	 * This constructor gets the selected {@link CmrRepositoryDefinition}.
	 *
	 * @param cmrRepositoryDefinition
	 *            Selected {@link CmrRepositoryDefinition}.
	 */
	public StartRecordingWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this(cmrRepositoryDefinition, Collections.<PlatformIdent> emptyList());
	}

	/**
	 * The constructor sets the CMR and provides option to define the collection of agents that will
	 * be automatically selected in the {@link SelectExistingStorageWizardPage}.
	 *
	 * @param cmrRepositoryDefinition
	 *            Selected {@link CmrRepositoryDefinition}.
	 * @param autoSelectedAgents
	 *            The collection of agents that will be automatically selected in the
	 *            {@link SelectExistingStorageWizardPage}.
	 */
	public StartRecordingWizard(CmrRepositoryDefinition cmrRepositoryDefinition, Collection<PlatformIdent> autoSelectedAgents) {
		this();
		this.selectedCmr = cmrRepositoryDefinition;
		this.autoSelectedAgents = autoSelectedAgents;
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
		defineNewStorageWizzardPage = new DefineNewStorageWizzardPage(selectedCmr);
		addPage(defineNewStorageWizzardPage);
		selectStorageWizardPage = new SelectExistingStorageWizardPage(selectedCmr, true);
		addPage(selectStorageWizardPage);
		selectAgentsWizardPage = new SelectAgentsWizardPage("Select Agent(s) that should participate in recording", autoSelectedAgents);
		addPage(selectAgentsWizardPage);
		defineDataPage = new DefineDataProcessorsWizardPage(
				DefineDataProcessorsWizardPage.BUFFER_DATA | DefineDataProcessorsWizardPage.SYSTEM_DATA | DefineDataProcessorsWizardPage.EXTRACT_INVOCATIONS);
		addPage(defineDataPage);
		timelineWizardPage = new DefineTimelineWizardPage("Limit Recording", "Optionally select how long recording should last", DefineTimelineWizardPage.FUTURE | DefineTimelineWizardPage.BOTH_DATES);
		addPage(timelineWizardPage);
		addLabelWizardPage = new AddStorageLabelWizardPage(selectedCmr);
		addPage(addLabelWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		StorageData storageData;
		CmrRepositoryDefinition cmrRepositoryDefinition;
		boolean autoFinalize;

		if (newOrExistsingStorageWizardPage.useNewStorage()) {
			storageData = defineNewStorageWizzardPage.getStorageData();
			cmrRepositoryDefinition = defineNewStorageWizzardPage.getSelectedRepository();
			autoFinalize = defineNewStorageWizzardPage.isAutoFinalize();
		} else {
			storageData = selectStorageWizardPage.getSelectedStorageData();
			cmrRepositoryDefinition = selectStorageWizardPage.getSelectedRepository();
			autoFinalize = selectStorageWizardPage.isAutoFinalize();
		}

		List<AbstractDataProcessor> recordingProcessors = defineDataPage.getProcessorList();
		if (!selectAgentsWizardPage.isAllAgents()) {
			Set<Long> agentsIds = new HashSet<>(selectAgentsWizardPage.getSelectedAgents());
			AgentFilterDataProcessor agentFilterDataProcessor = new AgentFilterDataProcessor(recordingProcessors, agentsIds);
			recordingProcessors = new ArrayList<>(1);
			recordingProcessors.add(agentFilterDataProcessor);
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			recordingProperties = new RecordingProperties();
			recordingProperties.setRecordingDataProcessors(recordingProcessors);
			recordingProperties.setAutoFinalize(autoFinalize);
			if (timelineWizardPage.isTimerframeUsed()) {
				Date recordStartDate = timelineWizardPage.getFromDate();
				Date recordEndDate = timelineWizardPage.getToDate();
				Date now = new Date();
				if ((null != recordStartDate) && recordStartDate.after(now)) {
					recordingProperties.setStartDelay(recordStartDate.getTime() - now.getTime());
				}
				if ((null != recordEndDate) && recordEndDate.after(now)) {
					if ((null != recordStartDate) && recordStartDate.before(recordEndDate)) {
						recordingProperties.setRecordDuration(recordEndDate.getTime() - recordStartDate.getTime());
					} else {
						recordingProperties.setRecordDuration(recordEndDate.getTime() - now.getTime());
					}
				}
			}
			boolean canStart = cmrRepositoryDefinition.getStorageService().getRecordingState() == RecordingState.OFF;
			if (canStart) {
				try {
					StorageData recordingStorage = cmrRepositoryDefinition.getStorageService().startOrScheduleRecording(storageData, recordingProperties);
					List<AbstractStorageLabel<?>> labels = addLabelWizardPage.getLabelsToAdd();
					if (!labels.isEmpty()) {
						cmrRepositoryDefinition.getStorageService().addLabelsToStorage(recordingStorage, labels, true);
					}
				} catch (BusinessException e) {
					InspectIT.getDefault().createErrorDialog("Recording did not start.", e, -1);
					return false;
				}
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Recording did not start. Selected CMR repository is currently not available.", -1);
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
				return selectStorageWizardPage;
			}
		} else if (ObjectUtils.equals(page, defineNewStorageWizzardPage)) {
			selectAgentsWizardPage.setCmrRepositoryDefinition(defineNewStorageWizzardPage.getSelectedRepository());
			addLabelWizardPage.setStorageData(defineNewStorageWizzardPage.getStorageData());
			return selectAgentsWizardPage;
		} else if (ObjectUtils.equals(page, selectStorageWizardPage)) {
			selectAgentsWizardPage.setCmrRepositoryDefinition(selectStorageWizardPage.getSelectedRepository());
			addLabelWizardPage.setStorageData(selectStorageWizardPage.getSelectedStorageData());
			return selectAgentsWizardPage;
		} else {
			return super.getNextPage(page);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (ObjectUtils.equals(page, defineNewStorageWizzardPage) || ObjectUtils.equals(page, selectStorageWizardPage)) {
			return newOrExistsingStorageWizardPage;
		} else if (ObjectUtils.equals(page, selectAgentsWizardPage)) {
			if (newOrExistsingStorageWizardPage.useNewStorage()) {
				return defineNewStorageWizzardPage;
			} else {
				return selectStorageWizardPage;
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
		} else if (!newOrExistsingStorageWizardPage.useNewStorage() && !selectStorageWizardPage.isPageComplete()) {
			return false;
		} else if (!selectAgentsWizardPage.isPageComplete()) {
			return false;
		} else if (!defineDataPage.isPageComplete()) {
			return false;
		} else if (!timelineWizardPage.isPageComplete()) {
			return false;
		}
		return true;
	}

	/**
	 * Gets {@link #recordingProperties}.
	 *
	 * @return {@link #recordingProperties}
	 */
	public RecordingProperties getRecordingProperties() {
		return recordingProperties;
	}

}
