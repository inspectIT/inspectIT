package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.ci.form.part.EnvironmentSettingsPart;
import info.novatec.inspectit.rcp.ci.form.part.PlatformSensorSelectionPart;
import info.novatec.inspectit.rcp.ci.form.part.ProfileSelectionPart;
import info.novatec.inspectit.rcp.ci.form.part.SensorOptionsPart;
import info.novatec.inspectit.rcp.util.SelectionProviderAdapter;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
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
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Page for the environment general settings.
 * 
 * @author Ivan Senic
 * 
 */
public class EnvironmentSettingsPage extends FormPage implements IPropertyListener {

	/**
	 * Id of the page.
	 */
	private static final String ID = EnvironmentSettingsPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Environment Settings";

	/**
	 * {@link SelectionProviderAdapter} to handle site selection.
	 */
	private SelectionProviderAdapter selectionProviderAdapter;

	/**
	 * Default constructor.
	 * 
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public EnvironmentSettingsPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(TITLE);
		form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_TOOL));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		// set the selection adapter and make site selection be the the input that is also
		// IEnvironmentProvider
		EnvironmentEditorInput input = (EnvironmentEditorInput) getEditorInput();
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

		// body
		Composite body = form.getBody();
		body.setLayout(new GridLayout(2, true));

		Composite left = toolkit.createComposite(body);
		left.setLayoutData(new GridData(GridData.FILL_BOTH));
		left.setLayout(new GridLayout(1, true));

		Composite right = toolkit.createComposite(body);
		right.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout(1, true);
		gl.verticalSpacing = 20;
		right.setLayout(gl);

		ProfileSelectionPart profileSelectionPart = new ProfileSelectionPart(this, left, toolkit, Section.TITLE_BAR | Section.EXPANDED);
		profileSelectionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		managedForm.addPart(profileSelectionPart);

		EnvironmentSettingsPart environmentSettingsPart = new EnvironmentSettingsPart(this, right, toolkit, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		environmentSettingsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(environmentSettingsPart);

		PlatformSensorSelectionPart platformSensorSelectionPart = new PlatformSensorSelectionPart(this, right, toolkit, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		platformSensorSelectionPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(platformSensorSelectionPart);

		SensorOptionsPart sensorOptionsPart = new SensorOptionsPart(this, right, toolkit, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		sensorOptionsPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		managedForm.addPart(sensorOptionsPart);

		getEditor().addPropertyListener(this);
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
