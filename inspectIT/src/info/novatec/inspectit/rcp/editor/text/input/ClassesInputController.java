package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.util.SafeExecutor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class represents the textual view of the {@link ClassLoadingInformation} sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ClassesInputController extends AbstractTextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.classes";

	/**
	 * The name for the section.
	 */
	private static final String SECTION_CLASSES = "Classes";

	/**
	 * The template of the {@link ClassLoadingInformationData} object.
	 */
	private ClassLoadingInformationData classLoadingObj;

	/**
	 * The label for loaded classes.
	 */
	private Label loadedClassCount;

	/**
	 * The label for total loaded classes.
	 */
	private Label totalLoadedClassCount;

	/**
	 * The label for unloaded classes.
	 */
	private Label unloadedClassCount;

	/**
	 * The global data access service.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		classLoadingObj = new ClassLoadingInformationData();
		classLoadingObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_CLASSES);

		if (sections.containsKey(SECTION_CLASSES)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_CLASSES, "Current loaded classes: ");
			loadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), "n/a");
			loadedClassCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_CLASSES, "Total loaded classes: ");
			totalLoadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), "n/a");
			totalLoadedClassCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_CLASSES, "Total unloaded classes: ");
			unloadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), "n/a");
			unloadedClassCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		final ClassLoadingInformationData data = (ClassLoadingInformationData) dataAccessService.getLastDataObject(classLoadingObj);

		if (null != data) {
			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					// updates the labels
					int count = data.getCount();
					loadedClassCount.setText(NumberFormatter.formatInteger(data.getTotalLoadedClassCount() / count));
					totalLoadedClassCount.setText(NumberFormatter.formatLong(data.getTotalTotalLoadedClassCount() / count));
					unloadedClassCount.setText(NumberFormatter.formatLong(data.getTotalUnloadedClassCount() / count));
				}
			}, loadedClassCount, totalLoadedClassCount, unloadedClassCount);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
