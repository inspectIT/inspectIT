package rocks.inspectit.ui.rcp.ci.view;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.model.ci.ApplicationLeaf;
import rocks.inspectit.ui.rcp.provider.IApplicationProvider;

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
