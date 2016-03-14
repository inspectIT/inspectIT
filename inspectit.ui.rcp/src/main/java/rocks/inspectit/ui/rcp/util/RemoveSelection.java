package rocks.inspectit.ui.rcp.util;

import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Helper selection class to denote remove was executed.
 *
 * @author Ivan Senic
 *
 */
public class RemoveSelection extends StructuredSelection {

	/**
	 * Constructor.
	 *
	 * @param elements
	 *            removed elements
	 */
	public RemoveSelection(List<?> elements) {
		super(elements);
	}

}