package info.novatec.inspectit.rcp.ci.form.part.business.rules;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.page.ValidatorKey;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import info.novatec.inspectit.rcp.util.ListenerList;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This abstract class forms the basis for all editing elements allowing to view and modify specific
 * instances of {@link AbstractExpression} for an
 * {@link info.novatec.inspectit.ci.business.impl.ApplicationDefinition} or a
 * {@link info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition}.
 *
 * @param <T>
 *            expression type
 * @author Alexander Wert
 *
 */
public abstract class AbstractRuleEditingElement<T extends AbstractExpression> {
	/**
	 * Description text for the search in trace row.
	 */
	private static final String SEARCH_IN_TRACE_INFO_TEXT = "If disabled, only the root node of the call tree is evaluated against the specified condition.\n"
			+ "If enabled, the call tree (trace) is searched up to the specified maximum depth for a tree node that matches the specified condition.";

	/**
	 * Number of columns in the grid layout of the main composite.
	 */
	public static final int NUM_GRID_COLUMNS = 8;

	/**
	 * Listeners that are notified when this element is modified or disposed.
	 */
	protected ListenerList<IRuleEditingElementModifiedListener> modifyListeners = new ListenerList<IRuleEditingElementModifiedListener>();

	/**
	 * Switch for the search in depth property of the {@link AbstractExpression}.
	 */
	private boolean searchInDepth = false;

	/**
	 * Search depth property when {@link #searchInDepth} is enabled.
	 */
	private int searchDepth = -1;

	/**
	 * Check box to select {@link #searchInDepth} property.
	 */
	private Button searchInTraceCheckBox;

	/**
	 * Spinner for configuring the {@link #searchDepth} property.
	 */
	private Spinner depthSpinner;

	/**
	 * Name of the editing element.
	 */
	private final String name;

	/**
	 * Description text for the specific {@link AbstractRuleEditingElement} instance.
	 */
	private final String description;

	/**
	 * Indicates whether the searchInDepth sub-element shell be used in this editing element.
	 */
	private final boolean useSearchInDepthComposite;

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
	 * The {@link IValidatorRegistry} instance to delegate validator events to.
	 */
	private final IValidatorRegistry validatorRegistry;

	/**
	 * The {@link AbstractExpression} instance under modification.
	 */
	private final T expression;

	/**
	 * {@link ValidationControlDecoration} instances referenced by this rule editing element object.
	 */
	private final Map<ValidatorKey, ValidationControlDecoration<?>> controlValidators = new HashMap<>();

	/**
	 * A counter for the index of the {@link ValidationControlDecoration} instances of this element.
	 */
	private final AtomicInteger validationControlIndex = new AtomicInteger(0);

	/**
	 * Indicates whether this element has been already initialized or not.
	 */
	protected boolean initialized = false;

	/**
	 * The type of the matching rule.
	 */
	private final IRulesExpressionType ruleType;

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param ruleType
	 *            the type of the matching rule
	 * @param description
	 *            Description text for the specific {@link AbstractRuleEditingElement} instance.
	 * @param useSearchInDepthComposite
	 *            Indicates whether the searchInDepth sub-element shell be used in this editing
	 *            element.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 */
	public AbstractRuleEditingElement(T expression, IRulesExpressionType ruleType, String description, boolean useSearchInDepthComposite, boolean editable, IValidatorRegistry validatorRegistry) {
		this.expression = expression;
		this.ruleType = ruleType;
		this.editable = editable;
		this.name = ruleType.toString();
		this.description = description;
		this.useSearchInDepthComposite = useSearchInDepthComposite;
		this.validatorRegistry = validatorRegistry;
	}

	/**
	 * Constructs an {@link AbstractExpression} from the contents of the editing element controls.
	 * This method returns the same {@link AbstractExpression} object as passed in the constructor,
	 * however, with modified content.
	 *
	 * @return an {@link AbstractExpression}.
	 */
	public abstract AbstractExpression constructRuleExpression();

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
					deregisterValidators();
					dispose();
				}
			});
		} else {
			deleteText = toolkit.createFormText(parent, false);
			deleteText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		}
		addControl(deleteText);
		createSpecificElements(parent, toolkit);

		if (useSearchInDepthComposite) {
			Label searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceFillLabel);

			Label searchInTraceLabel = toolkit.createLabel(parent, "Search in trace:");
			searchInTraceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceLabel);

			searchInTraceCheckBox = toolkit.createButton(parent, "Yes", SWT.CHECK);
			searchInTraceCheckBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceCheckBox);

			Label depthLabel = toolkit.createLabel(parent, "Maximum search depth: ");
			depthLabel.setEnabled(false);
			depthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
			addControl(depthLabel);

			depthSpinner = new Spinner(parent, SWT.BORDER);
			depthSpinner.setMinimum(-1);
			depthSpinner.setMaximum(Integer.MAX_VALUE);
			depthSpinner.setSelection(-1);
			depthSpinner.setIncrement(1);
			depthSpinner.setPageIncrement(100);
			depthSpinner.setEnabled(false);
			depthSpinner.setToolTipText("A value of -1 means that no limit for the search depth is used!");
			depthSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			depthSpinner.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchDepth = depthSpinner.getSelection();
					notifyModifyListeners();
				}
			});
			addControl(depthSpinner);

			searchInTraceCheckBox.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (searchInTraceCheckBox.getSelection()) {
						depthSpinner.setEnabled(true);
						searchInDepth = true;
						searchDepth = depthSpinner.getSelection();
					} else {
						depthSpinner.setEnabled(false);
						searchInDepth = false;
					}
					notifyModifyListeners();
				}
			});
			Label searchInTraceInfoLabel = toolkit.createLabel(parent, "");
			searchInTraceInfoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			searchInTraceInfoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			searchInTraceInfoLabel.setToolTipText(SEARCH_IN_TRACE_INFO_TEXT);
			addControl(searchInTraceInfoLabel);
		} else {
			Label searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, NUM_GRID_COLUMNS, 1));
			addControl(searchInTraceFillLabel);
		}
		updateEnabledState();
	}

	/**
	 * Adds a {@link IRuleEditingElementModifiedListener} to this editing element.
	 *
	 * @param listener
	 *            {@link IRuleEditingElementModifiedListener} to add.
	 */
	public void addModifyListener(IRuleEditingElementModifiedListener listener) {
		modifyListeners.add(listener);
	}

	/**
	 * Disposes all controls.
	 */
	public void dispose() {
		disposeValidatorDecorations();

		for (Control control : childControls) {
			if (null != control) {
				control.dispose();
			}
		}

		notifyDisposed();
	}

	/**
	 * Deregisters control validators.
	 */
	public void deregisterValidators() {
		for (Entry<ValidatorKey, ValidationControlDecoration<?>> validatorEntry : controlValidators.entrySet()) {
			getValidatorRegistry().unregisterValidators(Collections.singleton(validatorEntry.getKey()));
		}
	}

	/**
	 * Hides and disposes control validators.
	 */
	public void disposeValidatorDecorations() {
		for (ValidationControlDecoration<?> validator : controlValidators.values()) {
			validator.dispose();
		}
		controlValidators.clear();
		validationControlIndex.set(0);
	}

	/**
	 * Gets {@link #searchInDepth}.
	 *
	 * @return {@link #searchInDepth}
	 */
	protected boolean isSearchInDepth() {
		return searchInDepth;
	}

	/**
	 * Gets {@link #searchDepth}.
	 *
	 * @return {@link #searchDepth}
	 */
	protected int getSearchDepth() {
		return searchDepth;
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
			for (IRuleEditingElementModifiedListener listener : modifyListeners) {
				listener.contentModified();
			}
		}
	}

	/**
	 * Executes sub-class specific initialization of the controls.
	 *
	 * @param expression
	 *            An {@link AbstractExpression} determining the content.
	 */
	protected void executeSpecificInitialization(T expression) {
		if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).isSearchNodeInTrace() && useSearchInDepthComposite) {
			searchDepth = ((StringMatchingExpression) expression).getMaxSearchDepth();
			searchInDepth = true;
			searchInTraceCheckBox.setSelection(true);

			depthSpinner.setSelection(searchDepth);
			depthSpinner.setEnabled(true);
		}
	}

	/**
	 * Adds an {@link ValidationControlDecoration} instance.
	 *
	 * @param validator
	 *            {@link ValidationControlDecoration} instance to add
	 */
	protected void addValidator(ValidationControlDecoration<?> validator) {
		ValidatorKey key = getNextControlValidatorId();
		getValidatorRegistry().registerValidator(key, validator);
		controlValidators.put(key, validator);
	}

	/**
	 * Gets {@link #validatorRegistry}.
	 *
	 * @return {@link #validatorRegistry}
	 */
	protected IValidatorRegistry getValidatorRegistry() {
		return validatorRegistry;
	}

	/**
	 * Gets {@link #expression}.
	 *
	 * @return {@link #expression}
	 */
	protected T getExpression() {
		return expression;
	}

	/**
	 * Generates the next identifier for a control validator.
	 *
	 * @return next identifier
	 */
	protected ValidatorKey getNextControlValidatorId() {
		ValidatorKey key = new ValidatorKey();
		key.setAbstractExpression(expression);
		key.setControlIndex(validationControlIndex.getAndIncrement());
		key.setGroupName(name);
		return key;
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
	private void updateEnabledState() {
		for (Control control : childControls) {
			if (null != control && !(control instanceof Label)) {
				control.setEnabled(isEditable());
			}
		}

		if (useSearchInDepthComposite) {
			depthSpinner.setEnabled(isEditable() && searchInTraceCheckBox.getSelection());
		}
	}

	/**
	 * Gets {@link #editable}.
	 *
	 * @return {@link #editable}
	 */
	private boolean isEditable() {
		return editable;
	}

	/**
	 * Notifies all modify listeners about the disposition of this editing element.
	 */
	private void notifyDisposed() {
		if (notificationActive) {
			for (IRuleEditingElementModifiedListener listener : modifyListeners) {
				listener.elementDisposed(this);
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
		validatorRegistry.performInitialValidation();
		initialized = true;
	}

	/**
	 * Interface for listeners that are modified when an instance of a
	 * {@link AbstractRuleEditingElement} is modified or disposed.
	 *
	 * @author Alexander Wert
	 *
	 */
	public interface IRuleEditingElementModifiedListener {
		/**
		 * Contents have been modified.
		 */
		void contentModified();

		/**
		 * Element has been disposed.
		 *
		 * @param ruleComposite
		 *            {@link AbstractRuleEditingElement} to be disposed.
		 */
		void elementDisposed(AbstractRuleEditingElement<?> ruleComposite);
	}
}
