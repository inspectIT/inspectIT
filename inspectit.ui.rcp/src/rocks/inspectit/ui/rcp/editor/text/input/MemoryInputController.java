package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class represents the textual view of the {@link MemoryInformation} sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class MemoryInputController extends AbstractTextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.memory";

	/**
	 * The name of the section.
	 */
	private static final String SECTION_MEMORY = "Memory";

	/**
	 * The string representing that something is not available.
	 */
	private static final String NOT_AVAILABLE = "N/A";

	/**
	 * The template of the {@link MemoryInformationData} object.
	 */
	private MemoryInformationData memoryObj;

	/**
	 * The template of the {@link SystemInformationData} object.
	 */
	private SystemInformationData systemObj;

	/**
	 * The label for free physical memory.
	 */
	private Label freePhysMemory;

	/**
	 * The label for free swap space.
	 */
	private Label freeSwapSpace;

	/**
	 * The label for committed heap memory size.
	 */
	private Label committedHeapMemorySize;

	/**
	 * The label for committed non-heap memory size.
	 */
	private Label committedNonHeapMemorySize;

	/**
	 * The label for used heap memory size.
	 */
	private Label usedHeapMemorySize;

	/**
	 * The label for used non-heap memory size.
	 */
	private Label usedNonHeapMemorySize;

	/**
	 * The global data access service.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		memoryObj = new MemoryInformationData();
		memoryObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		systemObj = new SystemInformationData();
		systemObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_MEMORY);

		SystemInformationData systemData = (SystemInformationData) dataAccessService.getLastDataObject(systemObj);
		if (systemData != null) {
			// adds some static informations
			addItemToSection(toolkit, SECTION_MEMORY, "Max heap size: ");
			if (systemData.getMaxHeapMemorySize() > 0) {
				addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getMaxHeapMemorySize()));
			} else {
				addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
			}
			addItemToSection(toolkit, SECTION_MEMORY, "Max non-heap size: ");
			if (systemData.getMaxNonHeapMemorySize() > 0) {
				addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getMaxNonHeapMemorySize()));
			} else {
				addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
			}
			addItemToSection(toolkit, SECTION_MEMORY, "Total physical memory: ");
			if (systemData.getTotalPhysMemory() > 0) {
				addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getTotalPhysMemory()));
			} else {
				addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
			}
			addItemToSection(toolkit, SECTION_MEMORY, "Total swap space: ");
			if (systemData.getTotalSwapSpace() > 0) {
				addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getTotalSwapSpace()));
			} else {
				addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
			}
		} else {
			// if no static informations available
			addItemToSection(toolkit, SECTION_MEMORY, "Max heap size: ");
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
			addItemToSection(toolkit, SECTION_MEMORY, "Max non-heap size: ");
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
			addItemToSection(toolkit, SECTION_MEMORY, "Total physical memory: ");
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
			addItemToSection(toolkit, SECTION_MEMORY, "Total swap space: ");
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE);
		}

		if (sections.containsKey(SECTION_MEMORY)) {
			// creates some labels
			addItemToSection(toolkit, SECTION_MEMORY, "Free physical memory: ");
			freePhysMemory = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, SWT.LEFT);
			freePhysMemory.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Free swap space: ");
			freeSwapSpace = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, SWT.LEFT);
			freeSwapSpace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Committed heap size: ");
			committedHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, SWT.LEFT);
			committedHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Committed non-heap size: ");
			committedNonHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, SWT.LEFT);
			committedNonHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Used heap size: ");
			usedHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, SWT.LEFT);
			usedHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Used non-heap size: ");
			usedNonHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, SWT.LEFT);
			usedNonHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		final MemoryInformationData data = (MemoryInformationData) dataAccessService.getLastDataObject(memoryObj);

		if (null != data) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// updates the labels
					int count = data.getCount();
					if (data.getTotalFreePhysMemory() > 0) {
						freePhysMemory.setText(NumberFormatter.formatBytesToKBytes(data.getTotalFreePhysMemory() / count));
					} else {
						freePhysMemory.setText(NOT_AVAILABLE);
					}
					if (data.getTotalFreeSwapSpace() > 0) {
						freeSwapSpace.setText(NumberFormatter.formatBytesToKBytes(data.getTotalFreeSwapSpace() / count));
					} else {
						freeSwapSpace.setText(NOT_AVAILABLE);
					}
					if (data.getTotalComittedHeapMemorySize() > 0) {
						committedHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalComittedHeapMemorySize() / count));
					} else {
						committedHeapMemorySize.setText(NOT_AVAILABLE);
					}
					if (data.getTotalComittedNonHeapMemorySize() > 0) {
						committedNonHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalComittedNonHeapMemorySize() / count));
					} else {
						committedNonHeapMemorySize.setText(NOT_AVAILABLE);
					}
					if (data.getTotalUsedHeapMemorySize() > 0) {
						usedHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalUsedHeapMemorySize() / count));
					} else {
						usedHeapMemorySize.setText(NOT_AVAILABLE);
					}
					if (data.getTotalUsedNonHeapMemorySize() > 0) {
						usedNonHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalUsedNonHeapMemorySize() / count));
					} else {
						usedNonHeapMemorySize.setText(NOT_AVAILABLE);
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
