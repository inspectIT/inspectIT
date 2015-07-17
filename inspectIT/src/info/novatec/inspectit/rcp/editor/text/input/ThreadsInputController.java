package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class represents the textual view of the {@link ThreadInformation} sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ThreadsInputController extends AbstractTextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.threads";

	/**
	 * The name of the section.
	 */
	private static final String SECTION_THREADS = "Threads";

	/**
	 * The template of the {@link ThreadInformationData} object.
	 */
	private ThreadInformationData threadObj;

	/**
	 * The label for live threads.
	 */
	private Label liveThreadCount;

	/**
	 * The label for daemon threads.
	 */
	private Label daemonThreadCount;

	/**
	 * The label for total started threads.
	 */
	private Label totalStartedThreadCount;

	/**
	 * The label for peak threads.
	 */
	private Label peakThreadCount;

	/**
	 * The global data access service.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		threadObj = new ThreadInformationData();
		threadObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_THREADS);

		if (sections.containsKey(SECTION_THREADS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_THREADS, "Live threads: ");
			liveThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			liveThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_THREADS, "Daemon threads: ");
			daemonThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			daemonThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_THREADS, "Peak: ");
			peakThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			peakThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_THREADS, "Total threads started: ");
			totalStartedThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			totalStartedThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		final ThreadInformationData data = (ThreadInformationData) dataAccessService.getLastDataObject(threadObj);

		if (null != data) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// updates the labels
					int count = data.getCount();
					liveThreadCount.setText(NumberFormatter.formatInteger(data.getTotalThreadCount() / count));
					daemonThreadCount.setText(NumberFormatter.formatInteger(data.getTotalDaemonThreadCount() / count));
					totalStartedThreadCount.setText(NumberFormatter.formatLong(data.getTotalTotalStartedThreadCount() / count));
					peakThreadCount.setText(NumberFormatter.formatInteger(data.getTotalPeakThreadCount() / count));
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
