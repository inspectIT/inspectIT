package info.novatec.inspectit.rcp.editor.tooltip;

import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

/**
 * Small extension of the {@link ColumnAwareToolTipSupport}. This one will check if the label
 * provider of the cell is {@link IColumnToolTipProvider} and if so if will pull the tip and image
 * from that interface and not directly from the label provider.
 * 
 * @author Ivan Senic
 * 
 */
public class ColumnAwareToolTipSupport extends ColumnViewerToolTipSupport {

	/**
	 * Viewer cell key. Copied from super class.
	 */
	private static final String VIEWER_CELL_KEY = Policy.JFACE + "_VIEWER_CELL_KEY";

	/**
	 * Shift X. Copied from super class.
	 */
	private static final int DEFAULT_SHIFT_X = 10;

	/**
	 * Shift Y. Copied from super class.
	 */
	private static final int DEFAULT_SHIFT_Y = 0;

	/**
	 * {@link ColumnViewer}.
	 */
	private ColumnViewer viewer;

	/**
	 * @param viewer
	 *            the viewer the support is attached to
	 * @param style
	 *            style passed to control tool tip behavior
	 * 
	 * @param manualActivation
	 *            <code>true</code> if the activation is done manually using {@link #show(Point)}
	 * @See {@link ColumnAwareToolTipSupport#ColumnAwareToolTipSupport(ColumnViewer, int, boolean)}
	 */
	public ColumnAwareToolTipSupport(ColumnViewer viewer, int style, boolean manualActivation) {
		super(viewer, style, manualActivation);
		this.viewer = viewer;
	}

	/**
	 * Enable ToolTip support for the viewer by creating an instance from this class. To get all
	 * necessary informations this support class consults the {@link CellLabelProvider}.
	 * 
	 * @param viewer
	 *            the viewer the support is attached to
	 */
	public static void enableFor(ColumnViewer viewer) {
		new ColumnAwareToolTipSupport(viewer, ToolTip.NO_RECREATE, false);
	}

	/**
	 * Enable ToolTip support for the viewer by creating an instance from this class. To get all
	 * necessary informations this support class consults the {@link CellLabelProvider}.
	 * 
	 * @param viewer
	 *            the viewer the support is attached to
	 * @param style
	 *            style passed to control tool tip behavior
	 * 
	 * @see ToolTip#RECREATE
	 * @see ToolTip#NO_RECREATE
	 */
	public static void enableFor(ColumnViewer viewer, int style) {
		new ColumnAwareToolTipSupport(viewer, style, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean shouldCreateToolTip(Event event) {
		Point point = new Point(event.x, event.y);
		ViewerCell cell = viewer.getCell(point);
		if (cell == null) {
			return false;
		}
		CellLabelProvider labelProvider = viewer.getLabelProvider(cell.getColumnIndex());
		if (labelProvider instanceof IColumnToolTipProvider) {
			IColumnToolTipProvider columnToolTipProvider = (IColumnToolTipProvider) labelProvider;
			Object element = cell.getViewerRow().getItem().getData();

			String text = columnToolTipProvider.getToolTipText(element, cell.getColumnIndex());
			Image image = columnToolTipProvider.getToolTipImage(element, cell.getColumnIndex());

			if (null == text && null == image) {
				return false;
			}

			viewer.getControl().setToolTipText("");

			setPopupDelay(labelProvider.getToolTipDisplayDelayTime(element));
			setHideDelay(labelProvider.getToolTipTimeDisplayed(element));

			Point shift = labelProvider.getToolTipShift(element);

			if (shift == null) {
				setShift(new Point(DEFAULT_SHIFT_X, DEFAULT_SHIFT_Y));
			} else {
				setShift(new Point(shift.x, shift.y));
			}

			setData(VIEWER_CELL_KEY, cell);

			setText(text);
			setImage(image);
			setStyle(labelProvider.getToolTipStyle(element));
			setForegroundColor(labelProvider.getToolTipForegroundColor(element));
			setBackgroundColor(labelProvider.getToolTipBackgroundColor(element));
			setFont(labelProvider.getToolTipFont(element));

			return true;
		} else {
			return super.shouldCreateToolTip(event);
		}
	}
}
