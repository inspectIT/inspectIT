package info.novatec.inspectit.rcp.property.control.impl;

import info.novatec.inspectit.cmr.property.configuration.impl.LongProperty;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.property.IPropertyUpdateListener;
import info.novatec.inspectit.rcp.property.control.AbstractPropertyControl;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * {@link AbstractPropertyControl} for the long property.
 * 
 * @author Ivan Senic
 * 
 */
public class LongPropertyControl extends AbstractPropertyControl<LongProperty, Long> {

	/**
	 * Text to display long value.
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
	public LongPropertyControl(LongProperty property, IPropertyUpdateListener propertyUpdateListener) {
		super(property, propertyUpdateListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createControl(Composite parent) {
		text = new Text(parent, SWT.BORDER | SWT.RIGHT);
		text.setText(NumberFormatter.formatLong(property.getValue()));
		text.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String oldText = text.getText();
				String update = e.text;
				String newText = oldText.substring(0, e.start) + update + oldText.substring(e.end, oldText.length());

				// allow blank text
				if (StringUtils.isNotBlank(newText)) {
					// allow minus to be specified only
					if (1 == newText.length() && '-' == newText.charAt(0)) {
						return;
					}

					// otherwise prove we have a valid long number
					try {
						Long.parseLong(newText);
					} catch (NumberFormatException exception) {
						e.doit = false;
						return;
					}
				}
			}
		});
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String valueText = text.getText();
				if (!valueText.isEmpty() && (valueText.charAt(0) != '-' || valueText.length() > 1)) {
					Long value = Long.parseLong(valueText);
					sendPropertyUpdateEvent(value);
				}
			}
		});
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String valueText = text.getText();
				if (valueText.isEmpty()) {
					text.setText(NumberFormatter.formatLong(getLastCorrectValue().longValue()));
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
		text.setText(NumberFormatter.formatLong(property.getDefaultValue().longValue()));
	}
}
