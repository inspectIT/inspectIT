package info.novatec.inspectit.rcp.editor.root;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.composite.BreadcrumbTitleComposite;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.preferences.FormPreferencePanel;
import info.novatec.inspectit.rcp.editor.preferences.IPreferencePanel;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An implementation of a root editor which uses a form to create a nicer view.
 * 
 * @author Patrice Bouillet
 * 
 */
public class FormRootEditor extends AbstractRootEditor {

	/**
	 * The identifier of the {@link FormRootEditor}.
	 */
	public static final String ID = "inspectit.editor.formrooteditor";

	/**
	 * The form toolkit which defines the colors etc.
	 */
	private FormToolkit toolkit;

	/**
	 * The form of the view.
	 */
	private Form form;

	/**
	 * {@link BreadcrumbTitleComposite}.
	 */
	private BreadcrumbTitleComposite breadcrumbTitleComposite;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createView(Composite parent) {
		// create the toolkit
		this.toolkit = new FormToolkit(parent.getDisplay());

		// create the preference panel with the callback
		IPreferencePanel preferencePanel = new FormPreferencePanel(toolkit);
		// set the preference panel
		setPreferencePanel(preferencePanel);

		// create the form
		form = toolkit.createForm(parent);
		form.getBody().setLayout(new GridLayout());
		// decorate the heading to make it look better
		toolkit.decorateFormHeading(form);

		// create breadcrumb composite
		RepositoryDefinition repositoryDefinition = getInputDefinition().getRepositoryDefinition();
		PlatformIdent platformIdent = repositoryDefinition.getCachedDataService().getPlatformIdentForId(getInputDefinition().getIdDefinition().getPlatformId());
		breadcrumbTitleComposite = new BreadcrumbTitleComposite(form.getHead(), SWT.NONE);
		breadcrumbTitleComposite.setRepositoryDefinition(repositoryDefinition);
		breadcrumbTitleComposite.setAgent(TextFormatter.getAgentDescription(platformIdent), InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT));
		breadcrumbTitleComposite.setGroup(getInputDefinition().getEditorPropertiesData().getSensorName(), getInputDefinition().getEditorPropertiesData().getSensorImage());
		breadcrumbTitleComposite.setView(getInputDefinition().getEditorPropertiesData().getViewName(), getInputDefinition().getEditorPropertiesData().getViewImage());
		form.setHeadClient(breadcrumbTitleComposite);

		// create an preference area if the subviews are requesting it
		preferencePanel.createPartControl(form.getBody(), getSubView().getPreferenceIds(), getInputDefinition(), breadcrumbTitleComposite.getToolBarManager());

		// go further with creating the subview(s)
		getSubView().createPartControl(form.getBody(), toolkit);
		getSubView().getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		// dispose of the toolkit
		toolkit.dispose();
		breadcrumbTitleComposite.dispose();
	}

	/**
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}

	/**
	 * Gets {@link #breadcrumbTitleComposite}.
	 * 
	 * @return {@link #breadcrumbTitleComposite}
	 */
	public BreadcrumbTitleComposite getBreadcrumbTitleComposite() {
		return breadcrumbTitleComposite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateEditorName(String name) {
		EditorPropertiesData editorPropertiesData = getInputDefinition().getEditorPropertiesData();
		if (Objects.equals(editorPropertiesData.getPartName(), editorPropertiesData.getSensorName())) {
			breadcrumbTitleComposite.setGroup(name, editorPropertiesData.getSensorImage());
		} else {
			breadcrumbTitleComposite.setView(name, editorPropertiesData.getViewImage());
		}
		super.updateEditorName(name);
	}

}
