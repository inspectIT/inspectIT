package info.novatec.inspectit.rcp.editor.graph.plot;

import org.eclipse.core.runtime.ListenerList;
import org.jfree.chart.axis.DateAxis;

/**
 * This class extends the date axis from JFreeChart and adds the possibility to be notified if a
 * zooming event occurs.
 * 
 * @author Patrice Bouillet
 * 
 */
public class DateAxisZoomNotify extends DateAxis {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2365282634625595024L;

	/**
	 * The registered listeners.
	 */
	private transient ListenerList zoomListeners = new ListenerList();

	/**
	 * Adds a zoom listener.
	 * 
	 * @param zoomListener
	 *            The zoom listener to add.
	 */
	public void addZoomListener(ZoomListener zoomListener) {
		zoomListeners.add(zoomListener);
	}

	/**
	 * Removes a zoom listener.
	 * 
	 * @param zoomListener
	 *            The zoom listener to remove.
	 */
	public void removeZoomListener(ZoomListener zoomListener) {
		zoomListeners.remove(zoomListener);
	}

	/**
	 * Notifies all zoom listeners that a zooming event occurred.
	 */
	public void notifyZoomListeners() {
		Object[] listeners = zoomListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((ZoomListener) listeners[i]).zoomOccured();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resizeRange(double percent) {
		super.resizeRange(percent);

		notifyZoomListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void zoomRange(double lowerPercent, double upperPercent) {
		super.zoomRange(lowerPercent, upperPercent);

		notifyZoomListeners();
	}

}
