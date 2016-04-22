package rocks.inspectit.ui.rcp.formatter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * The class provide different methods for color manipulation.
 *
 * @author Ivan Senic
 *
 */
public final class ColorFormatter {

	/**
	 * Private constructor.
	 */
	private ColorFormatter() {
	}

	/**
	 * Creates the color that is between two supplied color descriptors in linear gradient.
	 *
	 * @param rgb1
	 *            {@link RGB} descriptor for first (starting) color.
	 * @param rgb2
	 *            {@link RGB} descriptor for second (ending) color.
	 * @param ratio
	 *            Ratio should be number from 0 to 1 (including). The numbers closer to 0 will
	 *            favorite the first (starting) color, while the numbers closer to 1 will favorite
	 *            the second (ending) color.
	 * @param resourceManager
	 *            {@link ResourceManager} that color will be created with. Note that is
	 *            responsibility of caller to handle the disposal of the resource manager.
	 * @return {@link Color}
	 */
	public static Color getLinearGradientColor(RGB rgb1, RGB rgb2, double ratio, ResourceManager resourceManager) {
		Assert.isTrue((ratio >= 0) && (ratio <= 1), "Ratio for linear gradient must me between 0 and 1 (including).");
		int red = (int) ((rgb2.red * ratio) + (rgb1.red * (1 - ratio)));
		int green = (int) ((rgb2.green * ratio) + (rgb1.green * (1 - ratio)));
		int blue = (int) ((rgb2.blue * ratio) + (rgb1.blue * (1 - ratio)));
		RGB newRgb = new RGB(red, green, blue);
		return resourceManager.createColor(newRgb);
	}

	/**
	 * Returns the so-called performance color. This color is actually a color between good -
	 * average - bad color descriptors based on the values supplied. For example, if the actual
	 * value is close to the good value, good color will be returned. Color is created based on a
	 * linear gradient between two colors. It is possible that actual value is "better" than good
	 * value, and in this case always the good color is returned. It is irrelevant if the good value
	 * is higher or smaller that bad value, but it is important that they are not the same
	 * (exception will be thrown in that case).
	 *
	 * @param goodRgb
	 *            {@link RGB} that defines a color that should be returned if performance is good.
	 * @param avgRgb
	 *            {@link RGB} that defines a color that should be returned if performance is
	 *            average.
	 * @param badRgb
	 *            {@link RGB} that defines a color that should be returned if performance is bad.
	 * @param actualValue
	 *            Actual value.
	 * @param goodValue
	 *            Good value.
	 * @param badValue
	 *            Bad value.
	 * @param resourceManager
	 *            {@link ResourceManager} that color will be created with. Note that is
	 *            responsibility of caller to handle the disposal of the resource manager.
	 * @return {@link Color}.
	 */
	public static Color getPerformanceColor(RGB goodRgb, RGB avgRgb, RGB badRgb, double actualValue, double goodValue, double badValue, ResourceManager resourceManager) {
		Assert.isTrue(goodValue != badValue);
		double avg = (goodValue + badValue) / 2;
		if (goodValue > badValue) {
			if (actualValue > goodValue) {
				return resourceManager.createColor(goodRgb);
			} else if (actualValue < badValue) {
				return resourceManager.createColor(badRgb);
			} else if (actualValue > avg) {
				// return combination of green and yellow
				double factor = Math.abs((actualValue - avg) / (goodValue - avg));
				return getLinearGradientColor(avgRgb, goodRgb, factor, resourceManager);
			} else {
				// return combination of red and yellow
				double factor = Math.abs((actualValue - badValue) / (avg - badValue));
				return getLinearGradientColor(badRgb, avgRgb, factor, resourceManager);
			}
		} else if (goodValue < badValue) {
			if (actualValue < goodValue) {
				return resourceManager.createColor(goodRgb);
			} else if (actualValue > badValue) {
				return resourceManager.createColor(badRgb);
			} else if (actualValue < avg) {
				// return combination of green and yellow
				double factor = 1 - Math.abs((actualValue - goodValue) / (avg - goodValue));
				return ColorFormatter.getLinearGradientColor(avgRgb, goodRgb, factor, resourceManager);
			} else {
				// return combination of red and yellow
				double factor = 1 - Math.abs((actualValue - avg) / (badValue - avg));
				return ColorFormatter.getLinearGradientColor(badRgb, avgRgb, factor, resourceManager);
			}
		} else {
			throw new RuntimeException("Performance color can not be created due to the bad input values.");
		}
	}
}
