package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.MatchingRule;
import info.novatec.inspectit.cmr.configuration.business.IMatchingRule;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.ci.view.matchingrules.AbstractRuleEditingElement;
import info.novatec.inspectit.rcp.ci.view.matchingrules.MatchingRulesEditingElement;
import info.novatec.inspectit.rcp.ci.view.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;
import info.novatec.inspectit.rcp.ci.view.matchingrules.RuleEditingElementModifiedListener;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Section part for the definition of {@link IMatchingRule} instances.
 *
 * @author Alexander Wert
 *
 */
public class MatchingRulesPart extends SectionPart {

	/**
	 * {@link MatchingRulesEditingElement}.
	 */
	private MatchingRulesEditingElement matchingRuleComposite;

	/**
	 * Label holding the description text.
	 */
	private Label descriptionLabel;

	/**
	 * Drop down menu for the creation of new matching rules.
	 */
	private MenuAction createNewRuleMenu;

	/**
	 * Default constructor.
	 *
	 * @param title
	 *            title of the section.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public MatchingRulesPart(String title, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);

		// client
		createClient(toolkit);
		// text and description on our own
		getSection().setText(title);
	}

	/**
	 * Creates complete client.
	 *
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createClient(FormToolkit toolkit) {

		matchingRuleComposite = new MatchingRulesEditingElement(getSection(), null, getSection().getBackground());
		matchingRuleComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		matchingRuleComposite.addModifyListener(new RuleEditingElementModifiedListener() {

			@Override
			public void contentModified() {
				markDirty();

			}

			@Override
			public void elementDisposed(AbstractRuleEditingElement ruleComposite) {

			}
		});
		getSection().setClient(matchingRuleComposite.getForm());

		descriptionLabel = toolkit.createLabel(getSection(), "Select rules that should be used to match this application:");
		descriptionLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(descriptionLabel);

		createToolbar();
	}

	/**
	 * Creates tool bar for creation of new rules.
	 *
	 */
	private void createToolbar() {

		createNewRuleMenu = new MenuAction();
		createNewRuleMenu.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
		createNewRuleMenu.setToolTipText("Add new rule");
		for (MatchingRuleType type : MatchingRuleType.values()) {
			createNewRuleMenu.addAction(new AddMatchingRuleCompositeAction(type));
		}

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		final ToolBar toolbar = toolBarManager.createControl(getSection());
		toolBarManager.add(createNewRuleMenu);
		toolBarManager.update(true);

		createNewRuleMenu.setRunTask(new MenuAction.ToolbarDropDownTask(toolbar));

		getSection().setTextClient(toolbar);
	}

	/**
	 * Changes input for this section.
	 *
	 * @param matchingRule
	 *            new {@link IMatchingRule} to use as initialization
	 */
	public void changeInput(IMatchingRule matchingRule) {
		matchingRuleComposite.reinitialize(matchingRule);
	}

	/**
	 * Constructs a matching rule from the contents of the underlying widgets.
	 *
	 * @return new {@link IMatchingRule}.
	 */
	public MatchingRule constructMatchingRule() {
		return matchingRuleComposite.constructMatchingRule();
	}

	/**
	 * Sets the description text for this section.
	 *
	 * @param description
	 *            new description text.
	 */
	public void setDescriptionText(String description) {
		descriptionLabel.setText(description);
		getSection().layout(true);
	}

	/**
	 * Sets the visibility status of the rules controls.
	 *
	 * @param visible
	 *            visibility status. If true, the controls are visible.
	 */
	public void setRulesVisible(boolean visible) {
		matchingRuleComposite.setVisible(visible);
		createNewRuleMenu.setEnabled(visible);
	}

	/**
	 * Adds a {@link RuleEditingElementModifiedListener} that is notified when
	 * {@link MatchingRulesEditingElement} elements are modified or disposed.
	 *
	 * @param listener
	 *            {@link RuleEditingElementModifiedListener} to add.
	 */
	public void addModifyListener(RuleEditingElementModifiedListener listener) {
		matchingRuleComposite.addModifyListener(listener);
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
			matchingRuleComposite.addNewRuleComposite(type);
		}

		@Override
		public String getText() {
			return type.toString();
		}
	}
}
