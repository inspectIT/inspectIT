package info.novatec.inspectit.rcp.storage.label.composite.impl;

import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Composite for selecting the {@link StorageStorageLabel}.
 * 
 * @author Ivan Senic
 * 
 */
public class StringStorageLabelComposite extends AbstractStorageLabelComposite {

	/**
	 * Label type.
	 */
	private AbstractStorageLabelType<String> stringLabelType;

	/**
	 * Text box for entering string.
	 */
	private Text textBox;

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
	 * @param stringLabelType
	 *            Storage label type.
	 * @see Composite#Composite(Composite, int)
	 */
	public StringStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<String> stringLabelType) {
		this(parent, style, stringLabelType, true);
	}

	/**
	 * Secondary constructor. Defines if the label should be displayed in the composite.
	 * 
	 * @param parent
	 *            Parent.
	 * @param style
	 *            Style.
	 * @param stringLabelType
	 *            Storage label type.
	 * @param showLabel
	 *            Should label be displayed next to the selection widget.
	 * @see Composite#Composite(Composite, int)
	 */
	public StringStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<String> stringLabelType, boolean showLabel) {
		super(parent, style);
		this.stringLabelType = stringLabelType;
		this.showLabel = showLabel;
		initComposite();
	}

	/**
	 * Initializes the composite.
	 */
	private void initComposite() {
		if (showLabel) {
			GridLayout gl = new GridLayout(2, false);
			this.setLayout(gl);

			new Label(this, SWT.NONE).setText("Enter text:");
		} else {
			GridLayout gl = new GridLayout(1, false);
			this.setLayout(gl);
		}

		textBox = new Text(this, SWT.BORDER);
		textBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractStorageLabel<?> getStorageLabel() {
		return new StringStorageLabel(textBox.getText().trim(), stringLabelType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInputValid() {
		return !textBox.getText().trim().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(Listener pageCompletionListener) {
		textBox.addListener(SWT.Modify, pageCompletionListener);
	}

}
