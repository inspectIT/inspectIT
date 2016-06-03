package rocks.inspectit.ui.rcp.ci.view;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.provider.IEnvironmentProvider;

/**
 * Environment label provider.
 *
 * @author Ivan Senic
 *
 */
class EnvironmentLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * Empty.
	 */
	private static final StyledString EMPTY = new StyledString();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StyledString getStyledText(Object element, int index) {
		if (element instanceof IEnvironmentProvider) {
			Environment environment = ((IEnvironmentProvider) element).getEnvironment();
			switch (index) {
			case 0:
				return new StyledString(environment.getName());
			case 1:
				if (null != environment.getUpdatedDate()) {
				return new StyledString(NumberFormatter.formatTime(environment.getUpdatedDate()));
			} else {
				return new StyledString(NumberFormatter.formatTime(environment.getCreatedDate()));
			}
			case 2:
				if (CollectionUtils.isEmpty(environment.getProfileIds())) {
					return new StyledString(String.valueOf(0));
				} else {
					return new StyledString(String.valueOf(environment.getProfileIds().size()));
				}
			case 3:
				return TextFormatter.emptyStyledStringIfNull(TextFormatter.clearLineBreaks(environment.getDescription()));
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
		if (element instanceof IEnvironmentProvider) {
			switch (index) {
			case 0:
				return ImageFormatter.getEnvironmentImage(((IEnvironmentProvider) element).getEnvironment());
			default:
				return super.getColumnImage(element, index);
			}
		}
		return super.getColumnImage(element, index);
	}
}