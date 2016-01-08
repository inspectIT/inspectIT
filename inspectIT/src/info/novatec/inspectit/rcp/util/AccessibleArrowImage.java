package info.novatec.inspectit.rcp.util;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;

/**
 * An arrow image descriptor. The images color is related to the list fore- and background color.
 * This makes the arrow visible even in high contrast mode. If <code>ltr</code> is true the arrow
 * points to the right, otherwise it points to the left.
 * <p>
 * <b>IMPORTANT:</b> The class is licensed under the Eclipse Public License v1.0 as it contains the
 * code from the {@link org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.BreadcrumbItem} class
 * belonging to the Eclipse Rich Client Platform. EPL v1.0 license can be found
 * <a href="https://www.eclipse.org/legal/epl-v10.html">here</a>.
 * <p>
 * Please relate to the LICENSEEXCEPTIONS.txt file for more information about license exceptions
 * that apply regarding to InspectIT and Eclipse RCP and/or EPL Components.
 */
public class AccessibleArrowImage extends CompositeImageDescriptor {

	/**
	 * Arrow size.
	 */
	private static final int ARROW_SIZE = 5;

	/**
	 * Left to right arrow boolean.
	 */
	private final boolean fLTR;

	/**
	 * Default constructor.
	 * 
	 * @param ltr
	 *            Left to right arrow.
	 */
	public AccessibleArrowImage(boolean ltr) {
		fLTR = ltr;
	}

	/**
	 * Draw the composite images.
	 * <p>
	 * Subclasses must implement this framework method to paint images within the given bounds using
	 * one or more calls to the <code>drawImage</code> framework method.
	 * </p>
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	protected void drawCompositeImage(int width, int height) {
		Display display = Display.getDefault();

		Image image = new Image(display, ARROW_SIZE, ARROW_SIZE * 2);

		GC gc = new GC(image);

		Color triangle = createColor(SWT.COLOR_LIST_FOREGROUND, SWT.COLOR_LIST_BACKGROUND, 20, display);
		Color aliasing = createColor(SWT.COLOR_LIST_FOREGROUND, SWT.COLOR_LIST_BACKGROUND, 30, display);
		gc.setBackground(triangle);

		if (fLTR) {
			gc.fillPolygon(new int[] { mirror(0), 0, mirror(ARROW_SIZE), ARROW_SIZE, mirror(0), ARROW_SIZE * 2 });
		} else {
			gc.fillPolygon(new int[] { ARROW_SIZE, 0, 0, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE * 2 });
		}

		gc.setForeground(aliasing);
		gc.drawLine(mirror(0), 1, mirror(ARROW_SIZE - 1), ARROW_SIZE);
		gc.drawLine(mirror(ARROW_SIZE - 1), ARROW_SIZE, mirror(0), ARROW_SIZE * 2 - 1);

		gc.dispose();
		triangle.dispose();
		aliasing.dispose();

		ImageData imageData = image.getImageData();
		for (int y = 1; y < ARROW_SIZE; y++) {
			for (int x = 0; x < y; x++) {
				imageData.setAlpha(mirror(x), y, 255);
			}
		}
		for (int y = 0; y < ARROW_SIZE; y++) {
			for (int x = 0; x <= y; x++) {
				imageData.setAlpha(mirror(x), ARROW_SIZE * 2 - y - 1, 255);
			}
		}

		int offset = 0;
		if (!fLTR) {
			offset = -1;
		}
		drawImage(imageData, width / 2 - ARROW_SIZE / 2 + offset, height / 2 - ARROW_SIZE - 1);

		image.dispose();
	}

	/**
	 * Returns correct number of pixels depending on the arrow orientation. If arrow is set to be
	 * from left to right original parameter values i returned. if not then the mirrored value is
	 * returned.
	 * 
	 * @param x
	 *            Pixels.
	 * @return Returns correct number of pixels depending on the arrow orientation. If arrow is set
	 *         to be from left to right original parameter values i returned. if not then the
	 *         mirrored value is returned.
	 */
	private int mirror(int x) {
		if (fLTR) {
			return x;
		}

		return ARROW_SIZE - x - 1;
	}

	/**
	 * Return the size of this composite image.
	 * <p>
	 * Subclasses must implement this framework method.
	 * </p>
	 * 
	 * @return the x and y size of the image expressed as a point object
	 */
	protected Point getSize() {
		return new Point(10, 16);
	}

	/**
	 * Blends two colors with the given ration. The colors are represented by int values as colors
	 * as defined in the {@link SWT} class.
	 * 
	 * @param color1
	 *            First color.
	 * @param color2
	 *            Second color.
	 * @param ratio
	 *            Percentage of the first color in the blend (0-100).
	 * @param display
	 *            {@link Display}
	 * @return New color.
	 * @see FormColors#blend(RGB, RGB, int)
	 */
	private Color createColor(int color1, int color2, int ratio, Display display) {
		RGB rgb1 = display.getSystemColor(color1).getRGB();
		RGB rgb2 = display.getSystemColor(color2).getRGB();

		RGB blend = FormColors.blend(rgb2, rgb1, ratio);

		return new Color(display, blend);
	}
}