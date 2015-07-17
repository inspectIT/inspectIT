package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * General implementation of {@link TextInputController}, where most of the methods are not doing
 * anything. Classes that extend should override the methods they want to define the proper
 * behavior.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractTextInputController implements TextInputController {

	/**
	 * The input definition.
	 */
	private InputDefinition inputDefinition;

	/**
	 * The {@link HashMap} containing the different sections.
	 */
	protected Map<String, Composite> sections = new HashMap<String, Composite>();

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		Assert.isNotNull(inputDefinition);

		this.inputDefinition = inputDefinition;
	}

	/**
	 * Returns the input definition.
	 * 
	 * @return The input definition.
	 */
	protected InputDefinition getInputDefinition() {
		Assert.isNotNull(inputDefinition);

		return inputDefinition;
	}

	/**
	 * Adds a section to bundle some content.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The section title
	 */
	protected void addSection(Composite parent, FormToolkit toolkit, String sectionTitle) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR);
		section.setText(sectionTitle);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Composite sectionComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginLeft = 5;
		gridLayout.marginTop = 5;
		sectionComposite.setLayout(gridLayout);
		section.setClient(sectionComposite);

		if (!sections.containsKey(sectionTitle)) {
			sections.put(sectionTitle, sectionComposite);
		}
	}

	/**
	 * Adds an item to the specified section.
	 * 
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The section title.
	 * @param text
	 *            The text which will be shown.
	 */
	protected void addItemToSection(FormToolkit toolkit, String sectionTitle, String text) {
		if (sections.containsKey(sectionTitle)) {
			Label label = toolkit.createLabel(sections.get(sectionTitle), text);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		}
	}

	/**
	 * Adds an item to the specified section.
	 * 
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The section title.
	 * @param text
	 *            The text which will be shown.
	 * @param minColumnWidth
	 *            the minimum width of the column.
	 */
	protected void addItemToSection(FormToolkit toolkit, String sectionTitle, String text, int minColumnWidth) {
		if (sections.containsKey(sectionTitle)) {
			Label label = toolkit.createLabel(sections.get(sectionTitle), text, SWT.LEFT);
			label.setLayoutData(new GridData(minColumnWidth, SWT.DEFAULT));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends DefaultData> data) {
	}

}
