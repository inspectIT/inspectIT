package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.expression.impl.OrExpression;
import info.novatec.inspectit.ci.business.impl.IMatchingRuleProvider;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.validation.IValidatorRegistry;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;
import info.novatec.inspectit.rcp.validation.ValidatorKey;

import java.util.Collections;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;

/**
 * This class decorates an {@link IMatchingRulesPart} with the functionality of switching between a
 * {@link SimpleMatchingRulesPart} and a {@link AdvancedMatchingRulesPart}.
 *
 * @author Alexander Wert
 *
 */
public class SelectiveRulesPart implements IMatchingRulesPart {
	/**
	 * Enable advanced view text.
	 */
	private static final String ENABLE_ADVANCED = "Enable advanced rules view.";

	/**
	 * Disable advanced view text.
	 */
	private static final String DISABLE_ADVANCED = "Disable advanced rules view.";

	/**
	 * Title of the part.
	 */
	private final String title;

	/**
	 * The parent composite.
	 */
	private final Composite parent;

	/**
	 * Managed form where to add the part to.
	 */
	private final IManagedForm managedForm;

	/**
	 * The decorated {@link IMatchingRulesPart} instance.
	 */
	private IMatchingRulesPart rulesView;

	/**
	 * Indicates whether the current view is simple or advanced.
	 */
	private boolean advanced = false;

	/**
	 * Action for switching between simple and advanced view.
	 */
	private final ToggleAdvancedViewAction toggleAdvancedAction = new ToggleAdvancedViewAction(ENABLE_ADVANCED);

	/**
	 * Provider and receiver of the {@link AbstractExpression} instance edited in this form part.
	 */
	private IMatchingRuleProvider ruleProvider;

	/**
	 * The description text for this form part.
	 */
	private String descriptionText;

	/**
	 * Indicates whether this form part is editable or not.
	 */
	private boolean editable;

	/**
	 * The {@link IValidatorRegistry} instance to delegate validator events to.
	 */
	private final IValidatorRegistry validatorRegistry;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            Title of the part.
	 * @param parent
	 *            The parent composite.
	 * @param managedForm
	 *            Managed form where to add the part to.
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 */
	public SelectiveRulesPart(String title, Composite parent, IManagedForm managedForm, IValidatorRegistry validatorRegistry) {
		this.title = title;
		this.parent = parent;
		this.managedForm = managedForm;
		this.validatorRegistry = validatorRegistry;
		toggleAdvancedAction.setText(ENABLE_ADVANCED);
		toggleAdvancedAction.setChecked(false);
		createPart(false);
	}

	/**
	 * Switches the view mode.
	 *
	 * @param advanced
	 *            if true, switches to advanced mode, otherwise to simple mode.
	 */
	private void selectView(boolean advanced) {
		if (this.advanced != advanced) {
			Control insertAfter = null;
			if (null != rulesView) {
				for (Control child : parent.getChildren()) {
					if (child == rulesView.getControl()) {
						break;
					}
					insertAfter = child;
				}
				managedForm.removePart(rulesView);
				rulesView.dispose();
			}

			createPart(advanced);
			if (null != insertAfter) {
				rulesView.getControl().moveBelow(insertAfter);
			} else {
				rulesView.getControl().moveAbove(null);
			}
			managedForm.getForm().layout(true, true);
			this.advanced = advanced;
		}
	}

	/**
	 * Creates the {@link IMatchingRulesPart} instance to be decorated.
	 *
	 * @param advanced
	 *            indicates whether an {@link AdvancedMatchingRulesPart} (if true) or a
	 *            {@link SimpleMatchingRulesPart} shell be created.
	 */
	private void createPart(boolean advanced) {
		if (advanced) {
			rulesView = new AdvancedMatchingRulesPart(title, validatorRegistry);
		} else {
			rulesView = new SimpleMatchingRulesPart(title, parent, managedForm, validatorRegistry);
		}
		createContent(managedForm, parent);
		setEditable(isEditable());
		if (null != descriptionText) {
			setDescriptionText(descriptionText);
		}
		IToolBarManager toolbarManager = rulesView.getToolbarManager();
		if (toolbarManager.getItems().length > 0) {
			toolbarManager.add(new Separator());
		}
		toolbarManager.add(toggleAdvancedAction);
		toolbarManager.update(true);
		toggleAdvancedAction.setChecked(advanced);
		toggleAdvancedAction.setText(advanced ? DISABLE_ADVANCED : ENABLE_ADVANCED);
		managedForm.addPart(rulesView);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContent(IManagedForm managedForm, Composite parent) {
		rulesView.createContent(managedForm, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
		rulesView.initialize(form);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		rulesView.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return rulesView.isDirty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		rulesView.commit(onSave);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		return rulesView.setFormInput(input);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		rulesView.setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStale() {
		return rulesView.isStale();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		rulesView.refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initContent(IMatchingRuleProvider ruleProvider) {
		this.ruleProvider = ruleProvider;
		setEditable(ruleProvider.isChangeable());
		boolean wasDirty = isDirty();
		AbstractExpression expression = ruleProvider.getMatchingRuleExpression();
		boolean isAdvanced = expression.isAdvanced();
		selectView(isAdvanced);

		if (wasDirty) {
			markDirty();
		}
		if (expression instanceof BooleanExpression && !((BooleanExpression) expression).isValue()) {
			OrExpression orExpression = new OrExpression();
			orExpression.setAdvanced(isAdvanced);
			ruleProvider.setMatchingRuleExpression(orExpression);
		}
		rulesView.initContent(ruleProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDescriptionText(String description) {
		this.descriptionText = description;
		rulesView.setDescriptionText(description);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markDirty() {
		rulesView.markDirty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return rulesView.getControl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ToolBarManager getToolbarManager() {
		return rulesView.getToolbarManager();
	}

	/**
	 * Gets {@link #editable}.
	 *
	 * @return {@link #editable}
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Sets {@link #editable}.
	 *
	 * @param editable
	 *            New value for {@link #editable}
	 */
	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;
		rulesView.setEditable(editable);
	}

	/**
	 * Action for switching the mode between advanced and simple view.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class ToggleAdvancedViewAction extends Action {
		/**
		 * Constructor.
		 *
		 * @param text
		 *            the text for the action.
		 */
		ToggleAdvancedViewAction(String text) {
			super(text, AS_CHECK_BOX);
			setId(this.getClass().getSimpleName() + hashCode());
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADVANCED_MODE));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			AbstractExpression matchingRuleExpression = ruleProvider.getMatchingRuleExpression();
			matchingRuleExpression.setAdvanced(!matchingRuleExpression.isAdvanced());
			if (!matchingRuleExpression.isAdvanced() && !SimpleMatchingRulesPart.canShowRule(matchingRuleExpression)) {
				// showing not-advanced mode for an expression that cannot be displayed by that
				// view. So, ask user how to proceed.
				MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Opening advanced rule in simple mode.", null,
						"The current configuration of the matching rule cannot be displayed in the simple mode. Opening it in the simple mode will discard the current configuration of the matching rule.\n\nDo you want to open it in the simple mode, anyhow?",
						MessageDialogWithToggle.WARNING, new String[] { "Yes (Simple Mode)", "No (Advanced Mode)" }, 1);
				int result = dialog.open();

				if (MessageDialog.OK != result) {
					matchingRuleExpression.setAdvanced(true);
				} else {
					ValidatorKey key = new ValidatorKey();
					key.setAbstractExpression(matchingRuleExpression);
					validatorRegistry.unregisterValidators(Collections.singleton(key));
					markDirty();
				}
			} else {
				markDirty();
			}

			initContent(ruleProvider);
		}
	}

}
