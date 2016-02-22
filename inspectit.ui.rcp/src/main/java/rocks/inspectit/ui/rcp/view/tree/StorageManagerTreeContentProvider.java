package rocks.inspectit.ui.rcp.view.tree;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import rocks.inspectit.ui.rcp.model.Component;
import rocks.inspectit.ui.rcp.model.Composite;
import rocks.inspectit.ui.rcp.model.storage.LocalStorageTreeModelManager;
import rocks.inspectit.ui.rcp.model.storage.StorageTreeModelManager;

/**
 * Content provider for the storage tree.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageManagerTreeContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof StorageTreeModelManager) {
			return ((StorageTreeModelManager) inputElement).getRootObjects();
		} else if (inputElement instanceof LocalStorageTreeModelManager) {
			return ((LocalStorageTreeModelManager) inputElement).getRootObjects();
		} else {
			return super.getElements(inputElement);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Composite) {
			return ((Composite) parentElement).getChildren().toArray();
		}
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
		if (element instanceof Component) {
			return ((Component) element).getParent();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof Composite) {
			return !((Composite) element).getChildren().isEmpty();
		}
		return false;
	}

}
