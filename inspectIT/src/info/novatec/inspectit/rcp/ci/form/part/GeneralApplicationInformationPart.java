package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Section part for the name and description of an {@link IApplicationDefinition}.
 *
 * @author Alexander Wert
 *
 */
public class GeneralApplicationInformationPart extends SectionPart {

	/**
	 * Edit box for the name.
	 */
	private Text nameBox;

	/**
	 * Edit box for the description text.
	 */
	private Text descriptionBox;

	/**
	 * {@link FormPage} section belongs to.
	 */
	private final FormPage formPage;

	/**
	 * Default constructor.
	 *
	 * @param formPage
	 *            {@link FormPage} section belongs to.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public GeneralApplicationInformationPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		this.formPage = formPage;

		// client
		createClient(getSection(), toolkit);

		// text and description on our own
		getSection().setText("Application Definition");

	}

	/**
	 * Creates complete client.
	 *
	 * @param section
	 *            {@link Section}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createClient(Section section, FormToolkit toolkit) {
		ApplicationDefinition application = getApplication();
		Composite mainComposite = toolkit.createComposite(section);
		mainComposite.setLayout(new GridLayout(1, true));
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		section.setClient(mainComposite);

		Composite topComposite = toolkit.createComposite(mainComposite);
		topComposite.setLayout(new GridLayout(2, false));
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label nameLabel = new Label(topComposite, SWT.LEFT);
		nameLabel.setText("Name:");
		nameBox = new Text(topComposite, SWT.BORDER);
		nameBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		nameBox.setText(application.getApplicationName());
		nameBox.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				markDirty();
			}
		});

		Label descLabel = new Label(topComposite, SWT.LEFT);
		descLabel.setText("Description:");
		descLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		descriptionBox = new Text(topComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridData gData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gData.heightHint = 100;
		descriptionBox.setLayoutData(gData);
		descriptionBox.setText((null != application.getDescription()) ? application.getDescription() : "");

		descriptionBox.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				markDirty();
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);
			ApplicationDefinition application = getApplication();
			application.setApplicationName(nameBox.getText());
			application.setDescription(descriptionBox.getText());
		}
	}

	/**
	 * Retrieves current {@link ApplicationDefinition} instance under modification.
	 *
	 * @return Returns current {@link ApplicationDefinition} instance under modification.
	 */
	private ApplicationDefinition getApplication() {
		return ((ApplicationDefinitionEditorInput) formPage.getEditorInput()).getApplication();
	}

}
