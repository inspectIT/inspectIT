package rocks.inspectit.ui.rcp.editor.testers;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.ui.rcp.editor.ISubView;
import rocks.inspectit.ui.rcp.editor.composite.AbstractCompositeSubView;
import rocks.inspectit.ui.rcp.editor.graph.GraphSubView;
import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.editor.search.ISearchExecutor;
import rocks.inspectit.ui.rcp.editor.table.TableSubView;
import rocks.inspectit.ui.rcp.editor.tree.SteppingTreeSubView;
import rocks.inspectit.ui.rcp.editor.tree.TreeSubView;

/**
 * @author Patrice Bouillet
 *
 */
public class ActiveSubViewTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof AbstractRootEditor) {
			AbstractRootEditor rootEditor = (AbstractRootEditor) receiver;
			if ("activeSubView".equals(property)) {
				if ("treeSubView".equals(expectedValue)) {
					return rootEditor.getActiveSubView() instanceof TreeSubView;
				} else if ("tableSubView".equals(expectedValue)) {
					return rootEditor.getActiveSubView() instanceof TableSubView;
				} else if ("notNull".equals(expectedValue)) {
					return rootEditor.getActiveSubView() != null;
				}
			} else if ("hasSubView".equals(property)) {
				if ("steppingTreeSubView".equals(expectedValue)) {
					return isSubViewExisting(rootEditor.getSubView(), SteppingTreeSubView.class);
				} else if ("graphSubView".equals(expectedValue)) {
					return isSubViewExisting(rootEditor.getSubView(), GraphSubView.class);
				} else if ("compositeSubView".equals(expectedValue)) {
					return isSubViewExisting(rootEditor.getSubView(), AbstractCompositeSubView.class);
				} else if ("searchExecutor".equals(expectedValue)) {
					return isSubViewExisting(rootEditor.getSubView(), ISearchExecutor.class);
				}
			}
		}

		return false;
	}

	/**
	 * Returns if the given sub view is a instance of given sub-view class or if there is a sub-view
	 * of this class in case composite sub-view is provided. This is a recursive method.
	 *
	 * @param subView
	 *            Sub-view to check.
	 * @param subViewClass
	 *            Class to search for.
	 * @return Returns true if the wanted class is found.
	 */
	private boolean isSubViewExisting(ISubView subView, Class<?> subViewClass) {
		if (subViewClass.isInstance(subView)) {
			return true;
		} else if (subView instanceof AbstractCompositeSubView) {
			AbstractCompositeSubView compositeSubView = (AbstractCompositeSubView) subView;
			for (ISubView viewInCompositeSubView : compositeSubView.getSubViews()) {
				if (isSubViewExisting(viewInCompositeSubView, subViewClass)) {
					return true;
				}
			}
		}
		return false;
	}
}
