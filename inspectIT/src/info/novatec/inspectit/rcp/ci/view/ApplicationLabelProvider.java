package info.novatec.inspectit.rcp.ci.view;

import info.novatec.inspectit.cmr.configuration.business.ApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.model.ci.ApplicationLeaf;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

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
		if (element instanceof ApplicationLeaf) {
			IApplicationDefinition appDef = ((ApplicationLeaf) element).getApplication();
			String content = "";
			switch (index) {
			case 0:
				content = appDef.getApplicationName();
				break;
			case 1:
				content = String.valueOf(appDef.getBusinessTransactionDefinitions().size());
				break;
			case 2:
				content = (appDef.getDescription() != null) ? appDef.getDescription() : "";
				break;
			default:
				return EMPTY;
			}
			if (appDef.getId() == ApplicationDefinition.DEFAULT_ID) {
				return new StyledString(content, StyledString.QUALIFIER_STYLER);
			} else {
				return new StyledString(content);
			}

		}

		return EMPTY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Image getColumnImage(Object element, int index) {
		if (element instanceof ApplicationLeaf) {
			IApplicationDefinition appDef = ((ApplicationLeaf) element).getApplication();
			switch (index) {
			case 0:
				if (appDef.getId() == ApplicationDefinition.DEFAULT_ID) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_PUZZLE_GREY);
				} else {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_PUZZLE);
				}

			default:
				return super.getColumnImage(element, index);
			}
		}
		return super.getColumnImage(element, index);
	}
}
