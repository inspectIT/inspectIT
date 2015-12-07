package info.novatec.inspectit.rcp.ci.wizard.page;

import info.novatec.inspectit.ci.business.MatchingRule;
import info.novatec.inspectit.cmr.configuration.business.IMatchingRule;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.view.matchingrules.AbstractRuleEditingElement;
import info.novatec.inspectit.rcp.ci.view.matchingrules.MatchingRulesEditingElement;
import info.novatec.inspectit.rcp.ci.view.matchingrules.MatchingRulesEditingElementFactory;
import info.novatec.inspectit.rcp.ci.view.matchingrules.RuleEditingElementModifiedListener;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A Wizard page for defining a matching rule for applications and business transactions.
 * 
 * @author Alexander Wert
 *
 */
public class DefineMatchingRuleWizardPage extends WizardPage {
	/**
	 * Default message.
	 */
	private final String defaultMessage;

	/**
	 * {@link MatchingRulesEditingElement}.
	 */
	private MatchingRulesEditingElement ruleComposite;

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *            title of the wizard page.
	 * @param defaultMessage
	 *            default message text.
	 */
	public DefineMatchingRuleWizardPage(String title, String defaultMessage) {
		super(title);
		setTitle(title);
		setMessage(defaultMessage);
		this.defaultMessage = defaultMessage;

	}

	@Override
	public void createControl(final Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite addRuleComposite = new Composite(main, SWT.NONE);
		addRuleComposite.setLayout(new GridLayout(3, false));
		addRuleComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));

		Label label = new Label(addRuleComposite, SWT.NONE);
		label.setText("Select rules for application mapping: ");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Combo matchingTypeSelectionCombo = new Combo(addRuleComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		String[] items = new String[MatchingRulesEditingElementFactory.MatchingRuleType.values().length];
		for (int i = 0; i < MatchingRulesEditingElementFactory.MatchingRuleType.values().length; i++) {
			items[i] = MatchingRulesEditingElementFactory.MatchingRuleType.values()[i].toString();
		}
		matchingTypeSelectionCombo.setItems(items);
		matchingTypeSelectionCombo.select(0);

		matchingTypeSelectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		final Button addButton = new Button(addRuleComposite, SWT.PUSH);
		addButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		addButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		addButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = matchingTypeSelectionCombo.getSelectionIndex();
				MatchingRulesEditingElementFactory.MatchingRuleType matchingRuleType = MatchingRulesEditingElementFactory.MatchingRuleType.values()[idx];

				ruleComposite.addNewRuleComposite(matchingRuleType);
				main.layout(true, true);
				ruleComposite.getForm().reflow(true);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		ruleComposite = new MatchingRulesEditingElement(main);
		ruleComposite.addModifyListener(new RuleEditingElementModifiedListener() {

			@Override
			public void contentModified() {
				setPageComplete(isPageComplete());
				setPageMessage();
			}

			@Override
			public void elementDisposed(AbstractRuleEditingElement ruleComposite) {

			}
		});
		setControl(main);
		setPageMessage();
	}

	/**
	 * Sets the message based on the page selections.
	 */
	private void setPageMessage() {
		if (!isPageComplete()) {
			setMessage("At least one rule must be configured!", ERROR);
			return;
		}

		setMessage(defaultMessage);
	}

	@Override
	public boolean isPageComplete() {
		return null != ruleComposite && null != ruleComposite.constructMatchingRule();
	}

	/**
	 * Constructs a {@link IMatchingRule} instance from the widget input.
	 * 
	 * @return a {@link IMatchingRule} instance.
	 */
	public MatchingRule constructMatchingRule() {
		return ruleComposite.constructMatchingRule();
	}

}
