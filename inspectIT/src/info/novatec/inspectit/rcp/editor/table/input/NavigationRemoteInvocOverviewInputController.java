package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.service.IRemoteCallDataAccessService;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * A extension of the {@link InvocOverviewInputController} that displays the invocations that are
 * called by remote call.
 * 
 * 
 * @author Thomas Kluge
 *
 */
public class NavigationRemoteInvocOverviewInputController extends InvocOverviewInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.navigationremoteinvocoverview";

	/**
	 * List of all invocation sequences that can be displayed.
	 */
	private List<RemoteCallData> remoteCallDataList;

	/**
	 * The remote data access service to access the data on the CMR.
	 */
	private IRemoteCallDataAccessService remoteDataAccessService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.REMOTE_INVOCATION_EXTRAS_MARKER)) {
			remoteCallDataList = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.REMOTE_INVOCATION_EXTRAS_MARKER).getRemoteCallDataList();
		}

		super.setInputDefinition(inputDefinition);

		remoteDataAccessService = inputDefinition.getRepositoryDefinition().getRemoteCallDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferences.add(PreferenceId.CLEAR_BUFFER);
		}
		preferences.add(PreferenceId.ITEMCOUNT);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		TableViewerComparator<InvocationSequenceData> invocationDataViewerComparator = new TableViewerComparator<InvocationSequenceData>();
		for (Column column : Column.values()) {
			ResultComparator<InvocationSequenceData> resultComparator = new ResultComparator<InvocationSequenceData>(column.dataComparator, getCachedDataService());
			invocationDataViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return invocationDataViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Updating Invocation Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the Invocation Overview from the CMR");
		List<InvocationSequenceData> invocData;
		Set<Long> invocationIdsSet = new HashSet<Long>();

		for (RemoteCallData remoteCallData : remoteCallDataList) {
			RemoteCallData temp = remoteDataAccessService.getRemoteCallData(remoteCallData.getRemotePlatformIdent(), remoteCallData.getIdentification(), !remoteCallData.isCalling());
			if (null != temp.getInvocationParentsIdSet()) {
				invocationIdsSet.addAll(temp.getInvocationParentsIdSet());
			}
		}

		invocData = getDataAccessService().getInvocationSequenceOverview(0, invocationIdsSet, getLimit(), getResultComparator());
		getInvocationSequenceData().clear();
		if (!invocData.isEmpty()) {
			monitor.subTask("Displaying the Invocation Overview");
			getInvocationSequenceData().addAll(invocData);
		}
		monitor.done();
	}

}
