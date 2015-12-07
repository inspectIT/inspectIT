package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Label provider for the business transaction table view.
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * Unicode checkmark character.
	 */
	private static final String CHECK_MARK = "\u2713";

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
				return new StyledString(bTxDef.dynamicNameExtractionActive() ? CHECK_MARK : "");
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
				return InspectIT.getDefault()
						.getImage(bTxDef.getId() == BusinessTransactionDefinition.DEFAULT_ID ? InspectITImages.IMG_BUSINESS_TRANSACTION_GREY : InspectITImages.IMG_BUSINESS_TRANSACTION);
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
