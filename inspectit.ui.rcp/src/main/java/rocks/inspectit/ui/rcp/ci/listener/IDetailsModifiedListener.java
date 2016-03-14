package rocks.inspectit.ui.rcp.ci.listener;

/**
 * Listener interface to be used on content modification within Master-Details constructs.
 *
 * @param <T>
 *            Type of the content element that has been modified.
 * @author Alexander Wert
 *
 */
public interface IDetailsModifiedListener<T> {

	/**
	 * Contents have been modified.
	 *
	 * @param modifiedElement
	 *            the modified element
	 */
	void contentModified(T modifiedElement);
}