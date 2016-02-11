package info.novatec.inspectit.rcp.editor.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Special {@link StyledCellIndexLabelProvider} that can be used when checked style in table
 * sub-view is active. This provider delegates to original provide with the decreased index.
 * 
 * @author Ivan Senic
 * 
 */
public class CheckedDelegatingIndexLabelProvider extends StyledCellIndexLabelProvider {

	/**
	 * Empty.
	 */
	private static final StyledString EMPTY_STYLED_STRING = new StyledString("");

	/**
	 * Delegated {@link StyledCellIndexLabelProvider}.
	 */
	private StyledCellIndexLabelProvider delegate;

	/**
	 * @param delegate
	 *            Delegated {@link StyledCellIndexLabelProvider}.
	 */
	public CheckedDelegatingIndexLabelProvider(StyledCellIndexLabelProvider delegate) {
		Assert.isNotNull(delegate);

		this.delegate = delegate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StyledString getStyledText(Object element, int index) {
		if (0 == index) {
			return EMPTY_STYLED_STRING;
		} else {
			return delegate.getStyledText(element, index - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Color getBackground(Object element, int index) {
		if (0 == index) {
			return null;
		} else {
			return delegate.getBackground(element, index - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Image getColumnImage(Object element, int index) {
		if (0 == index) {
			return null;
		} else {
			return delegate.getColumnImage(element, index - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Font getFont(Object element, int index) {
		if (0 == index) {
			return null;
		} else {
			return delegate.getFont(element, index - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Color getForeground(Object element, int index) {
		if (0 == index) {
			return null;
		} else {
			return delegate.getForeground(element, index - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToolTipText(Object element) {
		return delegate.getToolTipText(element);
	}

}
