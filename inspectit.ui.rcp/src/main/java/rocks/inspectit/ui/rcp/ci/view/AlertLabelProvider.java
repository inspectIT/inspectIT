package rocks.inspectit.ui.rcp.ci.view;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.provider.IAlertDefinitionProvider;

/**
 * Label provider for the {@link AlertManagerViewPart}.
 *
 * @author Alexander Wert
 *
 */
public class AlertLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StyledString getStyledText(Object element, int index) {
		if (element instanceof IAlertDefinitionProvider) {
			AlertingDefinition alertDef = ((IAlertDefinitionProvider) element).getAlertDefinition();
			switch (index) {
			case 0:
				return new StyledString(alertDef.getName());
			case 1:
				StyledString styledString = new StyledString();

				styledString.append(new StyledString(alertDef.getMeasurement() + "(" + alertDef.getField() + ")"));
				StringBuilder tagsString = new StringBuilder();
				for (Entry<String, String> tagKeyValue : alertDef.getTags().entrySet()) {
					tagsString.append(", ");
					tagsString.append(tagKeyValue.getKey());
					tagsString.append('=');
					tagsString.append(tagKeyValue.getValue());
				}
				styledString.append(new StyledString(tagsString.toString(), StyledString.QUALIFIER_STYLER));
				return styledString;
			case 2:
				return new StyledString(String.valueOf(alertDef.getThreshold()));
			default:
				return new StyledString();
			}
		}

		return new StyledString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Image getColumnImage(Object element, int index) {
		if (element instanceof IAlertDefinitionProvider) {
			switch (index) {
			case 0:
				return ImageFormatter.getAlertImage();
			default:
				return super.getColumnImage(element, index);
			}
		}
		return super.getColumnImage(element, index);
	}
}
