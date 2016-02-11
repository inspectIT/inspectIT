package info.novatec.inspectit.rcp.editor.viewers;

import info.novatec.inspectit.rcp.editor.tooltip.IColumnToolTipProvider;

import java.util.Arrays;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.PendingUpdateAdapter;

/**
 * This class extends the {@link StyledCellLabelProvider} with support for the index of the cell,
 * used in the {@link org.eclipse.swt.widgets.Tree} or the {@link org.eclipse.swt.widgets.Table}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class StyledCellIndexLabelProvider extends StyledCellLabelProvider implements IColumnToolTipProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		// we don't care about the PendingUpdateAdapter which is used in some of
		// the trees and tables.
		if (element instanceof PendingUpdateAdapter) {
			cell.setText(element.toString());
		} else {
			int index = cell.getColumnIndex();
			StyledString styledString = getStyledText(element, index);
			String newText = styledString.toString();

			StyleRange[] oldStyleRanges = cell.getStyleRanges();
			StyleRange[] newStyleRanges = null;
			if (isOwnerDrawEnabled()) {
				newStyleRanges = styledString.getStyleRanges();
			}

			if (!Arrays.equals(oldStyleRanges, newStyleRanges)) {
				cell.setStyleRanges(newStyleRanges);
				if (cell.getText().equals(newText)) {
					// make sure there will be a refresh from a change
					cell.setText("");
				}
			}

			cell.setText(newText);
			cell.setImage(getColumnImage(element, index));
			cell.setFont(getFont(element, index));
			cell.setForeground(getForeground(element, index));
			cell.setBackground(getBackground(element, index));
		}
	}

	/**
	 * Default behavior is to return an empty instance of {@link StyledString}. Clients should
	 * override this method if needed.
	 * 
	 * @param element
	 *            The element for which to provide the styled label text
	 * @param index
	 *            The index of the element.
	 * @return The styled text string used to label the element
	 */
	protected StyledString getStyledText(Object element, int index) {
		return new StyledString(element.toString());
	}

	/**
	 * Default behavior is to return <code>null</code>. Clients should override this method if
	 * needed.
	 * 
	 * @param element
	 *            the element for which to provide the label image
	 * @param index
	 *            the index of the element.
	 * @return the image used to label the element, or <code>null</code> if there is no image for
	 *         the given object
	 */
	protected Image getColumnImage(Object element, int index) {
		return null;
	}

	/**
	 * Default behavior is to return <code>null</code>. Clients should override this method if
	 * needed.
	 * 
	 * @param element
	 *            the element
	 * @param index
	 *            the index of the element.
	 * @return the font for the element, or <code>null</code> to use the default font
	 */
	protected Font getFont(Object element, int index) {
		return null;
	}

	/**
	 * Default behavior is to return <code>null</code>. Clients should override this method if
	 * needed.
	 * 
	 * @param element
	 *            the element
	 * @param index
	 *            the index of the element.
	 * @return the foreground color for the element, or <code>null</code> to use the default
	 *         foreground color
	 */
	protected Color getForeground(Object element, int index) {
		return null;
	}

	/**
	 * Default behavior is to return <code>null</code>. Clients should override this method if
	 * needed.
	 * 
	 * @param element
	 *            the element
	 * @param index
	 *            the index of the element.
	 * @return the background color for the element, or <code>null</code> to use the default
	 *         background color
	 */
	protected Color getBackground(Object element, int index) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToolTipText(Object element, int index) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getToolTipImage(Object element, int index) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * It is needed to return not <code>null</code> value when using the
	 * {@link IColumnToolTipProvider}, so that the tips from {@link IColumnToolTipProvider} can be
	 * displayed.
	 */
	@Override
	public String getToolTipText(Object element) {
		return null;
	}

}
