package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.ci.form.part.AgentMappingPart;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Page for agent mapping.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentMappingPage extends FormPage {

	/**
	 * Id of the page.
	 */
	private static final String ID = AgentMappingPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Agent Mapping";

	/**
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public AgentMappingPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(TITLE);
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		Composite body = form.getBody();
		body.setLayout(new GridLayout(1, true));

		AgentMappingPart agentMappingPart = new AgentMappingPart(this, body, toolkit);
		managedForm.addPart(agentMappingPart);
	}
}
