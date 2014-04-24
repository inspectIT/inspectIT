package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ProfileEditorInput;
import info.novatec.inspectit.rcp.ci.form.part.SensorAssignmentMasterBlock;
import info.novatec.inspectit.rcp.util.SelectionProviderAdapter;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
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
 * Page for method sensor definitions.
 * 
 * @author Ivan Senic
 * 
 */
public class MethodSensorDefinitionsPage extends FormPage implements IPropertyListener {

	/**
	 * Id of the page.
	 */
	private static final String ID = MethodSensorDefinitionsPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Sensor Definitions";

	/**
	 * Method sensor master block.
	 */
	private SensorAssignmentMasterBlock sensorAssignmentMasterBlock;

	/**
	 * {@link SelectionProviderAdapter} for the site.
	 */
	private SelectionProviderAdapter selectionProviderAdapter;

	/**
	 * Default constructor.
	 * 
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public MethodSensorDefinitionsPage(FormEditor editor) {
		super(editor, ID, TITLE);
		this.sensorAssignmentMasterBlock = new SensorAssignmentMasterBlock(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(TITLE);
		form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_TIMER));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

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

		sensorAssignmentMasterBlock.createContent(managedForm);

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
