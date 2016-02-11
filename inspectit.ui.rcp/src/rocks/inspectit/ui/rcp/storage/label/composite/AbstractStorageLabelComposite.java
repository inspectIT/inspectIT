package info.novatec.inspectit.rcp.storage.label.composite;

import info.novatec.inspectit.storage.label.AbstractStorageLabel;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

/**
 * Abstract class for all composite that are able to define a {@link AbstractStorageLabel}.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractStorageLabelComposite extends Composite {

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent.
	 * @param style
	 *            Style.
	 * @see Composite#Composite(Composite, int)
	 */
	public AbstractStorageLabelComposite(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Returns created {@link AbstractStorageLabel}.
	 * 
	 * @return Returns created {@link AbstractStorageLabel}.
	 */
	public abstract AbstractStorageLabel<?> getStorageLabel();

	/**
	 * Returns if the input is valid.
	 * 
	 * @return Returns if the input is valid.
	 */
	public abstract boolean isInputValid();

	/**
	 * Adds the listener that sub-classes should register in the correct way to the widgets, based
	 * on the widgets used.
	 * 
	 * @param pageCompletionListener
	 *            Listener to register.
	 */
	public abstract void addListener(Listener pageCompletionListener);

}
