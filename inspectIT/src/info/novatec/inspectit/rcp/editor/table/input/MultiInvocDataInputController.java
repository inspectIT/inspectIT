package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This view only displays the {@link info.novatec.inspectit.communication.data.TimerData} that are
 * in the invocations provided by the invocationIdsList to this view via the
 * {@link InputDefinition#getAdditionalOption(Object)}.
 * 
 * @author Ivan Senic
 * 
 */
public class MultiInvocDataInputController extends InvocOverviewInputController {

	/**
	 * Key for multi invocation list in the additional options.
	 */
	public static final Object ADDITIONAL_OPTION_KEY = "MULTI_INVOCATION_LIST";

	/**
	 * List of invocations to be loaded.
	 */
	private List<InvocationSequenceData> invocationList;

	/**
	 * List of loaded invocations that are complete.
	 */
	private List<InvocationSequenceData> loadedInvocations;

	/**
	 * List of invocations that are selected.
	 */
	private List<InvocationSequenceData> selectedList;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.COMBINED_INVOCATIONS_EXTRAS_MARKER)) {
			invocationList = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.COMBINED_INVOCATIONS_EXTRAS_MARKER).getTemplates();
		}

		super.setInputDefinition(inputDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
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
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		return false;
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
		monitor.beginTask("Updating Invocation Overview", invocationList.size());
		monitor.subTask("Retrieving the Invocation Overview from the CMR");
		loadedInvocations = new ArrayList<InvocationSequenceData>();
		for (InvocationSequenceData template : invocationList) {
			loadedInvocations.add(getDataAccessService().getInvocationSequenceDetail(template));
			monitor.worked(1);
		}
		getInvocationSequenceData().clear();
		if (!loadedInvocations.isEmpty()) {
			monitor.subTask("Displaying the Invocation Overview");
			getInvocationSequenceData().addAll(loadedInvocations);
		}
		selectedList = new ArrayList<InvocationSequenceData>(loadedInvocations);
		passSelectedList();
		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void objectChecked(Object object, boolean checked) {
		if (object instanceof InvocationSequenceData) {
			InvocationSequenceData selected = (InvocationSequenceData) object;
			if (checked) {
				for (InvocationSequenceData inData : loadedInvocations) {
					if (inData.getId() == selected.getId()) {
						if (!selectedList.contains(inData)) {
							selectedList.add(inData);
						}
						break;
					}
				}
			} else {
				for (InvocationSequenceData inData : selectedList) {
					if (inData.getId() == selected.getId()) {
						selectedList.remove(inData);
						break;
					}
				}
			}
			passSelectedList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCheckStyle() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean areItemsInitiallyChecked() {
		return true;
	}

	/**
	 * Passes the list of selected invocations to the subviews.
	 */
	private void passSelectedList() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
				rootEditor.setDataInput(selectedList);
			}
		});
	}
}
