package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.root.SubViewClassificationController.SubViewClassification;
import info.novatec.inspectit.rcp.editor.table.TableSubView;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;

import org.eclipse.core.expressions.PropertyTester;

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
