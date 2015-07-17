package info.novatec.inspectit.rcp.storage.label.composite.impl;

import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.NumberStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Composite for selecting the {@link NumberStorageLabel}.
 * 
 * @author Ivan Senic
 * 
 */
public class NumberStorageLabelComposite extends AbstractStorageLabelComposite {

	/**
	 * Label type.
	 */
	private AbstractStorageLabelType<Number> numberLabelType;

	/**
	 * Text box for entering number.
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
	 * @param numberLabelType
	 *            Storage label type.
	 * @see Composite#Composite(Composite, int)
	 */
	public NumberStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<Number> numberLabelType) {
		this(parent, style, numberLabelType, true);
	}

	/**
	 * Secondary constructor. Defines if the label should be displayed in the composite.
	 * 
	 * @param parent
	 *            Parent.
	 * @param style
	 *            Style.
	 * @param numberLabelType
	 *            Storage label type.
	 * @param showLabel
	 *            Should label be displayed next to the selection widget.
	 * @see Composite#Composite(Composite, int)
	 */
	public NumberStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<Number> numberLabelType, boolean showLabel) {
		super(parent, style);
		this.numberLabelType = numberLabelType;
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

			new Label(this, SWT.NONE).setText("Enter number:");
		} else {
			GridLayout gl = new GridLayout(1, false);
			this.setLayout(gl);
		}
		textBox = new Text(this, SWT.BORDER | SWT.RIGHT);
		textBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractStorageLabel<?> getStorageLabel() {
		String text = textBox.getText().trim();
		if (text.indexOf('.') != -1) {
			return new NumberStorageLabel(Double.parseDouble(text), numberLabelType);
		} else {
			return new NumberStorageLabel(Integer.parseInt(text), numberLabelType);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInputValid() {
		String text = textBox.getText().trim();
		if (text.isEmpty()) {
			return false;
		}
		if (text.indexOf('.') != -1) {
			try {
				Double.parseDouble(text);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			try {
				Integer.parseInt(text);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(Listener pageCompletionListener) {
		textBox.addListener(SWT.Modify, pageCompletionListener);
	}

}
