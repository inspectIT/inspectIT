package info.novatec.inspectit.rcp.validation;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Abstract class for all {@link ValidationControlDecoration}s.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type of control to decorate.
 */
public abstract class ValidationControlDecoration<T extends Control> extends ControlDecoration {

	/**
	 * Control.
	 */
	private final T control;

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
	 * Default constructor.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @see ControlDecoration
	 */
	public ValidationControlDecoration(T control) {
		super(control, SWT.LEFT | SWT.BOTTOM);
		this.control = control;
		this.controlBackground = control.getBackground();
		this.nonValidBackground = new Color(control.getDisplay(), 255, 200, 200);
		this.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());

		startupValidation();
	}

	/**
	 * Secondary constructor. Registers listener to the list of validation listeners.
	 * 
	 * @param control
	 *            Control to decorate.
	 * @param listener
	 *            {@link IControlValidationListener}.
	 */
	public ValidationControlDecoration(T control, IControlValidationListener listener) {
		this(control);
		addControlValidationListener(listener);
	}

	/**
	 * Executes validation on the startup.
	 */
	protected final void startupValidation() {
		this.valid = !controlActive() || validate(control);
		if (this.valid) {
			hide();
			control.setBackground(controlBackground);
		} else {
			show();
			control.setBackground(nonValidBackground);
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
		if (valid) {
			hide();
			control.setBackground(controlBackground);
		} else {
			show();
			control.setBackground(nonValidBackground);
		}

		if (tmp != valid) {
			for (IControlValidationListener listener : validationListeners) {
				listener.validationStateChanged(valid, ValidationControlDecoration.this);
			}
		}
	}

	/**
	 * Register event type when validation should kick in.
	 * 
	 * @param eventType
	 *            Event type.
	 */
	public void registerListener(int eventType) {
		control.addListener(eventType, listener);
	}

	/**
	 * Adds {@link IControlValidationListener} to the list of listeners.
	 * 
	 * @param validationListener
	 *            {@link IControlValidationListener}.
	 */
	public void addControlValidationListener(IControlValidationListener validationListener) {
		validationListeners.add(validationListener);
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
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		nonValidBackground.dispose();
		super.dispose();
	}

}
