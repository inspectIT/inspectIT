package info.novatec.inspectit.rcp.editor.tooltip;

import org.eclipse.swt.graphics.Image;

/**
 * Special interface for simple tooltip providing based on the table/tree column.
 * 
 * @author Ivan Senic
 * 
 */
public interface IColumnToolTipProvider {

	/**
	 * Returns tool-tip text.
	 * 
	 * @param element
	 *            Element to return the tool-tip for.
	 * @param index
	 *            Column index.
	 * @return Text or <code>null</code>.
	 */
	String getToolTipText(Object element, int index);

	/**
	 * Returns tool-tip image.
	 * 
	 * @param element
	 *            Element to return the tool-tip for.
	 * @param index
	 *            Column index.
	 * @return Image or <code>null</code>.
	 */
	Image getToolTipImage(Object element, int index);

}
