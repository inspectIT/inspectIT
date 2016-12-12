package rocks.inspectit.ui.rcp.editor.tree.input;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.ui.rcp.editor.tree.util.TraceTreeData;
import rocks.inspectit.ui.rcp.util.ElementOccurrenceCount;
import rocks.inspectit.ui.rcp.util.OccurrenceFinderFactory;

/**
 * Controller that combines all invocations belonging to one trace into a tree representing complete
 * trace call hierarchy.
 *
 * @author Ivan Senic
 *
 */
public class TraceInvocDetailsInputController extends SteppingInvocDetailInputController {

	/**
	 * Current view input.
	 */
	private TraceTreeData currentInput;

	/**
	 * Default constructor.
	 */
	public TraceInvocDetailsInputController() {
		super(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends Object> data) {
		if (null == data) {
			return false;
		}

		if (CollectionUtils.isEmpty(data)) {
			return true;
		}
		// we expect one trace tree data object
		return data.get(0) instanceof TraceTreeData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTreeInput() {
		return currentInput;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementOccurrenceCount countOccurrences(Object element, ViewerFilter[] filters) {
		if (null != currentInput) {
			return OccurrenceFinderFactory.getOccurrenceCount(TraceTreeData.collectInvocations(currentInput, new ArrayList<InvocationSequenceData>()), element, filters);
		}
		return ElementOccurrenceCount.emptyElement();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getElement(Object template, int occurance, ViewerFilter[] filters) {
		if (currentInput != null) {
			InvocationSequenceData found = OccurrenceFinderFactory.getOccurrence(TraceTreeData.collectInvocations(currentInput, new ArrayList<InvocationSequenceData>()), template, occurance, filters);
			if (null != found) {
				if (InvocationSequenceDataHelper.hasSpanIdent(found) && (template instanceof Span)) {
					return spanService.get(found.getSpanIdent());
				} else {
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new ContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object[] getObjectsToSearch(Object treeInput) {
		ArrayList<Object> objects = new ArrayList<>();
		TraceTreeData data = (TraceTreeData) ((List<Object>) treeInput).get(0);
		extractAllChildren(objects, data);
		return objects.toArray();
	}

	/**
	 * Extracts all children from the trace tree data that can be searched for.
	 *
	 * @param objects
	 *            list to extract to
	 * @param data
	 *            trace data
	 */
	public void extractAllChildren(List<Object> objects, TraceTreeData data) {
		objects.add(data.getSpan());

		if (CollectionUtils.isNotEmpty(data.getInvocations())) {
			objects.addAll(data.getInvocations());
		}

		for (TraceTreeData child : data.getChildren()) {
			extractAllChildren(objects, child);
		}
	}

	/**
	 * The content provider for this view.
	 *
	 * @author Ivan Senic
	 *
	 */
	private final class ContentProvider extends InvocDetailContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			List<Object> inputList = (List<Object>) newInput;
			if (CollectionUtils.isNotEmpty(inputList)) {
				Object first = inputList.get(0);
				if (first instanceof TraceTreeData) {
					currentInput = (TraceTreeData) first;
					return;
				}
			}
			currentInput = null; // NOPMD
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			List<Object> inputList = (List<Object>) inputElement;
			if (CollectionUtils.isNotEmpty(inputList)) {
				Object first = inputList.get(0);
				if (first instanceof TraceTreeData) {
					TraceTreeData data = (TraceTreeData) first;
					return new Object[] { data.getSpan() };
				}
			}

			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof InvocationSequenceData) {
				return super.getChildren((InvocationSequenceData) parentElement);
			}

			if (parentElement instanceof Span) {
				Span span = (Span) parentElement;
				TraceTreeData traceData = TraceTreeData.getForSpanIdent(currentInput, span.getSpanIdent());
				if (null != traceData) {
					List<Object> objects = new ArrayList<>();
					if (!span.isCaller()) {
						objects.addAll(traceData.getInvocations());
						// also all children that don't have invocations
						for (TraceTreeData child : traceData.getChildren()) {
							if (!child.getSpan().isCaller()) {
								objects.add(child.getSpan());
							}
						}
					} else {
						for (TraceTreeData child : traceData.getChildren()) {
							objects.add(child.getSpan());
						}
					}
					return objects.toArray();
				}
			}

			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getParent(Object element) {
			if ((element instanceof InvocationSequenceData)) {
				return super.getParent((InvocationSequenceData) element);
			}

			if (element instanceof Span) {
				Span span = (Span) element;
				TraceTreeData traceData = TraceTreeData.getForSpanIdent(currentInput, span.getSpanIdent());
				if (!span.isCaller()) {
					TraceTreeData parent = traceData.getParent();
					if (null != parent) {
						return parent.getSpan();
					}
				} else {
					TraceTreeData parent = traceData.getParent();
					while (null != parent) {
						InvocationSequenceData invoc = getForSpanIdent(parent.getInvocations(), span.getSpanIdent());
						if (null != invoc) {
							return invoc;
						}
						parent = parent.getParent();
					}
				}
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof InvocationSequenceData) {
				return super.hasChildren((InvocationSequenceData) element);
			}

			if (element instanceof Span) {
				Span span = (Span) element;
				TraceTreeData traceData = TraceTreeData.getForSpanIdent(currentInput, span.getSpanIdent());
				if (null != traceData) {
					if (!span.isCaller()) {
						return (CollectionUtils.isNotEmpty(traceData.getChildren())) || CollectionUtils.isNotEmpty(traceData.getInvocations());
					} else {
						return CollectionUtils.isNotEmpty(traceData.getChildren());
					}
				}
			}

			return false;
		}

	}

}
