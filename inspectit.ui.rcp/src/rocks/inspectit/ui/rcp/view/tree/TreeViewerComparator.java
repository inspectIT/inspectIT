package info.novatec.inspectit.rcp.view.tree;

import info.novatec.inspectit.rcp.model.DeferredComposite;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.PendingUpdateAdapter;

/**
 * This comparator is used to sort the elements in the server view. Only the ones in the
 * instrumentation browser are affected by the sorting. Additionally, the
 * {@link PendingUpdateAdapter} will always be displayed as the last element.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TreeViewerComparator extends TreePathViewerSorter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Viewer viewer, TreePath parentPath, Object e1, Object e2) {
		if (null == parentPath) {
			return 1;
		}

		if (e1 instanceof PendingUpdateAdapter) {
			return -1;
		}

		if (parentPath.getLastSegment() instanceof DeferredComposite) {
			IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
			ILabelProvider lprov = (ILabelProvider) prov;
			String name1 = lprov.getText(e1);
			String name2 = lprov.getText(e2);

			boolean e1LowerCase = Character.isLowerCase(name1.charAt(0));
			boolean e2LowerCase = Character.isLowerCase(name2.charAt(0));

			if (e1LowerCase && e2LowerCase) {
				return super.compare(viewer, parentPath, e1, e2);
			} else if (!e1LowerCase && !e2LowerCase) {
				return super.compare(viewer, parentPath, e1, e2);
			} else if (e1LowerCase) {
				return -1;
			} else {
				return 1;
			}
		}

		return 1;
	}

}
