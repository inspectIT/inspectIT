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
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeUtil;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.tree.InvocationTreeContentProvider;
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
		steppingObjectsList = new ArrayList<>();

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
	@Override
	public ElementOccurrenceCount countOccurrences(Object element, ViewerFilter[] filters) {
		InvocationTreeContentProvider contentProvider = (InvocationTreeContentProvider) getContentProvider();

		// count span occurence
		if (element instanceof Span) {
			boolean containsSpan = contentProvider.getLookupMap().containsKey(InvocationTreeUtil.calculateLookupKey(element));

			if (containsSpan) {
				ElementOccurrenceCount elementCount = new ElementOccurrenceCount();
				elementCount.increaseVisibleOccurrences();
				return elementCount;
			}
		}

		// count invocation occurence
		if (contentProvider.getRootElement() != null) {
			List<InvocationSequenceData> sequences;
			sequences = InvocationTreeUtil.getInvocationSequences(contentProvider.getRootElement());

			return OccurrenceFinderFactory.getOccurrenceCount(sequences, element, filters);
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
	@Override
	public Object getElement(Object template, int occurance, ViewerFilter[] filters) {
		// return the span directly because it is unique
		if (template instanceof Span) {
			return template;
		}

		// returning matching invocations
		InvocationTreeContentProvider contentProvider = (InvocationTreeContentProvider) getContentProvider();
		if (contentProvider.getRootElement() != null) {
			List<InvocationSequenceData> sequences;
			sequences = InvocationTreeUtil.getInvocationSequences(contentProvider.getRootElement());

			InvocationSequenceData found = OccurrenceFinderFactory.getOccurrence(sequences, template, occurance, filters);
			if (InvocationSequenceDataHelper.hasSpanIdent(found) && (template instanceof Span)) {
				return spanService.get(found.getSpanIdent());
			} else {
				return found;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getElementTextualRepresentation(Object object) {
		if (object instanceof SqlStatementData) {
			SqlStatementData sqlData = (SqlStatementData) object;
			if (0 == sqlData.getId()) {
				return "SQL: " + sqlData.getSql() + " [All]";
			} else {
				return "SQL: " + sqlData.getSql() + " [Single]";
			}
		} else if (object instanceof TimerData) {
			TimerData timerData = (TimerData) object;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(timerData.getMethodIdent());
			if (0 == timerData.getId()) {
				return TextFormatter.getMethodString(methodIdent) + " [All]";
			} else {
				return TextFormatter.getMethodString(methodIdent) + " [Single]";
			}
		} else if (object instanceof ExceptionSensorData) {
			ExceptionSensorData exData = (ExceptionSensorData) object;
			if (0 == exData.getId()) {
				return "Exception: " + exData.getThrowableType() + " [All]";
			} else {
				return "Exception: " + exData.getThrowableType() + " [Single]";
			}
		} else if (object instanceof InvocationSequenceData) {
			InvocationSequenceData invocationSequenceData = (InvocationSequenceData) object;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(invocationSequenceData.getMethodIdent());
			if (0 == invocationSequenceData.getId()) {
				return TextFormatter.getMethodString(methodIdent) + " [All]";
			} else {
				return TextFormatter.getMethodString(methodIdent) + " [Single]";
			}
		} else if (object instanceof Span) {
			return TextFormatter.getSpanDetailsFull((Span) object, cachedDataService).toString();
		}
		return "";
	}

}
