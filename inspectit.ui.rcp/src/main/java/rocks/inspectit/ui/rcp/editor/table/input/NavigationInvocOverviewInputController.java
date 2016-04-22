package rocks.inspectit.ui.rcp.editor.table.input;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerComparator;

import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.editor.table.TableViewerComparator;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * A extension of the {@link InvocOverviewInputController} that displays the invocations that are
 * statically transfered to the view via invocation aware data.
 *
 * @author Ivan Senic
 *
 */
public class NavigationInvocOverviewInputController extends InvocOverviewInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.navigationinvocoverview";

	/**
	 * List of all invocation sequences that can be displayed.
	 */
	private List<InvocationAwareData> invocationAwareDataList;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.NAVIGATION_STEPPING_EXTRAS_MARKER)) {
			invocationAwareDataList = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.NAVIGATION_STEPPING_EXTRAS_MARKER).getInvocationAwareDataList();
		}

		super.setInputDefinition(inputDefinition);
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
		TableViewerComparator<InvocationSequenceData> invocationDataViewerComparator = new TableViewerComparator<>();
		for (Column column : Column.values()) {
			ResultComparator<InvocationSequenceData> resultComparator = new ResultComparator<>(column.dataComparator, getCachedDataService());
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
		Set<Long> invocationIdsSet = new HashSet<>();
		for (InvocationAwareData invocationAwareData : invocationAwareDataList) {
			if (null != invocationAwareData.getInvocationParentsIdSet()) {
				invocationIdsSet.addAll(invocationAwareData.getInvocationParentsIdSet());
			}
		}
		long platformIdent = getInputDefinition().getIdDefinition().getPlatformId();
		invocData = getDataAccessService().getInvocationSequenceOverview(platformIdent, invocationIdsSet, getLimit(), getResultComparator());
		getInvocationSequenceData().clear();
		if (!invocData.isEmpty()) {
			monitor.subTask("Displaying the Invocation Overview");
			getInvocationSequenceData().addAll(invocData);
		}
		monitor.done();
	}

}
