package info.novatec.inspectit.rcp.view.tree;

import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.Composite;
import info.novatec.inspectit.rcp.model.storage.LocalStorageTreeModelManager;
import info.novatec.inspectit.rcp.model.storage.StorageTreeModelManager;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

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
