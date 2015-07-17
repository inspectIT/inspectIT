package info.novatec.inspectit.rcp.storage.label.composite.impl;

import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Composite for selecting the {@link BooleanStorageLabel}.
 * 
 * @author Ivan Senic
 * 
 */
public class BooleanStorageLabelComposite extends AbstractStorageLabelComposite {

	/**
	 * Label type.
	 */
	private AbstractStorageLabelType<Boolean> booleanStorageLabelType;

	/**
	 * Button for selecting <code>YES</code>.
	 */
	private Button yesButton;

	/**
	 * Should a label be displayed next to the selection widget.
	 */
	private boolean showLabel;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent.
	 * @param style
	 *            Style.
	 * @param booleanStorageLabelType
	 *            Storage label type.
	 * @see Composite#Composite(Composite, int)
	 */
	public BooleanStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<Boolean> booleanStorageLabelType) {
		this(parent, style, booleanStorageLabelType, true);
	}

	/**
	 * Secondary constructor. Defines if the label should be displayed in the composite.
	 * 
	 * @param parent
	 *            Parent.
	 * @param style
	 *            Style.
	 * @param booleanStorageLabelType
	 *            Storage label type.
	 * @param showLabel
	 *            Should label be displayed next to the selection widget.
	 * @see Composite#Composite(Composite, int)
	 */
	public BooleanStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<Boolean> booleanStorageLabelType, boolean showLabel) {
		super(parent, style);
		this.booleanStorageLabelType = booleanStorageLabelType;
		this.showLabel = showLabel;
		initComposite();
	}

	/**
	 * Initializes the composite.
	 */
	private void initComposite() {
		if (showLabel) {
			GridLayout gl = new GridLayout(3, true);
			this.setLayout(gl);
			new Label(this, SWT.NONE).setText("Select value:");
		} else {
			GridLayout gl = new GridLayout(2, true);
			this.setLayout(gl);
		}

		yesButton = new Button(this, SWT.RADIO);
		yesButton.setText("Yes");
		yesButton.setSelection(true);

		Button noButton = new Button(this, SWT.RADIO);
		noButton.setText("No");
		noButton.setSelection(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractStorageLabel<?> getStorageLabel() {
		return new BooleanStorageLabel(yesButton.getSelection(), booleanStorageLabelType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInputValid() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(Listener pageCompletionListener) {
		yesButton.addListener(SWT.Selection, pageCompletionListener);
	}
}
