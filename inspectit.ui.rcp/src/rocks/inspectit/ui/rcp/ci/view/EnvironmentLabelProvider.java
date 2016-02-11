package info.novatec.inspectit.rcp.ci.view;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.provider.IEnvironmentProvider;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

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
				if (CollectionUtils.isEmpty(environment.getProfileIds())) {
					return new StyledString(String.valueOf(0));
				} else {
					return new StyledString(String.valueOf(environment.getProfileIds().size()));
				}
			case 2:
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