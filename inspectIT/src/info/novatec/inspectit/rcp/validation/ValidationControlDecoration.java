package info.novatec.inspectit.rcp.validation;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IMessageManager;

/**
 * Abstract class for all {@link ValidationControlDecoration}s.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type of control to decorate.
 */
public abstract class ValidationControlDecoration<T extends Control> {

	/**
	 * Control.
	 */
	private final T control;

	/**
	 * {@link IMessageManager} that can be provided for reporting the validation of the input.
	 */
	private IMessageManager messageManager;

	/**
	 * Control decoration to be created when the {@link #messageManager} is not provided.
	 */
	private ControlDecoration controlDecoration;

	/**
	 * Description text.
	 */
	private String descriptionText;

	/**
	 * Color of the control background.
	 */
	private final Color controlBackground;

	/**
	 * Color to highlight the widget when input is not valid.
	 */
	private final Color nonValidBackground;

	/**
	 * If control holds valid values.
	 */
	private boolean valid = true;

	/**
	 * Defines if the control background should be changed to/from valid/invalid color. Defaults to
	 * <code>true</code>. If set to <code>false</code> no background will be changed on the control
	 * being validated.
	 */
	private boolean alterControlBackround = true;

	/**
	 * Validation listeners.
	 */
	private Collection<IControlValidationListener> validationListeners = new HashSet<>();

	/**
	 * Listener used to be hooked to the events.
	 */
	private final Listener listener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			executeValidation();
		}
	};

	/**
	 * Simple constructor. Control decoration will be handled by message manager if one is supplied.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param messageManager
	 *            {@link IMessageManager} to use for reporting messages. Can be <code>null</code>
	 */
	public ValidationControlDecoration(T control, IMessageManager messageManager) {
		this(control, messageManager, true);
	}

	/**
	 * Simple constructor. Control decoration will not be handled by message manager.Registers
	 * listener to the list of validation listeners.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param listener
	 *            {@link IControlValidationListener}.
	 */
	public ValidationControlDecoration(T control, IControlValidationListener listener) {
		this(control, (IMessageManager) null);
		addControlValidationListener(listener);
	}

	/**
	 * Default constructor that allows setting of the {@link #alterControlBackround}. Control
	 * decoration will be handled by message manager if one is supplied.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param messageManager
	 *            {@link IMessageManager} to use for reporting messages. Can be <code>null</code>
	 * @param alterControlBackround
	 *            Defines if the control background should be changed to/from valid/invalid color.
	 *            Defaults to <code>true</code>. If set to <code>false</code> no background will be
	 *            changed on the control being validated.
	 */
	public ValidationControlDecoration(T control, IMessageManager messageManager, boolean alterControlBackround) {
		this.control = control;
		this.messageManager = messageManager;
		this.alterControlBackround = alterControlBackround;
		this.controlBackground = control.getBackground();
		this.nonValidBackground = new Color(control.getDisplay(), 255, 200, 200);

		if (null == messageManager) {
			this.controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.BOTTOM);
			this.controlDecoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
		}

		startupValidation();
	}

	/**
	 * Constructor. Registers listener to the list of validation listeners.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param messageManager
	 *            {@link IMessageManager} to use for reporting messages.
	 * @param listener
	 *            {@link IControlValidationListener}.
	 */
	public ValidationControlDecoration(T control, IMessageManager messageManager, IControlValidationListener listener) {
		this(control, messageManager);
		addControlValidationListener(listener);
	}

	/**
	 * Constructor allowing setting of all fields. Registers listener to the list of validation
	 * listeners.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param messageManager
	 *            {@link IMessageManager} to use for reporting messages.
	 * @param alterControlBackround
	 *            Defines if the control background should be changed to/from valid/invalid color.
	 *            Defaults to <code>true</code>. If set to <code>false</code> no background will be
	 *            changed on the control being validated.
	 * @param listener
	 *            {@link IControlValidationListener}.
	 */
	public ValidationControlDecoration(T control, IMessageManager messageManager, boolean alterControlBackround, IControlValidationListener listener) {
		this(control, messageManager, alterControlBackround);
		addControlValidationListener(listener);
	}

	/**
	 * Executes validation on the startup.
	 */
	protected final void startupValidation() {
		this.valid = !controlActive() || validate(control);
		if (this.valid) {
			hide();
		} else {
			show();
		}
	}

	/**
	 * Validates the current value in the control.
	 * 
	 * @param control
	 *            {@link Control}
	 * @return <code>true</code> if validation passed, false otherwise.
	 */
	protected abstract boolean validate(T control);

	/**
	 * Executes the validation, shows or hides the decoration and informs the
	 * {@link #validationListeners}.
	 */
	public void executeValidation() {
		boolean tmp = valid;
		if (controlActive()) {
			valid = validate(control);
		} else {
			valid = true;
		}

		if (tmp != valid) {
			if (valid) {
				hide();
			} else {
				show();
			}
			for (IControlValidationListener listener : validationListeners) {
				listener.validationStateChanged(valid, ValidationControlDecoration.this);
			}
		}
	}

	/**
	 * Shows error.
	 */
	private void show() {
		if (null != controlDecoration) {
			controlDecoration.show();
		} else if (null != messageManager) {
			messageManager.addMessage(this, descriptionText, null, IMessageProvider.ERROR, control);
		}
		if (alterControlBackround) {
			control.setBackground(nonValidBackground);
		}
	}

	/**
	 * Hides error.
	 */
	private void hide() {
		if (null != controlDecoration) {
			controlDecoration.hide();
		} else if (null != messageManager) {
			messageManager.removeMessage(this, control);
		}
		if (alterControlBackround) {
			control.setBackground(controlBackground);
		}
	}

	/**
	 * Register event type when validation should kick in.
	 * 
	 * @param eventType
	 *            Event type.
	 */
	public void registerListener(int eventType) {
		registerListener(control, eventType);
	}

	/**
	 * Register event type when validation should kick in with any arbitrary control.
	 * 
	 * @param control
	 *            to register validation to
	 * @param eventType
	 *            Event type.
	 */
	public void registerListener(Control control, int eventType) {
		Assert.isNotNull(control);

		control.addListener(eventType, listener);
	}

	/**
	 * Adds {@link IControlValidationListener} to the list of listeners.
	 * 
	 * @param validationListener
	 *            {@link IControlValidationListener}.
	 */
	public void addControlValidationListener(IControlValidationListener validationListener) {
		if (null != validationListener) {
			validationListeners.add(validationListener);
		}
	}

	/**
	 * Returns if control is active. No validation will be process when control is not active.
	 * 
	 * @return Returns if control is active. No validation will be process when control is not
	 *         active.
	 */
	private boolean controlActive() {
		return control.isEnabled();
	}

	/**
	 * Gets {@link #valid}.
	 * 
	 * @return {@link #valid}
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Gets {@link #control}.
	 * 
	 * @return {@link #control}
	 */
	public T getControl() {
		return control;
	}

	/**
	 * Gets {@link #descriptionText}.
	 * 
	 * @return {@link #descriptionText}
	 */
	public String getDescriptionText() {
		return descriptionText;
	}

	/**
	 * Sets {@link #descriptionText}.
	 * 
	 * @param descriptionText
	 *            New value for {@link #descriptionText}
	 */
	public void setDescriptionText(String descriptionText) {
		this.descriptionText = descriptionText;
		if (null != controlDecoration) {
			controlDecoration.setDescriptionText(descriptionText);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		nonValidBackground.dispose();
		if (null != controlDecoration) {
			controlDecoration.dispose();
		}
	}

}
