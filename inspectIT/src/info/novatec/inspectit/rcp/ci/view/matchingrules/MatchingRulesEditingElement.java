package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.ci.business.BooleanExpression;
import info.novatec.inspectit.ci.business.Expression;
import info.novatec.inspectit.ci.business.MatchingRule;
import info.novatec.inspectit.ci.business.OrExpression;
import info.novatec.inspectit.cmr.configuration.business.IExpression;
import info.novatec.inspectit.cmr.configuration.business.IMatchingRule;
import info.novatec.inspectit.rcp.ci.view.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Composite element for matching rules viewing, creation and modification.
 *
 * @author Alexander Wert
 *
 */
public class MatchingRulesEditingElement {
	/**
	 * List of {@link AbstractRuleEditingElement} comprised in this element.
	 */
	private final List<AbstractRuleEditingElement> ruleElements = new ArrayList<AbstractRuleEditingElement>();

	/**
	 * Main {@link Composite} of this element.
	 */
	private final Composite main;

	/**
	 * Main form of this element.
	 */
	private final ScrolledForm form;

	/**
	 * {@link FormToolkit} to use for creation of sub elements.
	 */
	private final FormToolkit toolkit;

	/**
	 * Background color for the titles of the sub-elements.
	 */
	private final Color titleBackground;

	/**
	 * List of listeners to be notified on modification.
	 */
	private final List<RuleEditingElementModifiedListener> listeners = new ArrayList<RuleEditingElementModifiedListener>();

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            parent {@link Composite}
	 * @param matchingRule
	 *            {@link MatchingRule} instance to be used for initialization of the contents. Can
	 *            be null, then no initialization will be performed.
	 * @param titleBackgroundColor
	 *            Color for the title background.
	 */
	public MatchingRulesEditingElement(Composite parent, MatchingRule matchingRule, Color titleBackgroundColor) {
		this.titleBackground = titleBackgroundColor;
		Color color = parent.getBackground();
		final ManagedForm managedForm = new ManagedForm(parent);
		toolkit = managedForm.getToolkit();
		toolkit.setBackground(color);
		form = managedForm.getForm();
		form.setLayout(new GridLayout(1, false));
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		managedForm.getToolkit().decorateFormHeading(form.getForm());
		main = form.getBody();
		main.setBackground(color);
		main.setLayout(new GridLayout(1, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (null != matchingRule) {
			initialize(matchingRule);
		}

	}

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            parent {@link Composite}
	 * @param matchingRule
	 *            {@link MatchingRule} instance to be used for initialization of the contents. Can
	 *            be null, then no initialization will be performed.
	 */
	public MatchingRulesEditingElement(Composite parent, MatchingRule matchingRule) {
		this(parent, matchingRule, null);

	}

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            parent {@link Composite}
	 */
	public MatchingRulesEditingElement(Composite parent) {
		this(parent, null);
	}

	/**
	 * Adds a {@link RuleEditingElementModifiedListener}.
	 *
	 * @param listener
	 *            {@link RuleEditingElementModifiedListener} instance to add.
	 */
	public void addModifyListener(RuleEditingElementModifiedListener listener) {
		listeners.add(listener);

	}

	/**
	 * Constructs a {@link MatchingRule} instance from the contents of this element controls.
	 *
	 * @return Returns a {@link MatchingRule} instance.
	 */
	public MatchingRule constructMatchingRule() {
		List<Expression> activeExpressions = new ArrayList<Expression>();
		for (AbstractRuleEditingElement ruleComposite : ruleElements) {
			if (null != ruleComposite.constructRuleExpression()) {
				activeExpressions.add(ruleComposite.constructRuleExpression());
			}
		}
		MatchingRule matchingRule = null;
		if (!activeExpressions.isEmpty()) {
			matchingRule = new MatchingRule();
			Expression[] expressions = new Expression[activeExpressions.size()];
			activeExpressions.toArray(expressions);
			OrExpression orExpression = new OrExpression(expressions);
			matchingRule.setExpression(orExpression);
		}
		if (null == matchingRule) {
			matchingRule = new MatchingRule(new BooleanExpression(false));
		}
		return matchingRule;
	}

	/**
	 *
	 * @return The {@link Form} of this element.
	 */
	public ScrolledForm getForm() {
		return form;
	}

	/**
	 * Sets {@link GridData} for this element.
	 *
	 * @param gridData
	 *            {@link GridData}.
	 */
	public void setLayoutData(GridData gridData) {
		form.setLayoutData(gridData);
	}

	/**
	 * Resets all sub-elements.
	 */
	public void reset() {
		for (AbstractRuleEditingElement ruleComposite : ruleElements) {
			ruleComposite.getControl().dispose();
		}
		ruleElements.clear();
	}

	/**
	 * Initializes the contents of the sub-elements according to the {@link IMatchingRule}.
	 *
	 * @param matchingRule
	 *            {@link IMatchingRule} instance describing the content.
	 */
	public void initialize(IMatchingRule matchingRule) {

		IExpression topLevelExpression = matchingRule.getExpression();
		if (topLevelExpression instanceof OrExpression) {
			for (Expression expression : ((OrExpression) topLevelExpression).getOperands()) {
				AbstractRuleEditingElement ruleComposite = MatchingRulesEditingElementFactory.createRuleComposite(expression);
				addNewRuleEditingElement(ruleComposite);
				ruleComposite.initialize(expression);
			}
		}
		main.layout(true, true);
	}

	/**
	 * Adds a new rule editing element to this composite element.
	 *
	 * @param ruleComposite
	 *            {@link AbstractRuleEditingElement} instance to add.
	 */
	private void addNewRuleEditingElement(AbstractRuleEditingElement ruleComposite) {
		if (null != titleBackground) {
			ruleComposite.setTitleBackgroundColor(titleBackground);
		}
		ruleComposite.createControls(main, toolkit);

		for (RuleEditingElementModifiedListener listener : listeners) {
			ruleComposite.addModifyListener(listener);
		}
		ruleComposite.addModifyListener(new RuleEditingElementModifiedListener() {

			@Override
			public void contentModified() {
				// TODO Auto-generated method stub

			}

			@Override
			public void elementDisposed(AbstractRuleEditingElement ruleComposite) {
				ruleElements.remove(ruleComposite);
				main.layout(true, true);
				for (RuleEditingElementModifiedListener listener : listeners) {
					listener.contentModified();
				}

			}
		});
		ruleElements.add(ruleComposite);

	}

	/**
	 * Reinitializes the contents of the sub-elements according to the {@link IMatchingRule}.
	 *
	 * @param matchingRule
	 *            {@link IMatchingRule} instance describing the content.
	 */
	public void reinitialize(IMatchingRule matchingRule) {
		reset();
		initialize(matchingRule);
	}

	/**
	 * Marks the receiver as visible if the argument is true, and marks it invisible otherwise.
	 *
	 * @param visible
	 *            the new visibility state
	 */
	public void setVisible(boolean visible) {
		for (AbstractRuleEditingElement ruleComposite : ruleElements) {
			ruleComposite.setVisible(visible);
		}
	}

	/**
	 * Adds a new rule editing element to this composite element.
	 *
	 * @param matchingRuleType
	 *            {@link MatchingRuleType} specifying the type of the sub-element.
	 */
	public void addNewRuleComposite(MatchingRulesEditingElementFactory.MatchingRuleType matchingRuleType) {
		AbstractRuleEditingElement ruleComposite = MatchingRulesEditingElementFactory.createRuleComposite(matchingRuleType);
		addNewRuleEditingElement(ruleComposite);
		main.layout(true, true);
		for (RuleEditingElementModifiedListener listener : listeners) {
			listener.contentModified();
		}

	}

}
