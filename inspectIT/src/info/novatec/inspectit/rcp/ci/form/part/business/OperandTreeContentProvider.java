package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.IContainerExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.util.CollectionUtils;

/**
 * Content provider for the tree viewer that displays the expression tree.
 *
 * @author Alexander Wert
 *
 */
public final class OperandTreeContentProvider implements ITreeContentProvider {
	/**
	 * This map holds the child parent relationship.
	 */
	private final Map<Object, Object> parentMap = new HashMap<>();

	/**
	 * root expression.
	 */
	private AbstractExpression rootExpression;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		parentMap.clear();
		if (newInput instanceof TreeInput) {
			rootExpression = ((TreeInput) newInput).getExpression();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof TreeInput) {
			AbstractExpression expression = ((TreeInput) inputElement).getExpression();
			if (null != expression) {
				return new Object[] { expression };
			}
		}
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IContainerExpression) {
			return ((IContainerExpression) parentElement).getOperands().toArray();
		}
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object element) {
		// needed for direct expansion of the elements
		Object parent = null;
		if (element instanceof AbstractExpression) {
			if (element.equals(rootExpression)) {
				return null;
			}
			parent = parentMap.get(element);
			if (null == parent) {
				parent = searchParent(rootExpression, (AbstractExpression) element);
				parentMap.put(element, parent);
			}
		}

		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainerExpression) {
			return !((IContainerExpression) element).getOperands().isEmpty();
		}
		return false;
	}

	/**
	 * Searches parent for the target {@link AbstractExpression} beginning from the root element.
	 * This is a recursive method.
	 *
	 * @param parentCandidate
	 *            parent candidate to check
	 * @param target
	 *            target {@link AbstractExpression} instance
	 * @return the parent of the target {@link AbstractExpression} instance or null, if target is a
	 *         root element.
	 */
	private AbstractExpression searchParent(AbstractExpression parentCandidate, AbstractExpression target) {
		if (null == parentCandidate || parentCandidate.equals(target)) {
			return null;
		}

		List<? extends AbstractExpression> children = null;
		if (parentCandidate instanceof IContainerExpression) {
			children = ((IContainerExpression) parentCandidate).getOperands();
		}
		if (CollectionUtils.isEmpty(children)) {
			return null;
		}
		for (AbstractExpression child : children) {
			if (target.equals(child)) {
				return parentCandidate;
			} else {
				AbstractExpression childResult = searchParent(child, target);
				if (null != childResult) {
					return childResult;
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

	/**
	 * Wrapper class for the tree input to avoid infinite recursion that may occur when using the
	 * same class for the input and the tree elements.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class TreeInput {
		/**
		 * Root expression.
		 */
		private final AbstractExpression expression;

		/**
		 * Constructor.
		 *
		 * @param expression
		 *            root expression.
		 */
		TreeInput(AbstractExpression expression) {
			this.expression = expression;
		}

		/**
		 * Gets {@link #expression}.
		 *
		 * @return {@link #expression}
		 */
		public AbstractExpression getExpression() {
			return expression;
		}
	}
}
