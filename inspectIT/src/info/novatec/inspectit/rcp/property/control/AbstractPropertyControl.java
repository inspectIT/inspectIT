package info.novatec.inspectit.rcp.property.control;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.BooleanProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.ByteProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.LongProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.PercentageProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.StringProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidationException;
import info.novatec.inspectit.cmr.property.configuration.validation.ValidationError;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.property.IPropertyUpdateListener;
import info.novatec.inspectit.rcp.property.control.impl.BooleanPropertyControl;
import info.novatec.inspectit.rcp.property.control.impl.BytePropertyControl;
import info.novatec.inspectit.rcp.property.control.impl.LongPropertyControl;
import info.novatec.inspectit.rcp.property.control.impl.PercentagePropertyControl;
import info.novatec.inspectit.rcp.property.control.impl.StringPropertyControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Abstract class for all property controls.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of the property control can handle.
 * @param <V>
 *            Value holding the property.
 */
public abstract class AbstractPropertyControl<E extends SingleProperty<V>, V> {

	/**
	 * Property.
	 */
	protected E property;

	/**
	 * If property gets changed, we will keep here the propertyUpdate.
	 */
	protected IPropertyUpdate<V> propertyUpdate;

	/**
	 * Property update listener to report updates to.
	 */
	protected IPropertyUpdateListener propertyUpdateListener;

	/**
	 * Decoration for the mail control for displaying validation errors.
	 */
	protected ControlDecoration decoration;

	/**
	 * If decoration should be displayed.
	 */
	private boolean decorationDisplayed = false;

	/**
	 * Main control that displays the value and enables updates.
	 */
	private Control control;

	/**
	 * All controls, needed for hiding in case of advanced properties.
	 */
	private List<Control> allControls = new ArrayList<>();

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property.
	 * @param propertyUpdateListener
	 *            Property update listener to report updates to.
	 */
	protected AbstractPropertyControl(E property, IPropertyUpdateListener propertyUpdateListener) {
		this.property = property;
		this.propertyUpdateListener = propertyUpdateListener;
	}

	/**
	 * Create control that will be displayed for the property.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @return Created control.
	 */
	protected abstract Control createControl(Composite parent);

	/**
	 * Shows default value in the control.
	 */
	protected abstract void showDefaultValue();

	/**
	 * Shows or hides all controls for this property if property is advanced.
	 * 
	 * @param visible
	 *            <code>true</code> if control should be visible, <code>false</code> otherwise.
	 */
	public void showIfAdvanced(boolean visible) {
		if (property.isAdvanced()) {
			for (Control control : allControls) {
				control.setVisible(visible);
			}
			if (decorationDisplayed && visible) {
				decoration.show();
			} else {
				decoration.hide();
			}
		}
	}

	/**
	 * Restores default value in this property control.
	 */
	public void restoreDefault() {
		this.showDefaultValue();
		propertyUpdate = property.createRestoreDefaultPropertyUpdate();
		propertyUpdateListener.propertyUpdated(property, propertyUpdate);
	}

	/**
	 * Displays validation errors in the decoration box if any of the passed {@link ValidationError}
	 * s is involving the property displayed in this control. Otherwise the decoration box is
	 * hidden.
	 * 
	 * @param errors
	 *            Validation errors to check.
	 */
	public void displayValidationErrors(Collection<ValidationError> errors) {
		boolean showDecoration = false;
		StringBuilder stringBuilder = new StringBuilder("Validation errors:");
		for (ValidationError error : errors) {
			if (error.getInvolvedProperties().contains(property)) {
				showDecoration = true;
				stringBuilder.append("\n - ");
				stringBuilder.append(error.getMessage());
			}
		}

		if (showDecoration) {
			decoration.show();
			decoration.setDescriptionText(stringBuilder.toString());
		} else {
			decoration.hide();
		}
		decorationDisplayed = showDecoration;
	}

	/**
	 * Creates set of controls that will be displayed for the property.
	 * <p>
	 * It is expected that the parent composite has a grid layout with four columns.
	 * 
	 * @param parent
	 *            Parent composite.
	 */
	public synchronized void create(Composite parent) {
		if (null == control) {
			// name first
			Label name = new Label(parent, SWT.LEFT);
			name.setText(property.getName() + ":");
			name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

			// then specific control
			control = createControl(parent);
			GridData controlGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			controlGridData.widthHint = 175;
			controlGridData.horizontalIndent = 10;
			control.setLayoutData(controlGridData);

			// decoration for handling validation errors
			decoration = new ControlDecoration(control, SWT.LEFT | SWT.BOTTOM);
			decoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
			decoration.hide();

			// info icon
			Label info = new Label(parent, SWT.CENTER);
			info.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			info.setToolTipText(property.getDescription());
			info.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

			// server re-start icon if needed
			Label serverRestart = new Label(parent, SWT.CENTER);
			if (property.isServerRestartRequired()) {
				serverRestart.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_WARNING));
				serverRestart.setToolTipText("Changing this property requires restart of the CMR");
			}
			serverRestart.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

			CollectionUtils.addAll(allControls, new Object[] { name, control, info, serverRestart });
		}
	}

	/**
	 * Sends property update event.
	 * 
	 * @param newValue
	 *            New value.
	 */
	protected void sendPropertyUpdateEvent(V newValue) {
		if (!property.getValue().equals(newValue)) {
			try {
				propertyUpdate = property.createAndValidatePropertyUpdate(newValue);
				propertyUpdateListener.propertyUpdated(property, propertyUpdate);
			} catch (PropertyValidationException e) {
				PropertyValidation propertyValidation = e.getPropertyValidation();
				propertyUpdateListener.propertyValidationFailed(property, propertyValidation);
			}
		} else {
			propertyUpdateListener.propertyUpdateCanceled(property);
			propertyUpdate = null; // NOPMD
		}

	}

	/**
	 * @return Returns the last correctly set value for the property.
	 */
	protected V getLastCorrectValue() {
		if (null != propertyUpdate) {
			return propertyUpdate.getUpdateValue();
		} else {
			return property.getValue();
		}
	}

	/**
	 * Utility method for creating the {@link AbstractPropertyControl}.
	 * 
	 * @param property
	 *            Property to get the control for.
	 * @param propertyUpdateListener
	 *            Property update listener to report updates to.
	 * @return Returns {@link AbstractPropertyControl} or <code>null</code> if the one for the given
	 *         property can not be created.
	 */
	public static AbstractPropertyControl<?, ?> createFor(SingleProperty<?> property, IPropertyUpdateListener propertyUpdateListener) {
		if (property instanceof BooleanProperty) {
			return new BooleanPropertyControl((BooleanProperty) property, propertyUpdateListener);
		} else if (property instanceof PercentageProperty) {
			return new PercentagePropertyControl((PercentageProperty) property, propertyUpdateListener);
		} else if (property instanceof StringProperty) {
			return new StringPropertyControl((StringProperty) property, propertyUpdateListener);
		} else if (property instanceof LongProperty) {
			return new LongPropertyControl((LongProperty) property, propertyUpdateListener);
		} else if (property instanceof ByteProperty) {
			return new BytePropertyControl((ByteProperty) property, propertyUpdateListener);
		}
		return null;
	}
}
