package rocks.inspectit.ui.rcp.ci.form.part.business.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.HttpParameterRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.HttpUriRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.IpRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.MethodParameterRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.MethodSignatureRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.util.ListenerList;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.IControlValidationListener;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * This abstract class forms the basis for all editing elements allowing to view and modify specific
 * instances of {@link AbstractExpression} for an
 * {@link rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition} or a
 * {@link rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition}.
 *
 * @param <T>
 *            expression type
 * @author Alexander Wert
 *
 */
public abstract class AbstractRuleEditingElement<T extends AbstractExpression> {

	/**
	 * Number of columns in the grid layout of the main composite.
	 */
	public static final int NUM_GRID_COLUMNS = 8;

	/**
	 * Validates the contents of the passed {@link StringMatchingExpression} without the need to
	 * create corresponding editing controls.
	 *
	 * @param expression
	 *            {@link StringMatchingExpression} to validate
	 * @return a set of {@link ValidationState} instances.
	 */
	public static Set<ValidationState> validate(StringMatchingExpression expression) {
		StringMatchingExpression stringMatchingExpression = expression;
		MatchingRuleType ruleType = MatchingRulesEditingElementFactory.getMatchingRuleType(stringMatchingExpression);
		switch (ruleType) {
		case HTTP_PARAMETER:
			return HttpParameterRuleEditingElement.validate(stringMatchingExpression);
		case HTTP_URI:
			return HttpUriRuleEditingElement.validate(stringMatchingExpression);
		case IP:
			return IpRuleEditingElement.validate(stringMatchingExpression);
		case METHOD_PARAMETER:
			return MethodParameterRuleEditingElement.validate(stringMatchingExpression);
		case METHOD_SIGNATURE:
			return MethodSignatureRuleEditingElement.validate(stringMatchingExpression);
		default:
			return Collections.emptySet();
		}
	}

	/**
	 * Listeners that are notified when this element is modified.
	 */
	protected ListenerList<IDetailsModifiedListener<AbstractExpression>> modifyListeners = new ListenerList<>();

	/**
	 * Listeners that are notified when this element is disposed.
	 */
	protected ListenerList<DisposeListener> disposeListeners = new ListenerList<>();

	/**
	 * Name of the editing element.
	 */
	private final String name;

	/**
	 * Description text for the specific {@link AbstractRuleEditingElement} instance.
	 */
	private final String description;

	/**
	 * Indicates whether listeners shell be notified in the current state.
	 */
	private boolean notificationActive = true;

	/**
	 * Indicates whether this form part is editable or not.
	 */
	private final boolean editable;

	/**
	 * List of all controls in this editing element.
	 */
	private final List<Control> childControls = new ArrayList<>();

	/**
	 * The {@link AbstractExpression} instance under modification.
	 */
	private final T expression;

	/**
	 * Parent composite.
	 */
	private Composite parent;

	/**
	 * The type of the matching rule.
	 */
	private final IRulesExpressionType ruleType;

	/**
	 * Upstream validation manager.
	 */
	private final RulesValidationManager validationManager;

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param ruleType
	 *            the type of the matching rule
	 * @param description
	 *            Description text for the specific {@link AbstractRuleEditingElement} instance.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	public AbstractRuleEditingElement(T expression, IRulesExpressionType ruleType, String description, boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		if (null == expression) {
			throw new IllegalArgumentException("Expression must not be null!");
		}
		this.expression = expression;
		this.ruleType = ruleType;
		this.editable = editable;
		this.name = ruleType.toString();
		this.description = description;
		this.validationManager = new RulesValidationManager(upstreamValidationManager);
	}

	/**
	 * Creates specific controls of this editing element.
	 *
	 * @param parent
	 *            parent {@link Composite}.
	 * @param toolkit
	 *            {@link FormToolkit} to use for the creation of controls.
	 */
	protected abstract void createSpecificElements(Composite parent, FormToolkit toolkit);

	/**
	 * Creates the control validators for this element.
	 */
	public abstract void createControlValidators();

	/**
	 * Creates controls for this editing element.
	 *
	 * @param parent
	 *            parent {@link Composite}.
	 * @param toolkit
	 *            {@link FormToolkit} to use for the creation of controls.
	 * @param disposable
	 *            specifies whether the self-delete button shell be created for this editing
	 *            element.
	 */
	public void createControls(Composite parent, FormToolkit toolkit, boolean disposable) {
		this.parent = parent;
		Label icon = toolkit.createLabel(parent, "");
		icon.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		icon.setImage(InspectIT.getDefault().getImage(ruleType.getImageKey()));
		icon.setToolTipText(description);
		addControl(icon);

		FormText headingText = toolkit.createFormText(parent, false);
		headingText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_GRID_COLUMNS - 3, 1));
		headingText.setText("<form><p><b>" + getName() + "</b></p></form>", true, false);
		addControl(headingText);

		Label infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(description);
		addControl(infoLabel);
		FormText deleteText;
		if (disposable) {
			deleteText = toolkit.createFormText(parent, false);
			deleteText.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			deleteText.setText("<form><p><a href=\"delete\"><img href=\"deleteImg\" /></a></p></form>", true, false);
			deleteText.setImage("deleteImg", InspectIT.getDefault().getImage(InspectITImages.IMG_DELETE));
			deleteText.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					validationManager.removeValidationStates();
					dispose();
				}
			});
		} else {
			deleteText = toolkit.createFormText(parent, false);
			deleteText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		}
		addControl(deleteText);
		createSpecificElements(parent, toolkit);

		updateEnabledState();
	}

	/**
	 * Adds a {@link IDetailsModifiedListener} to this editing element.
	 *
	 * @param listener
	 *            {@link IDetailsModifiedListener} to add.
	 */
	public void addModifyListener(IDetailsModifiedListener<AbstractExpression> listener) {
		modifyListeners.add(listener);
	}

	/**
	 * Adds a {@link DisposeListener} to this editing element.
	 *
	 * @param listener
	 *            {@link DisposeListener} to add.
	 */
	public void addDisposeListener(DisposeListener listener) {
		disposeListeners.add(listener);
	}

	/**
	 * Disposes all controls.
	 */
	public void dispose() {
		validationManager.clearValidators();
		for (Control control : childControls) {
			if (null != control) {
				control.dispose();
			}
		}
		notifyDisposed();
	}

	/**
	 * Adds a control to this editing element.
	 *
	 * @param control
	 *            {@link Control} to add.
	 */
	protected void addControl(Control control) {
		childControls.add(control);
	}

	/**
	 * Notifies all modify listeners about a modification of the controls content.
	 */
	protected void notifyModifyListeners() {
		if (notificationActive) {
			for (IDetailsModifiedListener<AbstractExpression> listener : modifyListeners) {
				listener.contentModified(expression);
			}
		}
	}

	/**
	 * Executes sub-class specific initialization of the controls.
	 *
	 * @param expression
	 *            An {@link AbstractExpression} determining the content.
	 */
	protected abstract void executeSpecificInitialization(T expression);

	/**
	 * Gets {@link #expression}.
	 *
	 * @return {@link #expression}
	 */
	public T getExpression() {
		return expression;
	}

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets enabled state for common controls of the {@link AbstractRuleEditingElement} class.
	 */
	protected void updateEnabledState() {
		for (Control control : childControls) {
			if (null != control && !(control instanceof Label)) {
				control.setEnabled(isEditable());
			}
		}
	}

	/**
	 * Gets {@link #editable}.
	 *
	 * @return {@link #editable}
	 */
	protected boolean isEditable() {
		return editable;
	}

	/**
	 * Notifies all modify listeners about the disposition of this editing element.
	 */
	private void notifyDisposed() {
		if (notificationActive) {
			for (DisposeListener listener : disposeListeners) {
				Event event = new Event();
				event.widget = parent;
				DisposeEvent disposeEvent = new DisposeEvent(event);
				disposeEvent.data = this;
				listener.widgetDisposed(disposeEvent);
			}
		}
	}

	/**
	 * Initializes the content of the controls comprised in this editing element.
	 */
	public void initialize() {
		notificationActive = false;
		executeSpecificInitialization(expression);
		createControlValidators();
		notificationActive = true;
		validationManager.performInitialValidation();
	}

	/**
	 * Gets {@link #validationManager}.
	 *
	 * @return {@link #validationManager}
	 */
	public RulesValidationManager getValidationManager() {
		return validationManager;
	}

	/**
	 * Validation Manager for rule editing elements.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class RulesValidationManager implements IControlValidationListener {

		/**
		 * The upstream validation manager to be notified on changes.
		 */
		private final AbstractValidationManager<AbstractExpression> upstreamValidationManager;

		/**
		 * {@link ValidationControlDecoration} instances referenced by this rule editing element
		 * object.
		 */
		private final Map<ValidationControlDecoration<?>, String> controlValidators = new HashMap<>();

		/**
		 * Constructor.
		 *
		 * @param upstreamValidationManager
		 *            The upstream validation manager to be notified on changes.
		 */
		RulesValidationManager(AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
			this.upstreamValidationManager = upstreamValidationManager;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
			upstreamValidationManager.validationStateChanged(expression,
					new ValidationState(controlValidators.get(validationControlDecoration), valid, validationControlDecoration.getDescriptionText()));
		}

		/**
		 * Performs initial validation of the controls.
		 */
		private void performInitialValidation() {
			for (ValidationControlDecoration<?> validator : controlValidators.keySet()) {
				validationStateChanged(validator.isValid(), validator);
			}
		}

		/**
		 * Hides and disposes control validators.
		 */
		private void removeValidationStates() {
			upstreamValidationManager.validationStatesRemoved(expression);
		}

		/**
		 * Clears all validators.
		 */
		private void clearValidators() {
			controlValidators.clear();
		}

		/**
		 * Adds an {@link ValidationControlDecoration} instance.
		 *
		 * @param validator
		 *            {@link ValidationControlDecoration} instance to add
		 * @param controlId
		 *            the identifier of the {@link ValidationControlDecoration} instance
		 */
		public void addValidator(ValidationControlDecoration<?> validator, String controlId) {
			controlValidators.put(validator, controlId);
		}

	}

}
