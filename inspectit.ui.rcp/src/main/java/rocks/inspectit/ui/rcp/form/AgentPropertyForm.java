package rocks.inspectit.ui.rcp.form;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.progress.UIJob;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.InstrumentationStatus;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.model.AgentLeaf;
import rocks.inspectit.ui.rcp.model.Component;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.util.SafeExecutor;

/**
 * Class having a form for displaying the properties of a {@link AgentLeaf}.
 * 
 * @author Tobias Angerstein
 *
 */
public class AgentPropertyForm implements ISelectionChangedListener {

	/**
	 * {@link AgentLeaf} to be displayed.
	 */
	private AgentLeaf agentLeaf;

	/**
	 * Job for updating the Agent properties.
	 */
	private final UpdateAgentPropertiesJob updateAgentPropertiesJob = new UpdateAgentPropertiesJob();

	private ManagedForm managedForm; // NOCHK
	private FormToolkit toolkit; // NOCHK
	private ScrolledForm form; // NOCHK
	private Composite mainComposite; // NOCHK
	private CmrRepositoryDefinition cmrRepositoryDefinition; // NOCHK
	private Label uptime; // NOCHK
	private Label instrumentationStatus; // NOCHK
	private Label ipAdresses; // NOCHK
	private Label architecture; // NOCHK
	private Label operatingSystem; // NOCHK

	/**
	 * Default constructor.
	 *
	 * @param parent
	 *            Parent composite.
	 */
	public AgentPropertyForm(Composite parent) {
		this(parent, null);
	}

	/**
	 * Secondary constructor. Set the displayed {@link AgentLeaf}.
	 *
	 * @param parent
	 *            Parent composite.
	 * @param agentLeaf
	 *            Displayed Agent.
	 */
	public AgentPropertyForm(Composite parent, AgentLeaf agentLeaf) {
		this.managedForm = new ManagedForm(parent);
		this.toolkit = managedForm.getToolkit();
		this.form = managedForm.getForm();
		this.agentLeaf = agentLeaf;
		initWidget();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (!selection.isEmpty() && (selection instanceof StructuredSelection)) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (!(firstElement instanceof Component)) {
				return;
			}
			while (firstElement != null) {
				if (firstElement instanceof AgentLeaf) { // NOPMD
					if (!ObjectUtils.equals(agentLeaf, ((AgentLeaf) firstElement))) {
						agentLeaf = (AgentLeaf) firstElement;
						cmrRepositoryDefinition = ((AgentLeaf) firstElement).getCmrRepositoryDefinition();
						refreshAgentStatusData();
						refreshData();
					}
					return;
				}
				firstElement = ((Component) firstElement).getParent();
			}
		}
		if (null != agentLeaf) {
			agentLeaf = null; // NOPMD
			refreshData();
		}

	}

	/**
	 * Refreshes {@link AgentStatusData} of {@link #agentLeaf}.
	 */
	private void refreshAgentStatusData() {
		if (OnlineStatus.ONLINE.equals(cmrRepositoryDefinition.getOnlineStatus())) {
			Map<PlatformIdent, AgentStatusData> statusMap = cmrRepositoryDefinition.getGlobalDataAccessService().getAgentsOverview();
			PlatformIdent currentPlatformIdent = null;
			// find platformIdent with same Id, because the PlatformIdent objects differs
			for (PlatformIdent platformIdent : statusMap.keySet()) {
				if (agentLeaf.getPlatformIdent().getId().equals(platformIdent.getId())) {
					currentPlatformIdent = platformIdent;
				}
			}
			agentLeaf.setAgentStatusData(statusMap.get(currentPlatformIdent));
		}
	}

	/**
	 * Instantiate the widgets.
	 */
	private void initWidget() {
		Composite body = form.getBody();
		body.setLayout(new TableWrapLayout());
		managedForm.getToolkit().decorateFormHeading(form.getForm());
		mainComposite = toolkit.createComposite(body, SWT.NONE);
		mainComposite.setLayout(new TableWrapLayout());
		mainComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// START - General section
		Section generalSection = toolkit.createSection(mainComposite, ExpandableComposite.TITLE_BAR);
		generalSection.setText("General information");

		Composite generalComposite = toolkit.createComposite(generalSection, SWT.NONE);
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		generalComposite.setLayout(tableWrapLayout);
		generalComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		// Uptime
		toolkit.createLabel(generalComposite, "Uptime:");
		uptime = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		// Instrumentation status
		toolkit.createLabel(generalComposite, "Instrumentation Status:");
		instrumentationStatus = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		// IP- Adresses
		toolkit.createLabel(generalComposite, "IP- Adresses:");
		ipAdresses = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		generalSection.setClient(generalComposite);
		generalSection.setLayout(new TableWrapLayout());
		generalSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - General section

		// START - System section
		Section systemSection = toolkit.createSection(mainComposite, ExpandableComposite.TITLE_BAR);
		systemSection.setText("System information");

		Composite systemComposite = toolkit.createComposite(systemSection, SWT.NONE);
		tableWrapLayout.numColumns = 2;
		systemComposite.setLayout(tableWrapLayout);
		systemComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));
		// Operating System
		toolkit.createLabel(systemComposite, "Operating System:");
		operatingSystem = toolkit.createLabel(systemComposite, null, SWT.WRAP);

		// Architecture
		toolkit.createLabel(systemComposite, "Architecture:");
		architecture = toolkit.createLabel(systemComposite, null, SWT.WRAP);

		systemSection.setClient(systemComposite);
		systemSection.setLayout(new TableWrapLayout());
		systemSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - System section

		refreshData();
	}

	/**
	 * Refreshes the data on the view.
	 */
	private void refreshData() {
		// we only schedule if the cancel returns true
		// because cancel fails when job is currently in process
		if (updateAgentPropertiesJob.cancel()) {
			updateAgentPropertiesJob.schedule();
		}
	}

	/**
	 * Sets layout data for the form.
	 *
	 * @param layoutData
	 *            LayoutData.
	 */
	public void setLayoutData(Object layoutData) {
		form.setLayoutData(layoutData);
	}

	/**
	 * Refreshes the property form.
	 */
	public void refresh() {
		refreshData();
	}

	/**
	 *
	 * @return Returns if the form is disposed.
	 */
	public boolean isDisposed() {
		return form.isDisposed();
	}

	/**
	 * Disposes the form.
	 */
	public void dispose() {
		form.dispose();
	}

	/**
	 * Updates agent properties.
	 * 
	 * @param agentLeaf
	 *            Agent Leaf
	 */
	private void updateAgentProperties(AgentLeaf agentLeaf) {
		if (null != agentLeaf) {
			// Get runtime information data
			RuntimeInformationData runtimeDataTemplate = new RuntimeInformationData();
			runtimeDataTemplate.setPlatformIdent(agentLeaf.getPlatformIdent().getId());
			RuntimeInformationData runtimeData = (RuntimeInformationData) cmrRepositoryDefinition.getGlobalDataAccessService().getLastDataObject(runtimeDataTemplate);

			// Get system information data
			SystemInformationData systemDataTemplate = new SystemInformationData();
			systemDataTemplate.setPlatformIdent(agentLeaf.getPlatformIdent().getId());
			SystemInformationData systemData = (SystemInformationData) cmrRepositoryDefinition.getGlobalDataAccessService().getLastDataObject(systemDataTemplate);

			// Update General Information
			if (AgentConnection.CONNECTED.equals(agentLeaf.getAgentStatusData().getAgentConnection())) {
				// Update update time
				uptime.setText(NumberFormatter.millisecondsToString(runtimeData.getUptime()));

				// Update instrumentation status
				if (InstrumentationStatus.PENDING.equals(agentLeaf.getAgentStatusData().getInstrumentationStatus())) {
					instrumentationStatus.setText("Instrumentation is not up-to-date. Last instrumentation update was at "
							+ new SimpleDateFormat().format(new Date(agentLeaf.getAgentStatusData().getLastInstrumentationUpate())) + ".");
				} else if (InstrumentationStatus.UP_TO_DATE.equals(agentLeaf.getAgentStatusData().getInstrumentationStatus())) {
					instrumentationStatus.setText("Instrumentation is up-to-date");
				} else {
					instrumentationStatus.setText("No class cache is available for this agent. Please restart the agent to refresh the class cache.");
				}

				// Update IP list
				StringBuilder formattedIpList = new StringBuilder();
				for (String ip : agentLeaf.getPlatformIdent().getDefinedIPs()) {
					formattedIpList.append(ip).append('\n');
				}
				ipAdresses.setText(formattedIpList.toString());
			} else {
				uptime.setText("");
				instrumentationStatus.setText("");
				ipAdresses.setText("");
			}

			// Update System Information
			operatingSystem.setText(systemData.getOsName() + ", " + systemData.getOsVersion());
			architecture.setText(systemData.getArchitecture());
		} else {
			uptime.setText("");
			instrumentationStatus.setText("");
			ipAdresses.setText("");
			operatingSystem.setText("");
			architecture.setText("");
		}
	}

	/**
	 * Class for updating the agent properties.
	 *
	 * @author Tobias Angerstein
	 *
	 */
	private class UpdateAgentPropertiesJob extends UIJob {

		/**
		 * Default constructor.
		 */
		UpdateAgentPropertiesJob() {
			super("Updating Agent Properties..");
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			final AgentLeaf agentLeaf = AgentPropertyForm.this.agentLeaf;
			final CmrRepositoryDefinition cmrRepositoryDefinition = AgentPropertyForm.this.cmrRepositoryDefinition;
			if (agentLeaf != null && cmrRepositoryDefinition != null && OnlineStatus.ONLINE.equals(cmrRepositoryDefinition.getOnlineStatus())) {
				SafeExecutor.asyncExec(new Runnable() {

					@Override
					public void run() {
							form.setBusy(true);
							form.setMessage(null, IMessageProvider.NONE);
							form.setText(agentLeaf.getName());
							refreshAgentStatusData();
							if (AgentConnection.CONNECTED.equals(agentLeaf.getAgentStatusData().getAgentConnection())) {
								form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_ACTIVE));
							} else {
								form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_NOT_ACTIVE));
							}
							form.setMessage(null, IMessageProvider.NONE);
							updateAgentProperties(agentLeaf);
							mainComposite.setVisible(true);
							form.getBody().layout(true, true);
							form.setBusy(false);
					}
				});
			} else {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						form.setBusy(true);
						form.setText(null);
						form.setMessage("Please select an Agent to see its properties.", IMessageProvider.INFORMATION);
						form.getBody().layout(true, true);

						updateAgentProperties(null);

						mainComposite.setVisible(true);
						form.getBody().layout(true, true);
						form.setBusy(false);
					}
				});

			}
			return Status.OK_STATUS;
		}
	}
}