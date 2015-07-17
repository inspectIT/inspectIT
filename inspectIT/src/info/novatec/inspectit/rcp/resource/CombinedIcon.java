package info.novatec.inspectit.rcp.resource;

import java.util.Arrays;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * Combined icon image descriptor capable of combining several images in one on the vertical or
 * horizontal level.
 * 
 * @author Ivan Senic
 * 
 */
public class CombinedIcon extends CompositeImageDescriptor {

	/**
	 * {@link ImageDescriptor}s that will be included in the combined icon.
	 */
	private ImageDescriptor[] descriptors;

	/**
	 * Size of the combined image.
	 */
	private Point size;

	/**
	 * Is vertical combination.
	 */
	private boolean isVertical;

	/**
	 * Default constructor.
	 * 
	 * @param descriptors
	 *            {@link ImageDescriptor}s that will be included in the combined icon.
	 * @param orientation
	 *            {@link SWT#VERTICAL} or {@link SWT#HORIZONTAL}. Way the images will be pasted.
	 */
	public CombinedIcon(ImageDescriptor[] descriptors, int orientation) {
		if (null == descriptors) {
			throw new IllegalArgumentException("Image descriptor array for combined icon must not be null");
		}
		if (descriptors.length == 0) {
			throw new IllegalArgumentException("Amount of given image descriptors for combined icon must be at least 1.");
		}
		this.descriptors = Arrays.copyOf(descriptors, descriptors.length);
		this.isVertical = orientation == SWT.VERTICAL;
		int width = 0;
		int height = 0;
		for (ImageDescriptor imageDescriptor : descriptors) {
			ImageData imageData = imageDescriptor.getImageData();
			if (isVertical) {
				width = Math.max(width, imageData.width);
				height += imageData.height;
			} else {
				width += imageData.width;
				height = Math.max(height, imageData.height);
			}
		}
		this.size = new Point(width, height);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		int xOffset = 0;
		int yOffset = 0;

		for (ImageDescriptor imageDescriptor : descriptors) {
			ImageData data = imageDescriptor.getImageData();
			if (isVertical) {
				drawImage(data, 0, yOffset);
				yOffset += data.height;
			} else {
				drawImage(data, xOffset, 0);
				xOffset += data.width;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Point getSize() {
		return size;
	}

}
