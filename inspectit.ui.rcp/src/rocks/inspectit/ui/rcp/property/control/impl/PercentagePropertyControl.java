package info.novatec.inspectit.rcp.property.control.impl;

import info.novatec.inspectit.cmr.property.configuration.impl.PercentageProperty;
import info.novatec.inspectit.rcp.property.IPropertyUpdateListener;
import info.novatec.inspectit.rcp.property.control.AbstractPropertyControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * {@link AbstractPropertyControl} for the percentage property.
 * 
 * @author Ivan Senic
 * 
 */
public class PercentagePropertyControl extends AbstractPropertyControl<PercentageProperty, Float> {

	/**
	 * Spinner for displaying the value.
	 */
	private Spinner spinner;

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property.
	 * @param propertyUpdateListener
	 *            Property update listener to report updates to.
	 */
	public PercentagePropertyControl(PercentageProperty property, IPropertyUpdateListener propertyUpdateListener) {
		super(property, propertyUpdateListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		spinner = new Spinner(composite, SWT.BORDER | SWT.RIGHT);
		spinner.setValues((int) (property.getValue().floatValue() * 100), 0, 100, 0, 1, 5);
		spinner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		spinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				int selection = spinner.getSelection();
				Float value = Float.valueOf(selection / 100f);
				sendPropertyUpdateEvent(value);
			}
		});

		Label percentage = new Label(composite, SWT.NONE);
		percentage.setText("%");
		percentage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void showDefaultValue() {
		spinner.setSelection((int) (property.getDefaultValue().floatValue() * 100));
	}
}
