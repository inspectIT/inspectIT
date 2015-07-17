package info.novatec.inspectit.rcp.editor.graph.plot;

import java.util.EventListener;

/**
 * The interface that must be supported by classes that wish to receive notification of zooming
 * events into an axis.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface ZoomListener extends EventListener {

	/**
	 * Method is executed whenever a zooming occurs.
	 */
	void zoomOccured();

}
