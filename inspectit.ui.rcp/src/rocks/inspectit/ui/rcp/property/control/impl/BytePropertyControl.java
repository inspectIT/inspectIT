package info.novatec.inspectit.rcp.property.control.impl;

import info.novatec.inspectit.cmr.property.configuration.impl.ByteProperty;
import info.novatec.inspectit.rcp.property.IPropertyUpdateListener;
import info.novatec.inspectit.rcp.property.control.AbstractPropertyControl;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Property control for {@link ByteProperty}.
 * 
 * @author Ivan Senic
 * 
 */
public class BytePropertyControl extends AbstractPropertyControl<ByteProperty, Long> {

	/**
	 * Combo displaying the available units.
	 */
	private Combo unitCombo;

	/**
	 * Value being displayed.
	 */
	private Text valueText;

	/**
	 * Flag to skip the modify listener when {@link #valueText} is changed by us.
	 */
	private boolean modifyMarker;

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property.
	 * @param propertyUpdateListener
	 *            Property update listener to report updates to.
	 */
	public BytePropertyControl(ByteProperty property, IPropertyUpdateListener propertyUpdateListener) {
		super(property, propertyUpdateListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		valueText = new Text(composite, SWT.BORDER | SWT.RIGHT);
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		valueText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String oldText = valueText.getText();
				String update = e.text;
				String newText = oldText.substring(0, e.start) + update + oldText.substring(e.end, oldText.length());

				// allow blank text
				if (StringUtils.isNotBlank(newText)) {
					// otherwise prove we have a valid double number
					try {
						Double.parseDouble(newText);
					} catch (NumberFormatException exception) {
						e.doit = false;
						return;
					}
				}
			}
		});
		valueText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!modifyMarker) {
					String text = valueText.getText();
					if (!text.isEmpty() && (text.charAt(0) != '-' || text.length() > 1)) {
						long currentSize = getCurrentSize();
						sendPropertyUpdateEvent(currentSize);
					}
				} else {
					modifyMarker = false;
				}
			}
		});

		valueText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String text = valueText.getText();
				if (text.isEmpty()) {
					displayValue(getLastCorrectValue());
				}
			}
		});

		unitCombo = new Combo(composite, SWT.BORDER);
		unitCombo.setItems(new String[] { "B", "KB", "MB", "GB" });
		GridData unitGd = new GridData(SWT.RIGHT, SWT.FILL, false, false);
		unitGd.widthHint = 60;
		unitCombo.setLayoutData(unitGd);
		unitCombo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				long currentSize = getLastCorrectValue();
				int exp = unitCombo.getSelectionIndex();
				displayValue(currentSize, exp);
			};
		});

		displayValue(property.getValue().longValue());

		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void showDefaultValue() {
		displayValue(property.getDefaultValue().longValue());
	}

	/**
	 * @return Returns current size defined by spinner and unit.
	 */
	private long getCurrentSize() {
		return (long) (Double.parseDouble(valueText.getText()) * Math.pow(1024, unitCombo.getSelectionIndex()));
	}

	/**
	 * Displays value in text based on default unit .
	 * 
	 * @param bytes
	 *            Amount of bytes.
	 */
	private void displayValue(long bytes) {
		int exp = getUnit(bytes);
		unitCombo.select(exp);
		displayValue(bytes, exp);
	}

	/**
	 * Displays value in text based on given exp.
	 * 
	 * @param bytes
	 *            Amount of bytes.
	 * @param exp
	 *            Unit index or exp.
	 */
	private void displayValue(long bytes, int exp) {
		int unit = 1024;
		double value = (double) bytes / Math.pow(unit, exp);
		modifyMarker = true;
		valueText.setText(String.format(Locale.ENGLISH, "%.2f", value));
	}

	/**
	 * Unit we want to display for required amount of bytes.
	 * 
	 * @param bytes
	 *            Amount of bytes.
	 * @return {@link #BYTES}, {@link #KILO_BYTES} or {@link #MEGA_BYTES}
	 */
	private int getUnit(long bytes) {
		bytes = Math.abs(bytes);
		int unit = 1024;
		if (bytes < unit) {
			return 0;
		} else {
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			if (exp < 4) {
				return exp;
			} else {
				return 3;
			}
		}
	}

}
