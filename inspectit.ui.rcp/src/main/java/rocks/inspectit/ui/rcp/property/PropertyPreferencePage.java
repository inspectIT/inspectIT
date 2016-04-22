package rocks.inspectit.ui.rcp.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormText;

import rocks.inspectit.shared.cs.cmr.property.configuration.PropertySection;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidation;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.ValidationError;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.IPropertyUpdate;
import rocks.inspectit.ui.rcp.property.control.AbstractPropertyControl;

/**
 * Preference page for displaying the CMR properties.
 *
 * @author Ivan Senic
 *
 */
public class PropertyPreferencePage extends PreferencePage implements IPropertyUpdateListener {

	/**
	 * {@link Comparator} to sort properties by being advanced or not.
	 */
	private static final Comparator<SingleProperty<?>> PROPERTIES_COMPARATOR = new Comparator<SingleProperty<?>>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(SingleProperty<?> p1, SingleProperty<?> p2) {
			if (p1.isAdvanced() && !p2.isAdvanced()) {
				return 1;
			} else if (p1.isAdvanced() && !p2.isAdvanced()) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	/**
	 * {@link PropertySection} this page will display.
	 */
	private List<SingleProperty<?>> properties;

	/**
	 * All created property controls in this page.
	 */
	private Collection<AbstractPropertyControl<?, ?>> propertyControls = new ArrayList<>();

	/**
	 * Update map containing all correctly updated properties and their
	 * {@link AbstractPropertyUpdate}.
	 */
	protected Map<SingleProperty<?>, IPropertyUpdate<?>> correctUpdateMap = new HashMap<>();

	/**
	 * Map of the current validation problems on this page.
	 */
	private Map<SingleProperty<?>, PropertyValidation> validationMap = new HashMap<>();

	/**
	 * If the page contains all advanced properties.
	 */
	private boolean allAdvancedProperties = true;

	/**
	 * If advanced properties are currently visible.
	 */
	private boolean advancedVisible;

	/**
	 * Button for restoring defaults.
	 */
	private Button restoreDefaults;

	/**
	 * Main composite.
	 */
	private Composite mainComposite;

	/**
	 * For displaying the advanced properties text.
	 */
	private FormText advancedText;

	/**
	 * Default constructor.
	 *
	 * @param name
	 *            Name of the page.
	 * @param properties
	 *            Collection of properties to display.
	 */
	public PropertyPreferencePage(String name, Collection<SingleProperty<?>> properties) {
		super(name);
		this.properties = new ArrayList<>(properties);
		noDefaultAndApplyButton();
		for (SingleProperty<?> property : this.properties) {
			if (!property.isAdvanced()) {
				allAdvancedProperties = false;
			}
		}
		Collections.sort(this.properties, PROPERTIES_COMPARATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyUpdated(SingleProperty<?> property, IPropertyUpdate<?> propertyUpdate) {
		validationMap.remove(property);
		if (!propertyUpdate.isRestoreDefault() || !property.isDefaultValueUsed()) {
			correctUpdateMap.put(property, propertyUpdate);
		} else {
			correctUpdateMap.remove(property);
		}
		updatePage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyUpdateCanceled(SingleProperty<?> property) {
		validationMap.remove(property);
		correctUpdateMap.remove(property);
		updatePage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyValidationFailed(SingleProperty<?> property, PropertyValidation propertyValidation) {
		if (propertyValidation.hasErrors()) {
			validationMap.put(property, propertyValidation);
			correctUpdateMap.remove(property);
		}
		updatePage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid() {
		return getValidationErrorsCount() == 0;
	}

	/**
	 * Returns all valid property updates on this page. Note that this method will return empty
	 * collection if the page is not valid.
	 *
	 * @return Returns all valid property updates on this page.
	 */
	public Collection<IPropertyUpdate<?>> getPropertyUpdates() {
		if (isValid()) {
			return correctUpdateMap.values();
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * If updates made on this page require the server restart.
	 *
	 * @return True if any correct update on this page requires the server restart
	 */
	public boolean isServerRestartRequired() {
		if (isValid()) {
			for (SingleProperty<?> property : correctUpdateMap.keySet()) {
				if (property.isServerRestartRequired()) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	/**
	 * Updates the page valid status, message and error table if needed.
	 */
	protected void updatePage() {
		setValid(isValid());
		updateMessage();
		updateValidationMessages();
	}

	/**
	 * Returns current count of validation errors. Sub-class can override this method to provide
	 * additional errors in the count.
	 *
	 * @return Returns current count of validation errors on this page.
	 */
	public int getValidationErrorsCount() {
		if (MapUtils.isEmpty(validationMap)) {
			return 0;
		} else {
			int count = 0;
			for (PropertyValidation propertyValidation : validationMap.values()) {
				count += propertyValidation.getErrorCount();
			}
			return count;
		}
	}

	/**
	 * Returns all validation errors. Sub-class can override this method to provide additional
	 * errors.
	 *
	 * @return Returns current validation errors on this page.
	 */
	public Collection<ValidationError> getValidationErrors() {
		if (MapUtils.isEmpty(validationMap)) {
			return Collections.emptyList();
		} else {
			List<ValidationError> returnList = new ArrayList<>();
			for (PropertyValidation propertyValidation : validationMap.values()) {
				returnList.addAll(propertyValidation.getErrors());
			}
			return returnList;
		}
	}

	/**
	 * Shows/hides advanced properties and it's controls.
	 *
	 * @param advanced
	 *            True if advanced should be shown, false otherwise.
	 */
	public void showAdvanced(boolean advanced) {
		advancedVisible = advanced;

		if (null != advancedText) {
			advancedText.setVisible(advanced);
		}
		if (CollectionUtils.isNotEmpty(propertyControls)) {
			for (AbstractPropertyControl<?, ?> propertyControl : propertyControls) {
				propertyControl.showIfAdvanced(advanced);
			}

			mainComposite.layout();
			mainComposite.update();
			if (allAdvancedProperties && !advancedVisible) {
				setMessage("This page contains only advanced properties.", INFORMATION);
			} else {
				updateMessage();
			}
		}
	}

	/**
	 * Updates the message of the page based on number of validation errors.
	 */
	private void updateMessage() {
		int count = getValidationErrorsCount();
		if (0 == count) {
			setMessage(null);
		} else {
			String msg = count + " validation error" + (count > 1 ? "s" : "") + " detected";
			setMessage(msg, ERROR);
		}
	}

	/**
	 * Updates the error composite.
	 */
	private void updateValidationMessages() {
		if (!mainComposite.isDisposed()) {
			for (AbstractPropertyControl<?, ?> propertyControl : propertyControls) {
				propertyControl.displayValidationErrors(getValidationErrors());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void performDefaults() {
		for (AbstractPropertyControl<?, ?> control : propertyControls) {
			control.restoreDefault();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * We hook here the show advanced and our own restore defaults button.
	 */
	@Override
	protected void contributeButtons(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		restoreDefaults = new Button(parent, SWT.PUSH);
		restoreDefaults.setText("Restore Defaults");
		restoreDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performDefaults();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createContents(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);

		boolean advancedCreated = false;

		for (SingleProperty<?> property : properties) {
			if (!advancedCreated && property.isAdvanced()) {
				advancedText = new FormText(mainComposite, SWT.WRAP);
				advancedText.setText("<form><p><b>Advanced properties:</b></p></form>", true, false);
				advancedText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
				advancedCreated = true;
			}
			createSinglePropertyContents(property, mainComposite);
		}

		Dialog.applyDialogFont(mainComposite);
		showAdvanced(advancedVisible);
		mainComposite.layout();
		return mainComposite;
	}

	/**
	 * Creates one line of widgets in the page for displaying a single property.
	 *
	 * @param property
	 *            {@link SingleProperty} to create content for.
	 * @param parent
	 *            Composite parent
	 */
	private void createSinglePropertyContents(SingleProperty<?> property, Composite parent) {
		AbstractPropertyControl<?, ?> propertyControl = AbstractPropertyControl.createFor(property, this);
		propertyControl.create(parent);
		propertyControls.add(propertyControl);
	}

	/**
	 * Gets {@link #allAdvancedProperties}.
	 *
	 * @return {@link #allAdvancedProperties}
	 */
	public boolean isAllAdvancedProperties() {
		return allAdvancedProperties;
	}

}
