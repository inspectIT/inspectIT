package rocks.inspectit.ui.rcp.ci.form.part.business;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.BooleanExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.OrExpression;
import rocks.inspectit.shared.cs.ci.business.impl.IMatchingRuleProvider;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.action.MenuAction;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.IControlValidationListener;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * Composite element for matching rules viewing, creation and modification.
 *
 * @author Alexander Wert
 *
 */
public class SimpleMatchingRulesPart extends SectionPart implements IMatchingRulesPart, IControlValidationListener {

	/**
	 * Checks whether the passed {@link AbstractExpression} can be displayed by the
	 * {@link SimpleMatchingRulesPart}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} instance to check.
	 * @return true, if {@link SimpleMatchingRulesPart} can display the passed
	 *         {@link AbstractExpression} instance.
	 */
	public static boolean canShowRule(AbstractExpression expression) {
		if (expression instanceof OrExpression) {
			for (AbstractExpression childExpression : ((OrExpression) expression).getOperands()) {
				try {
					MatchingRulesEditingElementFactory.getMatchingRuleType(childExpression);
				} catch (IllegalArgumentException e) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * List of {@link AbstractRuleEditingElement} comprised in this element.
	 */
	private final List<AbstractRuleEditingElement<?>> ruleElements = new ArrayList<AbstractRuleEditingElement<?>>();

	/**
	 * Main {@link Composite} of this element.
	 */
	private Composite main;

	/**
	 * Indicates whether this element is in the initialization phase.
	 */
	private boolean initializationPhase = false;

	/**
	 * Label holding the description text.
	 */
	private Label descriptionLabel;

	/**
	 * Indicates whether this form part is editable or not.
	 */
	private boolean editable;

	/**
	 * The toolbar manager used in this part.
	 */
	private ToolBarManager toolBarManager;

	/**
	 * Title of the section.
	 */
	private final String title;

	/**
	 * Provider and receiver of the {@link AbstractExpression} instance edited in this form part.
	 */
	private IMatchingRuleProvider ruleProvider;

	/**
	 * The upstream validation manager.
	 */
	private final AbstractValidationManager<AbstractExpression> validationManager;

	/**
	 * {@link ValidationControlDecoration} instance used to check whether at least one expression is
	 * defined in the list.
	 */
	private ValidationControlDecoration<Label> listValidation;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            The title of the section.
	 * @param parent
	 *            parent {@link Composite}.
	 * @param managedForm
	 *            the {@link IManagedForm} to add this part to.
	 * @param validationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	public SimpleMatchingRulesPart(String title, Composite parent, IManagedForm managedForm, AbstractValidationManager<AbstractExpression> validationManager) {
		super(parent, managedForm.getToolkit(), Section.TITLE_BAR | Section.EXPANDED);
		this.title = title;
		this.validationManager = validationManager;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContent(IManagedForm managedForm, Composite parent) {
		Section section = getSection();
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		section.setText(title);

		FormToolkit toolkit = managedForm.getToolkit();

		main = toolkit.createComposite(getSection(), SWT.NONE);
		GridLayout layout = new GridLayout(AbstractRuleEditingElement.NUM_GRID_COLUMNS, false);
		layout.horizontalSpacing = 8;
		main.setLayout(layout);

		descriptionLabel = toolkit.createLabel(section, "");
		descriptionLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		section.setDescriptionControl(descriptionLabel);
		section.setClient(main);

		createToolbar();
		updateEnabledState();
		listValidation = new ValidationControlDecoration<Label>(descriptionLabel, this, false) {

			@Override
			protected boolean validate(Label control) {
				return !ruleElements.isEmpty();
			}
		};
		listValidation.setDescriptionText("At least one rule expression must be defined!");
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void initContent(IMatchingRuleProvider ruleProvider) {
		initializationPhase = true;
		this.ruleProvider = ruleProvider;
		reset();
		AbstractExpression matchingRuleExpression = ruleProvider.getMatchingRuleExpression();
		if (canShowRule(matchingRuleExpression)) {
			for (AbstractExpression expression : ((OrExpression) matchingRuleExpression).getOperands()) {
				addNewRuleEditingElement(expression);
			}
			commit(false);
		}

		layoutAndReflow();

		initializationPhase = false;
		listValidation.executeValidation(true);
	}

	/**
	 * Sets the description text for this section.
	 *
	 * @param description
	 *            new description text.
	 */
	@Override
	public void setDescriptionText(String description) {
		descriptionLabel.setText(description);
		getSection().layout(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return getSection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ToolBarManager getToolbarManager() {
		return toolBarManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (null != ruleProvider) {
			AbstractExpression expression = constructMatchingRuleExpression();
			ruleProvider.setMatchingRuleExpression(expression);
		}
		if (onSave) {
			super.commit(onSave);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		getManagedForm().removePart(this);
		getSection().dispose();
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
		updateEnabledState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		validationManager.validationStateChanged(MatchingRulesEditingElementFactory.InvalidExpression.getInstance(),
				new ValidationState("emptyList", valid, validationControlDecoration.getDescriptionText()));
	}

	/**
	 * Constructs a {@link AbstractExpression} instance from the contents of this element controls.
	 *
	 * @return Returns a {@link AbstractExpression} instance.
	 */
	private AbstractExpression constructMatchingRuleExpression() {
		List<AbstractExpression> activeExpressions = new ArrayList<AbstractExpression>();
		for (AbstractRuleEditingElement<?> ruleComposite : ruleElements) {
			AbstractExpression expression = ruleComposite.getExpression();
			if (null != expression) {
				activeExpressions.add(expression);
			}
		}
		AbstractExpression matchingRuleExpression = null;
		if (!activeExpressions.isEmpty()) {
			AbstractExpression[] expressions = new AbstractExpression[activeExpressions.size()];
			activeExpressions.toArray(expressions);
			matchingRuleExpression = new OrExpression(expressions);
		}
		if (null == matchingRuleExpression) {
			matchingRuleExpression = new BooleanExpression(false);
		}
		matchingRuleExpression.setAdvanced(false);
		return matchingRuleExpression;
	}

	/**
	 * Sets the enabled state of this part.
	 */
	private void updateEnabledState() {
		if (null != toolBarManager) {
			toolBarManager.getControl().setEnabled(isEditable());
		}
	}

	/**
	 * Creates tool bar for creation of new rules.
	 *
	 */
	private void createToolbar() {
		MenuAction createNewRuleMenu = new MenuAction();
		createNewRuleMenu.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
		createNewRuleMenu.setToolTipText("Add new rule");
		for (MatchingRuleType type : MatchingRuleType.values()) {
			createNewRuleMenu.addAction(new AddMatchingRuleCompositeAction(type));
		}

		toolBarManager = new ToolBarManager();
		final ToolBar toolbar = toolBarManager.createControl(getSection());
		toolBarManager.add(createNewRuleMenu);
		toolBarManager.update(true);

		createNewRuleMenu.setRunTask(new MenuAction.ToolbarDropDownTask(toolbar));

		getSection().setTextClient(toolbar);
	}

	/**
	 * Resets the contents of the controls within this part.
	 */
	private void reset() {
		for (AbstractRuleEditingElement<?> ruleElement : ruleElements) {
			ruleElement.dispose();
		}
		ruleElements.clear();
	}

	/**
	 * Adds a new rule editing element to this composite element.
	 *
	 * @param expression
	 *            {@link AbstractExpression} instance to add.
	 */
	private void addNewRuleEditingElement(AbstractExpression expression) {
		AbstractRuleEditingElement<?> ruleComposite = MatchingRulesEditingElementFactory.createRuleComposite(expression, isEditable(), validationManager);
		ruleElements.add(ruleComposite);
		listValidation.executeValidation();

		ruleComposite.createControls(main, getManagedForm().getToolkit(), true);
		ruleComposite.addModifyListener(new IDetailsModifiedListener<AbstractExpression>() {
			@Override
			public void contentModified(AbstractExpression modifiedElement) {
				commit(false);
				markDirty();
			}
		});
		ruleComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				if ((event.data instanceof AbstractRuleEditingElement) && !initializationPhase) {
					ruleElements.remove(event.data);
					listValidation.executeValidation();
					ruleProvider.setMatchingRuleExpression(constructMatchingRuleExpression());
					markDirty();

					layoutAndReflow();
				}
			}
		});

		ruleComposite.initialize();
		if (!initializationPhase) {
			commit(false);
			markDirty();
			layoutAndReflow();
		}
	}

	/**
	 * Layouts the main composite and reflows the managed form.
	 */
	private void layoutAndReflow() {
		main.layout(true, true);

		boolean reflow = false;
		// this is a hack in order to get the ScrolledPageBook when part is used in details part of
		// the details master block and reflow it
		Composite parent = getSection().getParent();
		while (parent != null) {
			if (parent instanceof ScrolledPageBook) {
				((ScrolledPageBook) parent).reflow(true);
				reflow = true;
				break;
			}
			parent = parent.getParent();
		}

		// if we are not in details part then reflow to managed form
		if (!reflow) {
			getManagedForm().reflow(true);
		}
	}

	/**
	 * This action adds a new {@link AbstractRuleEditingElement} instance depending on the
	 * {@link MatchingRuleType} type.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class AddMatchingRuleCompositeAction extends Action {

		/**
		 * {@link MatchingRuleType}.
		 */
		private final MatchingRuleType type;

		/**
		 * Default constructor.
		 *
		 * @param type
		 *            {@link MatchingRuleType}
		 */
		AddMatchingRuleCompositeAction(MatchingRuleType type) {
			this.type = type;
			setText(type.toString());
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(type.getImageKey()));
		}

		@Override
		public void run() {
			addNewRuleEditingElement(MatchingRulesEditingElementFactory.createExpression(type));
		}
	}
}
