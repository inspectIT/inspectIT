package rocks.inspectit.ui.rcp.property.control.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import rocks.inspectit.shared.cs.cmr.property.configuration.impl.StringProperty;
import rocks.inspectit.ui.rcp.property.IPropertyUpdateListener;
import rocks.inspectit.ui.rcp.property.control.AbstractPropertyControl;

/**
 * {@link AbstractPropertyControl} for the string property.
 *
 * @author Ivan Senic
 *
 */
public class StringPropertyControl extends AbstractPropertyControl<StringProperty, String> {

	/**
	 * Text to display string value.
	 */
	private Text text;

	/**
	 * Default constructor.
	 *
	 * @param property
	 *            Property.
	 * @param propertyUpdateListener
	 *            Property update listener to report updates to.
	 */
	public StringPropertyControl(StringProperty property, IPropertyUpdateListener propertyUpdateListener) {
		super(property, propertyUpdateListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createControl(Composite parent) {
		int style = SWT.BORDER;
		if (property.isPassword()) {
			style |= SWT.PASSWORD;
		}

		text = new Text(parent, style);
		text.setText(property.getValue());
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				sendPropertyUpdateEvent(text.getText());
			}
		});

		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String value = text.getText();
				if (value.isEmpty() && (null == StringPropertyControl.super.propertyUpdate)) {
					text.setText(getLastCorrectValue());
				}
			}
		});

		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void showDefaultValue() {
		text.setText(property.getDefaultValue());
	}

}
