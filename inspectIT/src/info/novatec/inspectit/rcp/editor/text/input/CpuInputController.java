package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.util.SafeExecutor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class represents the textual view of the {@link CpuInformation} sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CpuInputController extends AbstractTextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.cpu";

	/**
	 * The name of the section.
	 */
	private static final String SECTION_CPU = "CPU";

	/**
	 * The string representing that something is not available.
	 */
	private static final String NOT_AVAILABLE = "N/A";

	/**
	 * The template of the {@link CpuInformationData} object.
	 */
	private CpuInformationData cpuObj;

	/**
	 * The label for the cpu usage.
	 */
	private Label cpuUsage;

	/**
	 * The label for the process cpu time.
	 */
	private Label processCpuTime;

	/**
	 * The global data access service.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		cpuObj = new CpuInformationData();
		cpuObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_CPU);

		if (sections.containsKey(SECTION_CPU)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_CPU, "Cpu Usage: ");
			cpuUsage = toolkit.createLabel(sections.get(SECTION_CPU), "n/a", SWT.LEFT);
			cpuUsage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_CPU, "Process Cpu Time: ");
			processCpuTime = toolkit.createLabel(sections.get(SECTION_CPU), "n/a", SWT.LEFT);
			processCpuTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		final CpuInformationData data = (CpuInformationData) dataAccessService.getLastDataObject(cpuObj);

		if (null != data) {
			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					int count = data.getCount();
					if (data.getTotalCpuUsage() > 0) {
						cpuUsage.setText(NumberFormatter.formatCpuPercent(data.getTotalCpuUsage() / count));
					} else {
						cpuUsage.setText(NOT_AVAILABLE);
					}
					if (data.getProcessCpuTime() > 0) {
						processCpuTime.setText(NumberFormatter.formatNanosToSeconds(data.getProcessCpuTime()));
					} else {
						processCpuTime.setText(NOT_AVAILABLE);
					}
				}
			}, cpuUsage, processCpuTime);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
