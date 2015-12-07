/**
 *
 */
package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;
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
	 * Property ID for toggling the advanced view.
	 */
	public static final int ADVANCED_VIEW_TOGGLE_PROP_ID = 1023;

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
	 * Action for switching between simple and advanced view.
	 */
	private final ToggleAdvancedViewAction toggleAdvancedAction = new ToggleAdvancedViewAction(ENABLE_ADVANCED);

	/**
	 * Property listeners to be modified when a switch of the mode (simple vs. advanced) has been
	 * performed.
	 */
	private final List<IPropertyListener> propertyListeners = new ArrayList<>(2);

	/**
	 * Indicates whether this form part is editable or not.
	 */
	private boolean editable;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            Title of the part.
	 * @param parent
	 *            The parent composite.
	 * @param managedForm
	 *            Managed form where to add the part to.
	 */
	public SelectiveRulesPart(String title, Composite parent, IManagedForm managedForm) {
		this.title = title;
		this.parent = parent;
		this.managedForm = managedForm;

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
			rulesView = new AdvancedMatchingRulesPart(title, parent, managedForm, isEditable());
		} else {
			rulesView = new SimpleMatchingRulesPart(title, parent, managedForm, isEditable());
		}

		IToolBarManager toolbarManager = rulesView.getToolbarManager();
		if (toolbarManager.getItems().length > 0) {
			toolbarManager.add(new Separator());
		}
		toolbarManager.add(getToggleAdvancedAction());

		toolbarManager.update(true);

		toggleAdvancedAction.setChecked(advanced);
		toggleAdvancedAction.setText(advanced ? DISABLE_ADVANCED : ENABLE_ADVANCED);

		managedForm.addPart(rulesView);
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
	public AbstractExpression constructMatchingRuleExpression() {
		AbstractExpression expressionToView = rulesView.constructMatchingRuleExpression();
		expressionToView.setAdvanced(toggleAdvancedAction.isChecked());
		return expressionToView;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initContent(AbstractExpression matchingRuleExpression) {
		boolean wasDirty = isDirty();
		if (matchingRuleExpression.isAdvanced()) {
			selectView(true);
		} else {
			selectView(false);
			if (!SimpleMatchingRulesPart.canShowRule(matchingRuleExpression)) {
				// showing not-advanced mode for an expression that cannot be displayed by that
				// view. So, ask user how to proceed.
				MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Opening advanced rule in simple mode.", null,
						"The current configuration of the matching rule cannot be displayed in the simple mode."
								+ "Opening it in the simple mode will discard the current configuration of the matching rule.\n\nDo you want to open it in the simple mode, anyhow?",
						MessageDialogWithToggle.WARNING, new String[] { "Yes (Simple Mode)", "No (Advanced Mode)" }, 1);
				int result = dialog.open();

				if (MessageDialog.OK != result) {
					selectView(true);
				}
				wasDirty = true;

			}
		}
		if (wasDirty) {
			markDirty();
		}
		rulesView.initContent(matchingRuleExpression);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDescriptionText(String description) {
		rulesView.setDescriptionText(description);
	}

	public Action getToggleAdvancedAction() {
		return toggleAdvancedAction;
	}

	/**
	 * Gets {@link #propertyChangeListeners}.
	 *
	 * @return {@link #propertyChangeListeners}
	 */
	public List<IPropertyListener> getPropertyListeners() {
		return propertyListeners;
	}

	/**
	 * Adds a {@link IPropertyListener} instance.
	 *
	 * @param propertyListener
	 *            {@link IPropertyListener} instance to add.
	 */
	public void addPropertyListeners(IPropertyListener propertyListener) {
		this.propertyListeners.add(propertyListener);
	}

	/**
	 * Removes the given {@link IPropertyListener} instance.
	 *
	 * @param propertyListener
	 *            {@link IPropertyListener} instance to be removed.
	 */
	public void removePropertyListener(IPropertyListener propertyListener) {
		this.propertyListeners.remove(propertyListener);
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
	public void setEditable(boolean editable) {
		this.editable = editable;
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
			commit(false);
			for (IPropertyListener changeListener : getPropertyListeners()) {
				changeListener.propertyChanged(this, ADVANCED_VIEW_TOGGLE_PROP_ID);
			}
		}
	}

}
