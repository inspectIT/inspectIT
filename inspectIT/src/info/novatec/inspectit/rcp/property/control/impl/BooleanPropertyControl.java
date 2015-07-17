package info.novatec.inspectit.rcp.property.control.impl;

import info.novatec.inspectit.cmr.property.configuration.impl.BooleanProperty;
import info.novatec.inspectit.rcp.property.IPropertyUpdateListener;
import info.novatec.inspectit.rcp.property.control.AbstractPropertyControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * {@link AbstractPropertyControl} for the boolean property.
 * 
 * @author Ivan Senic
 * 
 */
public class BooleanPropertyControl extends AbstractPropertyControl<BooleanProperty, Boolean> {

	/**
	 * Button for on/off.
	 */
	private Button button;

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property.
	 * @param propertyUpdateListener
	 *            Property update listener to report updates to.
	 */
	public BooleanPropertyControl(BooleanProperty property, IPropertyUpdateListener propertyUpdateListener) {
		super(property, propertyUpdateListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createControl(Composite parent) {
		button = new Button(parent, SWT.TOGGLE);
		button.setSelection(property.getValue().booleanValue());
		updateText();

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateText();
				sendPropertyUpdateEvent(Boolean.valueOf(button.getSelection()));
			}
		});

		return button;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void showDefaultValue() {
		button.setSelection(property.getDefaultValue().booleanValue());
		updateText();
	}

	/**
	 * Updates the text in the button.
	 */
	private void updateText() {
		if (button.getSelection()) {
			button.setText("On");
		} else {
			button.setText("Off");
		}
	}

}
