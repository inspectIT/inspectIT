package rocks.inspectit.ui.rcp.editor.testers;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.editor.root.SubViewClassificationController.SubViewClassification;
import rocks.inspectit.ui.rcp.editor.table.TableSubView;
import rocks.inspectit.ui.rcp.editor.tree.TreeSubView;

/**
 * Tester for testing the sub view classification. The tester can test if the view is master or
 * slave.
 * 
 * @author Ivan Senic
 * 
 */
public class SubViewClassificationTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof AbstractRootEditor) {
			AbstractRootEditor rootEditor = (AbstractRootEditor) receiver;
			if (rootEditor.getActiveSubView() instanceof TableSubView) {
				TableSubView tableSubView = (TableSubView) rootEditor.getActiveSubView();
				if ("master".equals(expectedValue)) {
					if (tableSubView.getTableInputController().getSubViewClassification() == SubViewClassification.MASTER) {
						return true;
					}
				} else if ("slave".equals(expectedValue)) {
					if (tableSubView.getTableInputController().getSubViewClassification() == SubViewClassification.SLAVE) {
						return true;
					}
				}
			} else if (rootEditor.getActiveSubView() instanceof TreeSubView) {
				TreeSubView treeSubView = (TreeSubView) rootEditor.getActiveSubView();
				if ("master".equals(expectedValue)) {
					if (treeSubView.getTreeInputController().getSubViewClassification() == SubViewClassification.MASTER) {
						return true;
					}
				} else if ("slave".equals(expectedValue)) {
					if (treeSubView.getTreeInputController().getSubViewClassification() == SubViewClassification.SLAVE) {
						return true;
					}
				}

			}
		}

		return false;
	}

}
