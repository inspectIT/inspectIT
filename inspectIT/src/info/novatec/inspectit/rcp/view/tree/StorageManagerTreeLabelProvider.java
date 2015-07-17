package info.novatec.inspectit.rcp.view.tree;

import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.provider.ILocalStorageDataProvider;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Styled cell label provider for the tree of storages.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageManagerTreeLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StyledString getStyledText(Object element, int index) {
		if (element instanceof IStorageDataProvider) {
			return TextFormatter.getStyledStorageDataString(((IStorageDataProvider) element).getStorageData(), ((IStorageDataProvider) element).getCmrRepositoryDefinition());
		} else if (element instanceof ILocalStorageDataProvider) {
			return TextFormatter.getStyledStorageDataString(((ILocalStorageDataProvider) element).getLocalStorageData());
		} else if (element instanceof Component) {
			return new StyledString(((Component) element).getName());
		}
		return super.getStyledText(element, index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Image getColumnImage(Object element, int index) {
		if (index == 0) {
			if (element instanceof Component) {
				return ((Component) element).getImage();
			}
		}
		return super.getColumnImage(element, index);
	}
}
