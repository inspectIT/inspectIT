package rocks.inspectit.ui.rcp.ci.view;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.provider.IProfileProvider;

/**
 * Profile label provider.
 *
 * @author Ivan Senic
 *
 */
class ProfileLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * Empty.
	 */
	private static final StyledString EMPTY = new StyledString();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StyledString getStyledText(Object element, int index) {
		if (element instanceof IProfileProvider) {
			Profile profile = ((IProfileProvider) element).getProfile();
			switch (index) {
			case 0:
				return new StyledString(profile.getName());
			case 1:
				if (null != profile.getUpdatedDate()) {
					return new StyledString(NumberFormatter.formatTime(profile.getUpdatedDate()));
				} else {
					return new StyledString(NumberFormatter.formatTime(profile.getCreatedDate()));
				}
			case 5:
				return TextFormatter.emptyStyledStringIfNull(TextFormatter.clearLineBreaks(profile.getDescription()));
			default:
				return EMPTY;
			}
		}
		return EMPTY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Image getColumnImage(Object element, int index) {
		if (element instanceof IProfileProvider) {
			Profile profile = ((IProfileProvider) element).getProfile();
			switch (index) {
			case 0:
				return ImageFormatter.getProfileImage(profile);
			case 2:
				return profile.isActive() ? InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK) : null; // NOPMD
			case 3:
				return profile.isDefaultProfile() ? InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK) : null; // NOPMD
			case 4:
				return ImageFormatter.getProfileDataImage(profile.getProfileData());
			default:
				return super.getColumnImage(element, index);
			}
		}
		return super.getColumnImage(element, index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Color getForeground(Object element, int index) {
		if (element instanceof IProfileProvider) {
			Profile profile = ((IProfileProvider) element).getProfile();
			if (profile.isCommonProfile()) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
			}
		}
		return super.getForeground(element, index);
	}

}