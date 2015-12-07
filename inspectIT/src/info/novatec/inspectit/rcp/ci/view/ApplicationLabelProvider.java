package info.novatec.inspectit.rcp.ci.view;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.ci.ApplicationLeaf;
import info.novatec.inspectit.rcp.provider.IApplicationProvider;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Label provider for {@link ApplicationLeaf} instances.
 *
 * @author Alexander Wert
 *
 */
public class ApplicationLabelProvider extends StyledCellIndexLabelProvider {
	/**
	 * Empty.
	 */
	private static final StyledString EMPTY = new StyledString();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StyledString getStyledText(Object element, int index) {
		if (element instanceof IApplicationProvider) {
			ApplicationDefinition appDef = ((IApplicationProvider) element).getApplication();
			switch (index) {
			case 0:
				return new StyledString(appDef.getApplicationName());
			case 1:
				return new StyledString(String.valueOf(appDef.getBusinessTransactionDefinitions().size()));
			case 2:
				return new StyledString((appDef.getDescription() != null) ? TextFormatter.clearLineBreaks(appDef.getDescription()) : "");
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
		if (element instanceof IApplicationProvider) {
			ApplicationDefinition appDef = ((IApplicationProvider) element).getApplication();
			switch (index) {
			case 0:
				if (appDef.getId() == ApplicationDefinition.DEFAULT_ID) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_APPLICATION_GREY);
				} else {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_APPLICATION);
				}

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
		if (element instanceof IApplicationProvider) {
			ApplicationDefinition appDefinition = ((IApplicationProvider) element).getApplication();
			if (appDefinition.getId() == ApplicationDefinition.DEFAULT_ID) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
			}
		}
		return super.getForeground(element, index);
	}
}
