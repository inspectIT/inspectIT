package info.novatec.inspectit.rcp.editor.viewers;

import info.novatec.inspectit.rcp.InspectIT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import ch.qos.logback.core.status.Status;

/**
 * Extension of the {@link StyledCellLabelProvider} that fixes the Eclipse bug with the image sizes
 * in the columns must be same width. Does this by custom painting. Note is not applicable for
 * viewers using PendingAdapterUpdates.
 * 
 * @author Ivan Senic
 * 
 */
public class ImageFixStyledCellIndexLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * Margin to display left and right to image.
	 */
	private static final int IMAGE_MARGIN = 1;

	/**
	 * We are using reflection to have possibility of calling the private method in the superclass.
	 */
	private static Method getSharedTextLayoutMethod;

	static {
		try {
			getSharedTextLayoutMethod = StyledCellLabelProvider.class.getDeclaredMethod("getSharedTextLayout", new Class<?>[] { Display.class });
			getSharedTextLayoutMethod.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			InspectIT.getDefault().log(Status.WARN, "Can no load the getSharedTextLayout() method on the Display class.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(ViewerCell cell) {
		super.update(cell);

		// we set image to null cause we will draw it on our own
		cell.setImage(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paint(Event event, Object element) {
		// first get cell bounds
		Rectangle cellBounds = getViewerCellBounds(event);
		if (cellBounds == null) {
			return;
		}

		// check if we have image for the element on the index
		Image image = getColumnImage(element, event.index);
		Rectangle imageBounds = null;

		// if we have ident the text for the image width plus the left + right margin
		if (null != image) {
			imageBounds = image.getBounds();
			TextLayout textLayout = getSharedTextLayout(event.display);
			textLayout.setIndent(imageBounds.width + IMAGE_MARGIN + IMAGE_MARGIN);
		}

		// then execute super paint
		super.paint(event, element);

		// additionally paint image ourself if we have it
		if (imageBounds != null) {
			// add margin to x
			int x = cellBounds.x + IMAGE_MARGIN;
			// center horizontally
			int y = cellBounds.y + Math.max(0, (cellBounds.height - imageBounds.height) / 2);
			// draw
			event.gc.drawImage(image, x, y);
		}

	}

	/**
	 * @param event
	 *            Event
	 * @return Returns the {@link Rectangle} describing the bounds of a cell for an event.
	 */
	private Rectangle getViewerCellBounds(Event event) {
		if (event.item instanceof TableItem) {
			return ((TableItem) event.item).getBounds(event.index);
		} else if (event.item instanceof TreeItem) {
			return ((TreeItem) event.item).getBounds(event.index);
		}
		throw new RuntimeException("ImageFixStyledCellIndexLabelProvider can only run with tree or table");
	}

	/**
	 * Using reflection to call {@link #getSharedTextLayout(Display)} in the super class.
	 * 
	 * @param display
	 *            {@link Display}
	 * @return {@link TextLayout} given by mentioned method or <code>null</code> if not available.
	 */
	private TextLayout getSharedTextLayout(Display display) {
		if (null == getSharedTextLayoutMethod) {
			return null;
		}

		try {
			return (TextLayout) getSharedTextLayoutMethod.invoke(this, display);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}
}
