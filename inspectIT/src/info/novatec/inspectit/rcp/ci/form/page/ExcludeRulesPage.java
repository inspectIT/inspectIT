package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ProfileEditorInput;
import info.novatec.inspectit.rcp.ci.form.part.ExcludeRulesPart;
import info.novatec.inspectit.rcp.util.SelectionProviderAdapter;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Page for exclude rules settings.
 * 
 * @author Ivan Senic
 * 
 */
public class ExcludeRulesPage extends FormPage implements IPropertyListener {

	/**
	 * Id of the page.
	 */
	private static final String ID = ExcludeRulesPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Exclude Rules";

	/**
	 * {@link SelectionProviderAdapter} for the site.
	 */
	private SelectionProviderAdapter selectionProviderAdapter;

	/**
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public ExcludeRulesPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(TITLE);
		form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS_EXCLUDE));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		Composite body = form.getBody();
		body.setLayout(new GridLayout(1, true));

		ExcludeRulesPart excludeRulesPart = new ExcludeRulesPart(this, body, toolkit);
		managedForm.addPart(excludeRulesPart);

		// set the selection adapter and make site selection be the the input that is also
		// IProfileProvider
		ProfileEditorInput input = (ProfileEditorInput) getEditorInput();
		selectionProviderAdapter = new SelectionProviderAdapter();
		getSite().setSelectionProvider(selectionProviderAdapter);
		selectionProviderAdapter.setSelection(new StructuredSelection(input));

		// tool-bar
		CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(getSite().getWorkbenchWindow(), "", ActionFactory.RENAME.getCommandId(), SWT.PUSH);
		contributionParameters.icon = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_EDIT);
		contributionParameters.label = "Rename";
		CommandContributionItem editCommandContribution = new CommandContributionItem(contributionParameters);
		form.getToolBarManager().add(editCommandContribution);
		form.getToolBarManager().update(false);

		getEditor().addPropertyListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			selectionProviderAdapter.setSelection(new StructuredSelection(getEditor().getEditorInput()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		getEditor().removePropertyListener(this);
		super.dispose();
	}

}
