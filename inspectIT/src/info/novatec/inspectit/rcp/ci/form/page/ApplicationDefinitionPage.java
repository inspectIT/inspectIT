package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.ci.business.ApplicationDefinition;
import info.novatec.inspectit.ci.business.BusinessTransactionDefinition;
import info.novatec.inspectit.ci.business.MatchingRule;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessTransactionDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import info.novatec.inspectit.rcp.ci.form.part.BusinessTransactionDefinitionPart;
import info.novatec.inspectit.rcp.ci.form.part.GeneralApplicationInformationPart;
import info.novatec.inspectit.rcp.ci.form.part.MatchingRulesPart;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Edit page for {@link ApplicationDefinition}.
 *
 * @author Alexander Wert
 *
 */
public class ApplicationDefinitionPage extends FormPage implements IPropertyListener {
	/**
	 * Id of the page.
	 */
	private static final String ID = ApplicationDefinitionPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Application Definition";

	/**
	 * {@link ApplicationDefinition} instance to be edited.
	 */
	private IApplicationDefinition application;

	/**
	 * Section part for general application information (i.e. application name and description).
	 */
	private GeneralApplicationInformationPart applicationDefinitionPart;

	/**
	 * Section part for the definition of application matching rules.
	 */
	private MatchingRulesPart applicationMatchingRulesPart;

	/**
	 * Section part showing the business transaction definitions and allowing to define new business
	 * transaction definitions.
	 */
	private BusinessTransactionDefinitionPart businessTransactionDefinitionPart;

	/**
	 * Section part for the definition of business transaction matching rules.
	 */
	private MatchingRulesPart businessTransactionMatchingRulesPart;

	/**
	 * Id of the currently selected business transaction.
	 */
	private Long selectedBusinessTransactionId = null;

	/**
	 * Main form of this editor page.
	 */
	private ScrolledForm mainForm;

	/**
	 * Default constructor.
	 *
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public ApplicationDefinitionPage(FormEditor editor) {
		super(editor, ID, TITLE);
		editor.addPropertyListener(this);

		ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) getEditor().getEditorInput();
		this.application = input.getApplication();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		mainForm = managedForm.getForm();
		if (null != application) {
			mainForm.setText(TITLE + " for " + application.getApplicationName());
		} else {
			mainForm.setText(TITLE);
		}

		mainForm.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_PUZZLE));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(mainForm.getForm());

		// body
		Composite body = mainForm.getBody();
		body.setLayout(new GridLayout(1, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));

		SashForm sashForm = new SashForm(body, SWT.HORIZONTAL);

		GridLayout mainLayout = new GridLayout(1, true);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		sashForm.setLayout(mainLayout);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		// left part - application definition
		Composite left = toolkit.createComposite(sashForm);
		left.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout leftLayout = new GridLayout(1, true);
		leftLayout.verticalSpacing = 20;

		left.setLayout(leftLayout);

		applicationDefinitionPart = new GeneralApplicationInformationPart(this, left, toolkit, Section.TITLE_BAR | Section.EXPANDED);
		applicationDefinitionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(applicationDefinitionPart);

		applicationMatchingRulesPart = new MatchingRulesPart("Application Mapping", left, toolkit, Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE);
		applicationMatchingRulesPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		applicationMatchingRulesPart.changeInput(application.getMatchingRule());
		managedForm.addPart(applicationMatchingRulesPart);

		// right part - business transaction definitions
		Composite rightMain = toolkit.createComposite(sashForm);
		rightMain.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout rightLayout = new GridLayout(1, true);
		rightLayout.verticalSpacing = 20;
		rightMain.setLayout(rightLayout);

		SashForm right = new SashForm(rightMain, SWT.VERTICAL);
		rightLayout = new GridLayout(1, true);
		rightLayout.verticalSpacing = 20;
		right.setLayout(rightLayout);
		right.setLayoutData(new GridData(GridData.FILL_BOTH));

		businessTransactionDefinitionPart = new BusinessTransactionDefinitionPart(this, right, toolkit, Section.TITLE_BAR | Section.EXPANDED);
		businessTransactionDefinitionPart.addSelectionChangeListener(new BusinessTransactionSelectionChangeListener());
		businessTransactionDefinitionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		managedForm.addPart(businessTransactionDefinitionPart);

		businessTransactionMatchingRulesPart = new MatchingRulesPart("Business Transaction Mapping", right, toolkit, Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE);
		businessTransactionMatchingRulesPart.setDescriptionText("No Business Transaction selected.");
		businessTransactionMatchingRulesPart.setRulesVisible(false);
		businessTransactionMatchingRulesPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		managedForm.addPart(businessTransactionMatchingRulesPart);

		right.setWeights(new int[] { 1, 2 });
	}

	/**
	 * Applies changes.
	 */
	public void doSave() {
		IBusinessTransactionDefinition selectedBusinessTransaction = getCurrentlySelectedBusinessTransaction();
		if (null != selectedBusinessTransaction) {
			MatchingRule businessTxMatchingrule = businessTransactionMatchingRulesPart.constructMatchingRule();
			selectedBusinessTransaction.setMatchingRule(businessTxMatchingrule);
		}

		MatchingRule applicationMatchingRule = applicationMatchingRulesPart.constructMatchingRule();
		application.setMatchingRule(applicationMatchingRule);
	}

	/**
	 * Executed on a changed selection of the business transaction. As a consequence updates the
	 * {@link #businessTransactionMatchingRulesPart}.
	 *
	 * @param bTxSelection
	 *            new selection
	 */
	private void businessTransactionSelectionChanged(IBusinessTransactionDefinition bTxSelection) {
		IBusinessTransactionDefinition previouslySelectedBusinessTx = getCurrentlySelectedBusinessTransaction();
		if (null != previouslySelectedBusinessTx) {
			MatchingRule rule = businessTransactionMatchingRulesPart.constructMatchingRule();
			previouslySelectedBusinessTx.setMatchingRule(rule);

		}
		selectedBusinessTransactionId = bTxSelection.getId();

		if (null == selectedBusinessTransactionId) {
			businessTransactionMatchingRulesPart.setRulesVisible(false);
			businessTransactionMatchingRulesPart.setDescriptionText("No Business Transaction selected.");
		} else if (selectedBusinessTransactionId == BusinessTransactionDefinition.DEFAULT_ID) {
			businessTransactionMatchingRulesPart.setRulesVisible(false);
			businessTransactionMatchingRulesPart.setDescriptionText("The default 'Unknown Transaction' is selected. The matching rules cannot be modified for this transaction definition!");
		} else {
			IBusinessTransactionDefinition selectedBusinessTransaction = getCurrentlySelectedBusinessTransaction();
			if (null != selectedBusinessTransaction) {
				businessTransactionMatchingRulesPart.changeInput(selectedBusinessTransaction.getMatchingRule());
				businessTransactionMatchingRulesPart.setRulesVisible(true);
				businessTransactionMatchingRulesPart.setDescriptionText("Select rules that should be used to match the selected business transaction:");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Manually set focus to form body, otherwise is the tool-bar in focus.
	 */
	@Override
	public void setFocus() {
		getManagedForm().getForm().getBody().setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) getEditor().getEditorInput();
			setInput(input);
			application = input.getApplication();

			if (null != application) {
				mainForm.setText(TITLE + " for " + application.getApplicationName());
				applicationMatchingRulesPart.changeInput(application.getMatchingRule());
			} else {
				mainForm.setText(TITLE);
			}

			mainForm.reflow(true);
		}
	}

	/**
	 * Retrieves the instance of the currently selected {@link IBusinessTransactionDefinition}.
	 *
	 * @return Returns the instance of the currently selected {@link IBusinessTransactionDefinition}
	 *         .
	 */
	private IBusinessTransactionDefinition getCurrentlySelectedBusinessTransaction() {
		try {
			if (null == selectedBusinessTransactionId) {
				return null;
			} else {
				return application.getBusinessTransactionDefinition(selectedBusinessTransactionId);
			}
		} catch (BusinessException e) {
			return null;
		}
	}

	/**
	 * Selection change listener for the business transaction list.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class BusinessTransactionSelectionChangeListener implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			StructuredSelection ss = (StructuredSelection) event.getSelection();
			if (ss.getFirstElement() instanceof IBusinessTransactionDefinition) {
				businessTransactionSelectionChanged((IBusinessTransactionDefinition) ss.getFirstElement());
			}
		}

	}

}
