package info.novatec.inspectit.rcp.details;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

/**
 * This {@link DetailsCellContent} sub-class displays Yes/No image with tool-tip based the boolean
 * value.
 * 
 * @author Ivan Senic
 * 
 */
public class YesNoDetailsCellContent extends DetailsCellContent {

	/**
	 * Default constructor.
	 * 
	 * @param value
	 *            Boolean value to display.
	 */
	public YesNoDetailsCellContent(boolean value) {
		if (value) {
			setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK));
			setImageToolTip("Yes");
		} else {
			setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CLOSE));
			setImageToolTip("No");
		}

	}
}
