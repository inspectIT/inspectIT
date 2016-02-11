package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.rcp.util.ElementOccurrenceCount;

import java.util.List;

import org.eclipse.jface.viewers.ViewerFilter;

/**
 * An extension of {@link TreeInputController} that provides the necessary functionality for
 * supporting {@link info.novatec.inspectit.rcp.editor.tree.SteppingTreeSubView}.
 * 
 * @author Ivan Senic
 * 
 */
public interface SteppingTreeInputController extends TreeInputController {

	/**
	 * 
	 * @return List of the objects that are possible to be located in the tree.
	 */
	List<Object> getSteppingObjectList();

	/**
	 * Counts number of occurrences of one stepping element in the current tree input.
	 * 
	 * @param element
	 *            Template element to count occurrences for.
	 * @param filters
	 *            Array of filters that each occurrence has to pass, so that it is included in the
	 *            count.
	 * @return Number of occurrences.
	 */
	ElementOccurrenceCount countOccurrences(Object element, ViewerFilter[] filters);

	/**
	 * Checks if the supplied occurrence of one stepping element in reachable in the current tree
	 * input.
	 * 
	 * @param element
	 *            Template element.
	 * @param occurance
	 *            Wanted occurrence.
	 * @param filters
	 *            Array of filters that each occurrence has to pass, so that it is included in the
	 *            count, and final result.
	 * @return True if wanted occurrence for the object is reachable, otherwise false.
	 */
	boolean isElementOccurrenceReachable(Object element, int occurance, ViewerFilter[] filters);

	/**
	 * Returns the concrete element from the tree input that correspond to the template element and
	 * wanted occurrence. This element can be further used to expand the tree viewer to it.
	 * 
	 * @param template
	 *            Template element.
	 * @param occurrence
	 *            Wanted occurrence.
	 * @param filters
	 *            Array of filters that each occurrence has to pass, so that it is included in the
	 *            count.
	 * @return Concrete element or null if the wanted occurrence is not reachable.
	 */
	Object getElement(Object template, int occurrence, ViewerFilter[] filters);

	/**
	 * Returns the textual representation of the stepping element.
	 * 
	 * @param element
	 *            Element to get the representation for.
	 * @return Textual representation.
	 */
	String getElementTextualRepresentation(Object element);

	/**
	 * Registers a new object that should be provided for stepping functionality.
	 * 
	 * @param element
	 *            Object to be added to the list.
	 */
	void addObjectToSteppingObjectList(Object element);

	/**
	 * 
	 * @return Returns if the sub-view should be loaded with stepping control visible or not.
	 */
	boolean initSteppingControlVisible();
}
