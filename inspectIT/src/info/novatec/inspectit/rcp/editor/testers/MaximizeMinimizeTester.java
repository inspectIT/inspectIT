package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tester that checks if the active sub-view can be maximized/minimized.
 * 
 * @author Ivan Senic
 * 
 */
public class MaximizeMinimizeTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof AbstractRootEditor) {
			AbstractRootEditor rootEditor = (AbstractRootEditor) receiver;
			if ("canMaximize".equals(property)) {
				return rootEditor.canMaximizeActiveSubView();
			} else if ("canMinimize".equals(property)) {
				return rootEditor.canMinimizeActiveSubView();
			}
		}
		return false;
	}

}
