package rocks.inspectit.ui.rcp.ci.form.page;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.JmxMasterDetailsBlock;

/**
 * Page for JMX beans definitions.
 *
 * @author Ivan Senic
 *
 */
public class JmxBeanDefinitionsPage extends FormPage {

	/**
	 * Id of the page.
	 */
	private static final String ID = JmxBeanDefinitionsPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "JMX Bean Definitions";

	/**
	 * JMX master block.
	 */
	private final JmxMasterDetailsBlock jmxMasterBlock;

	/**
	 * Default constructor.
	 *
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public JmxBeanDefinitionsPage(FormEditor editor) {
		super(editor, ID, TITLE);
		this.jmxMasterBlock = new JmxMasterDetailsBlock(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(TITLE);
		form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BEAN));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		jmxMasterBlock.createContent(managedForm);
	}

}
