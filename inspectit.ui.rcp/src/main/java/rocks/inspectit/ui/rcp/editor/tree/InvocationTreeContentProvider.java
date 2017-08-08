package rocks.inspectit.ui.rcp.editor.tree;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeBuilder;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeElement;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeBuilder.Mode;

/**
 * Content provider for invocation and trace trees.
 *
 * @author Marius Oehler
 *
 */
public class InvocationTreeContentProvider implements ITreeContentProvider {

	/**
	 * The span service.
	 */
	private ISpanService spanService;

	/**
	 * The invocation data access service.
	 */
	private IInvocationDataAccessService invocationDataAccessService;

	/**
	 * The tree which underlies this content provider.
	 */
	private InvocationTreeElement currentTree;

	/**
	 * The input for this content provider.
	 */
	private Object input;

	/**
	 * The mode to use by this builder.
	 */
	private Mode buildMode;

	/**
	 * Constructor.
	 * 
	 * @param spanService
	 *            the span service to use
	 * @param invocationDataAccessService
	 *            the invocation data access service to use
	 */
	public InvocationTreeContentProvider(ISpanService spanService, IInvocationDataAccessService invocationDataAccessService) {
		this.spanService = spanService;
		this.invocationDataAccessService = invocationDataAccessService;
	}

	/**
	 * Sets {@link #buildMode}.
	 *
	 * @param buildMode
	 *            New value for {@link #buildMode}
	 */
	public void setBuildMode(Mode buildMode) {
		this.buildMode = buildMode;
	}

	/**
	 * Sets {@link #currentTree}.
	 *
	 * @param currentTree
	 *            New value for {@link #currentTree}
	 */
	public void setTree(InvocationTreeElement currentTree) {
		this.currentTree = currentTree;
	}

	/**
	 * Gets {@link #currentTree}.
	 *
	 * @return {@link #currentTree}
	 */
	public InvocationTreeElement getTree() {
		return this.currentTree;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

	/**
	 * Gets {@link #input}.
	 *
	 * @return {@link #input}
	 */
	public Object getInput() {
		return this.input;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		input = newInput;

		if ((input == null)) {
			return;
		}

		List<Object> inputList = (List<Object>) newInput;
		if (CollectionUtils.isEmpty(inputList)) {
			return;
		}

		Object inputObject = inputList.get(0);

		InvocationTreeBuilder builder = new InvocationTreeBuilder().setSpanService(spanService).setInvocationService(invocationDataAccessService);

		if (inputObject instanceof InvocationSequenceData) {
			builder.setInvocationSequence((InvocationSequenceData) inputObject);
			if (buildMode == null) {
				buildMode = Mode.SINGLE;
			}
		} else if (inputObject instanceof Long) {
			builder.setTraceId((long) inputObject);
			if (buildMode == null) {
				buildMode = Mode.ALL;
			}
		} else if (inputObject instanceof Span) {
			builder.setTraceId(((Span) inputObject).getSpanIdent().getTraceId());
			if (buildMode == null) {
				buildMode = Mode.ALL;
			}
		}

		builder.setMode(buildMode);

		currentTree = builder.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		Object inputObject = ((List<Object>) inputElement).get(0);

		InvocationTreeElement ite = currentTree.lookup(inputObject);
		if (ite == null) {
			return new Object[] { inputElement };
		}

		if ((ite.getParent() != null) && ite.getParent().isSpan() && !((Span) ite.getParent().getDataElement()).isCaller()) {
			return new Object[] { ite.getParent().getDataElement() };
		} else {
			return new Object[] { ite.getDataElement() };
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getChildren(Object inputElement) {
		InvocationTreeElement treeElement = currentTree.lookup(inputElement);

		List<Object> objects = new ArrayList<>();

		for (InvocationTreeElement its : treeElement.getChildren()) {
			objects.add(its.getDataElement());
		}

		return objects.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object element) {
		InvocationTreeElement ite = currentTree.lookup(element);
		if ((ite == null) || (ite.getParent() == null)) {
			return null;
		} else {
			return ite.getParent().getDataElement();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren(Object element) {
		InvocationTreeElement ite = currentTree.lookup(element);
		return ite.hasChildren();
	}
}
