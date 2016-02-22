package rocks.inspectit.ui.rcp.editor.tree.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ViewerFilter;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.util.ElementOccurrenceCount;
import rocks.inspectit.ui.rcp.util.OccurrenceFinderFactory;

/**
 * Extension of the {@link InvocDetailInputController} adapted to serve as an input for a
 * {@link rocks.inspectit.ui.rcp.editor.tree.SteppingTreeSubView}.
 * 
 * @author Ivan Senic
 * 
 */
public class SteppingInvocDetailInputController extends InvocDetailInputController implements SteppingTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.steppinginvocdetail";

	/**
	 * List of the objects that are possible to locate in the tree.
	 */
	private List<Object> steppingObjectsList;

	/**
	 * Global data access service.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * Is stepping control be initially visible.
	 */
	private boolean initVisible;

	/**
	 * Constructor that defines if the stepping control is visible or not.
	 * 
	 * @param initVisible
	 *            Should stepping control be initially visible.
	 */
	public SteppingInvocDetailInputController(boolean initVisible) {
		this.initVisible = initVisible;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);
		steppingObjectsList = new ArrayList<Object>();

		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.NAVIGATION_STEPPING_EXTRAS_MARKER)) {
			List<DefaultData> steppingObj = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.NAVIGATION_STEPPING_EXTRAS_MARKER).getSteppingTemplateList();
			if (null != steppingObj) {
				for (DefaultData object : steppingObj) {
					addObjectToSteppingObjectList(object);
				}
			}
		}

		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = super.getPreferenceIds();
		preferences.add(PreferenceId.STEPPABLE_CONTROL);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> getSteppingObjectList() {
		return steppingObjectsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectToSteppingObjectList(Object template) {
		if (!steppingObjectsList.contains(template)) {
			steppingObjectsList.add(template);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean initSteppingControlVisible() {
		return initVisible;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ElementOccurrenceCount countOccurrences(Object element, ViewerFilter[] filters) {
		List<Object> input = (List<Object>) getTreeInput();
		if (input != null && !input.isEmpty()) {
			InvocationSequenceData invocation = (InvocationSequenceData) input.get(0);
			return OccurrenceFinderFactory.getOccurrenceCount(invocation, element, filters);
		}
		return ElementOccurrenceCount.emptyElement();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isElementOccurrenceReachable(Object element, int occurance, ViewerFilter[] filters) {
		Object object = getElement(element, occurance, filters);
		if (null != object) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getElement(Object template, int occurance, ViewerFilter[] filters) {
		List<Object> input = (List<Object>) getTreeInput();
		if (input != null && !input.isEmpty()) {
			InvocationSequenceData invocation = (InvocationSequenceData) input.get(0);
			return OccurrenceFinderFactory.getOccurrence(invocation, template, occurance, filters);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getElementTextualRepresentation(Object invAwareData) {
		if (invAwareData instanceof SqlStatementData) {
			SqlStatementData sqlData = (SqlStatementData) invAwareData;
			if (0 == sqlData.getId()) {
				return "SQL: " + sqlData.getSql() + " [All]";
			} else {
				return "SQL: " + sqlData.getSql() + " [Single]";
			}
		} else if (invAwareData instanceof TimerData) {
			TimerData timerData = (TimerData) invAwareData;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(timerData.getMethodIdent());
			if (0 == timerData.getId()) {
				return TextFormatter.getMethodString(methodIdent) + " [All]";
			} else {
				return TextFormatter.getMethodString(methodIdent) + " [Single]";
			}
		} else if (invAwareData instanceof ExceptionSensorData) {
			ExceptionSensorData exData = (ExceptionSensorData) invAwareData;
			if (0 == exData.getId()) {
				return "Exception: " + exData.getThrowableType() + " [All]";
			} else {
				return "Exception: " + exData.getThrowableType() + " [Single]";
			}
		} else if (invAwareData instanceof InvocationSequenceData) {
			InvocationSequenceData invocationSequenceData = (InvocationSequenceData) invAwareData;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(invocationSequenceData.getMethodIdent());
			if (0 == invocationSequenceData.getId()) {
				return TextFormatter.getMethodString(methodIdent) + " [All]";
			} else {
				return TextFormatter.getMethodString(methodIdent) + " [Single]";
			}
		}
		return "";
	}

}
