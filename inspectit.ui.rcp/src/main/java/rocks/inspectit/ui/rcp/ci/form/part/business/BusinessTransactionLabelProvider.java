package rocks.inspectit.ui.rcp.ci.form.part.business;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;

/**
 * Label provider for the business transaction table view.
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * Empty.
	 */
	private static final StyledString EMPTY = new StyledString();

	/**
	 * The resource manager is used for the images etc.
	 */
	private final LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StyledString getStyledText(Object element, int index) {
		if (element instanceof BusinessTransactionDefinition) {
			BusinessTransactionDefinition bTxDef = (BusinessTransactionDefinition) element;
			switch (index) {
			case 0:
				return new StyledString(bTxDef.getBusinessTransactionDefinitionName());
			case 1:
				if (bTxDef.dynamicNameExtractionActive()) {
					StyledString result = new StyledString("Pattern: ");
					result.append(new StyledString(bTxDef.getNameExtractionExpression().getTargetNamePattern(), StyledString.QUALIFIER_STYLER));
					return result;
				} else {
					return new StyledString(bTxDef.getBusinessTransactionDefinitionName());
				}
			case 2:
				return EMPTY;
			case 3:
				return new StyledString((bTxDef.getDescription() != null) ? bTxDef.getDescription() : "");
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
		if (element instanceof BusinessTransactionDefinition) {
			BusinessTransactionDefinition bTxDef = (BusinessTransactionDefinition) element;
			switch (index) {
			case 0:
				return ImageFormatter.getBusinessTransactionDefinitionImage(bTxDef);
			case 2:
				return bTxDef.dynamicNameExtractionActive() ? InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK) : super.getColumnImage(element, index);
			default:
			}
		}
		return super.getColumnImage(element, index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		resourceManager.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Color getForeground(Object element, int index) {
		if (element instanceof BusinessTransactionDefinition) {
			BusinessTransactionDefinition bTxDef = (BusinessTransactionDefinition) element;
			if (bTxDef.getId() == BusinessTransactionDefinition.DEFAULT_ID) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
			}
		}
		return super.getForeground(element, index);
	}
}
