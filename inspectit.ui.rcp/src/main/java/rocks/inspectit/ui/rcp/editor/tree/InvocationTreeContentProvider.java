package rocks.inspectit.ui.rcp.editor.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeElement;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeUtil;

/**
 * Content provider for invocation and trace trees.
 *
 * @author Marius Oehler
 *
 */
public class InvocationTreeContentProvider implements ITreeContentProvider {

	/**
	 * Lookup map of all elements in the current tree.
	 */
	private Map<Object, InvocationTreeElement> lookupMap;

	/**
	 * The root element of the tree.
	 */
	private InvocationTreeElement rootElement;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		InvocationTreeElement input = validateInput(newInput);
		if (input == null) {
			return;
		}

		lookupMap = InvocationTreeUtil.buildLookupMap(input);

		rootElement = InvocationTreeUtil.getRoot(input);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		InvocationTreeElement element = validateInput(inputElement);
		if (element == null) {
			return new Object[0];
		}

		return new Object[] { element.getDataElement() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getChildren(Object inputElement) {
		InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(lookupMap, inputElement);

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
		InvocationTreeElement ite = InvocationTreeUtil.lookupTreeElement(lookupMap, element);
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
		InvocationTreeElement ite = InvocationTreeUtil.lookupTreeElement(lookupMap, element);
		if (ite == null) {
			return false;
		}
		return ite.hasChildren();
	}

	/**
	 * Gets {@link #lookupMap}.
	 *
	 * @return {@link #lookupMap}
	 */
	public Map<Object, InvocationTreeElement> getLookupMap() {
		return this.lookupMap;
	}

	/**
	 * Gets {@link #rootElement}.
	 *
	 * @return {@link #rootElement}
	 */
	public InvocationTreeElement getRootElement() {
		return this.rootElement;
	}

	/**
	 * Validates that a {@link InvocationTreeElement} exists in the given input. It can be a single
	 * instance of it or contained in a collection.
	 *
	 * @param input
	 *            the input
	 * @return {@link InvocationTreeElement} contained in the input
	 */
	private InvocationTreeElement validateInput(Object input) {
		if (input == null) {
			return null;
		} else if (input instanceof Collection) {
			Collection<?> inputCollection = (Collection<?>) input;
			if (CollectionUtils.isEmpty(inputCollection)) {
				return null;
			} else {
				return (InvocationTreeElement) inputCollection.iterator().next();
			}
		} else if (input instanceof InvocationTreeElement) {
			return (InvocationTreeElement) input;
		}
		return null;
	}
}
