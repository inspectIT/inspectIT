package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.part.ExcludeRulesPart;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Page for exclude rules settings.
 * 
 * @author Ivan Senic
 * 
 */
public class ExcludeRulesPage extends FormPage {

	/**
	 * Id of the page.
	 */
	private static final String ID = ExcludeRulesPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Exclude Rules";

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
	}

}
