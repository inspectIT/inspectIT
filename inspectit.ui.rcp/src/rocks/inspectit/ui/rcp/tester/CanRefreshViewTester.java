package info.novatec.inspectit.rcp.tester;

import info.novatec.inspectit.rcp.view.IRefreshableView;

import org.eclipse.core.expressions.PropertyTester;

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
		return receiver instanceof IRefreshableView && ((IRefreshableView) receiver).canRefresh();
	}

}
