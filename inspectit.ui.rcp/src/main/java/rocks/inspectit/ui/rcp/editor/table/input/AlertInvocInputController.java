package rocks.inspectit.ui.rcp.editor.table.input;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerComparator;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.editor.table.TableViewerComparator;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Sub-class of {@link InvocOverviewInputController} that shows only the invocation sequences
 * related to an alert.
 *
 * @author Alexander Wert
 *
 */
public class AlertInvocInputController extends InvocOverviewInputController {

	/**
	 * The alert.
	 */
	private Alert alert;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.ALERT_EXTRAS_MARKER)) {
			alert = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.ALERT_EXTRAS_MARKER).getAlert();
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
			preferences.add(PreferenceId.LIVEMODE);
		}
		preferences.add(PreferenceId.UPDATE);
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
		getInvocationSequenceData().clear();
		List<InvocationSequenceData> invocData;
		try {
			invocData = getDataAccessService().getInvocationSequenceOverview(alert.getId(), getLimit(), getResultComparator());
			if (!invocData.isEmpty()) {
				monitor.subTask("Displaying the Invocation Overview");
				getInvocationSequenceData().addAll(invocData);
			}
		} catch (BusinessException e) {
			InspectIT.getDefault().createErrorDialog("Failed loading invocation sequences for alert '" + alert + "'", e, -1);
		}
		monitor.done();
	}
}
