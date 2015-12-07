package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.impl.OrExpression;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.AbstractRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.AbstractRuleEditingElement.RuleEditingElementModifiedListener;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Composite element for matching rules viewing, creation and modification.
 *
 * @author Alexander Wert
 *
 */
public class SimpleMatchingRulesPart extends SectionPart implements IMatchingRulesPart {

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
				if (null == MatchingRulesEditingElementFactory.getMatchingRuleType(childExpression)) {
					return false;
				}
			}
			return true;
		} else {
			return expression instanceof BooleanExpression && !((BooleanExpression) expression).isValue();
		}
	}

	/**
	 * List of {@link AbstractRuleEditingElement} comprised in this element.
	 */
	private final List<AbstractRuleEditingElement> ruleElements = new ArrayList<AbstractRuleEditingElement>();

	/**
	 * Main {@link Composite} of this element.
	 */
	private final Composite main;

	/**
	 * Indicates whether this element is in the initialization phase.
	 */
	private boolean initializationPhase = false;

	/**
	 * Label holding the description text.
	 */
	private final Label descriptionLabel;

	/**
	 * Indicates whether this form part is editable or not.
	 */
	private final boolean editable;

	/**
	 * The toolbar manager used in this part.
	 */
	private ToolBarManager toolBarManager;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            The title of the section.
	 * @param parent
	 *            parent {@link Composite}.
	 * @param managedForm
	 *            the {@link IManagedForm} to add this part to.
	 * @param editable
	 *            if false, then this part is read-only
	 */
	public SimpleMatchingRulesPart(String title, Composite parent, IManagedForm managedForm, boolean editable) {
		super(parent, managedForm.getToolkit(), Section.TITLE_BAR | Section.EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		this.editable = editable;
		this.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Color color = parent.getBackground();

		getSection().setText(title);

		ScrolledForm form = toolkit.createScrolledForm(getSection());
		form.setLayout(new GridLayout(1, false));
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		main = form.getBody();
		main.setBackground(color);
		main.setLayout(new GridLayout(AbstractRuleEditingElement.NUM_GRID_COLUMNS, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toolkit.decorateFormHeading(form.getForm());
		managedForm.addPart(this);

		getSection().setClient(form);

		descriptionLabel = toolkit.createLabel(getSection(), "Select rules that should be used to match this application:");
		descriptionLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(descriptionLabel);

		createToolbar();
		setEnabledState();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void initContent(AbstractExpression matchingRuleExpression) {
		initializationPhase = true;
		reset();
		if (canShowRule(matchingRuleExpression) && matchingRuleExpression instanceof OrExpression) {
			for (AbstractExpression expression : ((OrExpression) matchingRuleExpression).getOperands()) {
				AbstractRuleEditingElement ruleComposite = MatchingRulesEditingElementFactory.createRuleComposite(expression, editable);
				addNewRuleEditingElement(ruleComposite);
				ruleComposite.initialize(expression);
			}
		}
		main.layout(true, true);
		initializationPhase = false;
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public AbstractExpression constructMatchingRuleExpression() {
		List<AbstractExpression> activeExpressions = new ArrayList<AbstractExpression>();
		for (AbstractRuleEditingElement ruleComposite : ruleElements) {
			AbstractExpression expression = ruleComposite.constructRuleExpression();
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
		return matchingRuleExpression;
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
	public void dispose() {
		getManagedForm().removePart(this);
		getSection().dispose();
		super.dispose();
	}

	/**
	 * Sets the enabled state of this part.
	 */
	private void setEnabledState() {
		getSection().setEnabled(editable);
		toolBarManager.getControl().setEnabled(editable);
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
		for (AbstractRuleEditingElement ruleElement : ruleElements) {
			ruleElement.dispose();
		}
		ruleElements.clear();
	}

	/**
	 * Adds a new rule editing element to this composite element.
	 *
	 * @param ruleComposite
	 *            {@link AbstractRuleEditingElement} instance to add.
	 */
	private void addNewRuleEditingElement(AbstractRuleEditingElement ruleComposite) {
		ruleComposite.createControls(main, getManagedForm().getToolkit(), true);
		ruleComposite.addModifyListener(new RuleEditingElementModifiedListener() {
			@Override
			public void contentModified() {
				markDirty();
			}

			@Override
			public void elementDisposed(AbstractRuleEditingElement ruleComposite) {
				// execute update only if the rules element is not in an initialization phase
				if (!initializationPhase) {
					ruleElements.remove(ruleComposite);
					main.layout(true, true);
					markDirty();
				}
			}
		});
		ruleElements.add(ruleComposite);
	}

	/**
	 * Adds a new rule editing element to this composite element.
	 *
	 * @param matchingRuleType
	 *            {@link MatchingRuleType} specifying the type of the sub-element.
	 */
	private void addNewRuleComposite(MatchingRuleType matchingRuleType) {
		AbstractRuleEditingElement ruleComposite = MatchingRulesEditingElementFactory.createRuleComposite(matchingRuleType, editable);
		addNewRuleEditingElement(ruleComposite);
		main.layout(true, true);
		markDirty();
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
			super();
			this.type = type;
		}

		@Override
		public void run() {
			addNewRuleComposite(type);
		}

		@Override
		public String getText() {
			return type.toString();
		}
	}
}
