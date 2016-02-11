package info.novatec.inspectit.rcp.view;

/**
 * Interface for all view that are refreshable.
 * 
 * @author Ivan Senic
 * 
 */
public interface IRefreshableView {

	/**
	 * Perform view refresh.
	 */
	void refresh();

	/**
	 * Defines if the view can be refreshed.
	 * 
	 * @return True if the view can be refreshed at the given point of call.
	 */
	boolean canRefresh();
}
