package rocks.inspectit.ui.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.ui.rcp.view.IRefreshableView;

/**
 * Tester to check if {@link IRefreshableView} can be refreshed.
 *
 * @author Ivan Senic
 *
 */
public class CanRefreshViewTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return (receiver instanceof IRefreshableView) && ((IRefreshableView) receiver).canRefresh();
	}

}
