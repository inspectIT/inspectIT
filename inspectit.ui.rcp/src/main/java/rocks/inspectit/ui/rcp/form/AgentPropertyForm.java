package rocks.inspectit.ui.rcp.form;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.model.AgentLeaf;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.util.SafeExecutor;

/**
 * Class having a form for displaying the properties of a {@link AgentLeaf}.
 * 
 * @author Tobias Angerstein
 *
 */
public class AgentPropertyForm extends AbstractPropertyForm {

	/**
	 * {@link AgentStatusData} to be displayed.
	 */
	private AgentStatusData agentStatusData;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link PlatformIdent}.
	 */
	private PlatformIdent platformIdent;

	/**
	 * Job for updating the Agent properties.
	 */
	private final UpdateAgentPropertiesJob updateAgentPropertiesJob = new UpdateAgentPropertiesJob();

	private Label uptime; // NOCHK
	private Label instrumentationStatus; // NOCHK
	private FormText ipAdresses; // NOCHK
	private Label version; // NOCHK
	private Label connectionTimeStamp; // NOCHK
	private Label lastKeepAliveTimeStamp; // NOCHK
	private Label lastDataSendTimestamp; // NOCHK
	private Label architecture; // NOCHK
	private Label operatingSystem; // NOCHK

	/**
	 * Constructor. Set the displayed {@link AgentLeaf}.
	 *
	 * @param parent
	 *            Parent composite.
	 * @param agentLeaf
	 *            Displayed Agent.
	 */
	public AgentPropertyForm(Composite parent, AgentLeaf agentLeaf) {
		super(parent);
		this.agentStatusData = agentLeaf.getAgentStatusData();
		this.cmrRepositoryDefinition = agentLeaf.getCmrRepositoryDefinition();
		this.platformIdent = agentLeaf.getPlatformIdent();
		initWidget();
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
		toolkit.createLabel(generalComposite, "IPs:");
		ipAdresses = toolkit.createFormText(generalComposite, true);
		ipAdresses.setLayoutData(new TableWrapData(TableWrapData.FILL));

		// Version
		toolkit.createLabel(generalComposite, "Version:");
		version = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		// Connection timestamp
		toolkit.createLabel(generalComposite, "Connected:");
		connectionTimeStamp = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		// Last keep alive timestamp
		toolkit.createLabel(generalComposite, "Last keep alive:");
		lastKeepAliveTimeStamp = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		// Last sending timestamp
		toolkit.createLabel(generalComposite, "Last sending since:");
		lastDataSendTimestamp = toolkit.createLabel(generalComposite, null, SWT.WRAP);

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

		refresh();
	}

	/**
	 * Refreshes the data on the view.
	 */
	public void refresh() {
		// we only schedule if the cancel returns true
		// because cancel fails when job is currently in process
		if (updateAgentPropertiesJob.cancel()) {
			updateAgentPropertiesJob.schedule();
		}
	}

	/**
	 * Updates agent properties.
	 * 
	 * @param agentStatusData
	 *            {@link AgentStatusData}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 * @param platformIdent
	 *            {@link PlatformIdent}
	 * @param runtimeData
	 *            {@link RuntimeInformationData}
	 * @param systemData
	 *            {@link SystemInformationData}
	 */
	private void updateAgentProperties(AgentStatusData agentStatusData, CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent platformIdent, RuntimeInformationData runtimeData,
			SystemInformationData systemData) {
		if (null != agentStatusData && null != cmrRepositoryDefinition && null != platformIdent) {
			// Update General Information
			if (AgentConnection.CONNECTED.equals(agentStatusData.getAgentConnection())) {
				// Update update time
				if (null != runtimeData) {
					uptime.setText(NumberFormatter.millisecondsToString(runtimeData.getUptime()));
				} else {
					uptime.setText("");
				}
				// Update instrumentation status
				instrumentationStatus.setText(StringUtils.capitalize(agentStatusData.getInstrumentationStatus().toString().replace('_', ' ').toLowerCase()));

				// Update IP list
				StringBuilder formattedIpList = new StringBuilder();
				for (String ip : platformIdent.getDefinedIPs()) {
					formattedIpList.append(ip).append(", ");
				}
				formattedIpList.delete(formattedIpList.length() - 2, formattedIpList.length());
				ipAdresses.setText(formattedIpList.toString(), false, false);

				// Update connected since timestamp
				connectionTimeStamp.setText(NumberFormatter.formatTime(agentStatusData.getConnectionTimestamp()));

				// Update last keep alive timestamp
				lastKeepAliveTimeStamp.setText(NumberFormatter.formatTime(agentStatusData.getLastKeepAliveTimestamp()));

				// Update last sending timestamp
				if (null != agentStatusData.getMillisSinceLastData()) {
					lastDataSendTimestamp.setText(agentStatusData.getMillisSinceLastData() + " ms");
				} else {
					lastDataSendTimestamp.setText("");
				}

			} else {
				uptime.setText("");
				instrumentationStatus.setText("");
				ipAdresses.setText("", false, false);
				connectionTimeStamp.setText("");
				lastKeepAliveTimeStamp.setText("");
				lastDataSendTimestamp.setText("");
			}
			// Update version
			version.setText(platformIdent.getVersion());

			// Update System Information
			if (null != systemData) {
				operatingSystem.setText(systemData.getOsName() + ", " + systemData.getOsVersion());
				architecture.setText(systemData.getArchitecture());
			} else {
				operatingSystem.setText("");
				architecture.setText("");
			}
		} else {
			uptime.setText("");
			instrumentationStatus.setText("");
			ipAdresses.setText("", false, false);
			version.setText("");
			connectionTimeStamp.setText("");
			lastKeepAliveTimeStamp.setText("");
			lastDataSendTimestamp.setText("");
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
	private class UpdateAgentPropertiesJob extends Job {

		/**
		 * Default constructor.
		 */
		UpdateAgentPropertiesJob() {
			super("Updating Agent Properties..");
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			final AgentStatusData agentStatusData = AgentPropertyForm.this.agentStatusData;
			final CmrRepositoryDefinition cmrRepositoryDefinition = AgentPropertyForm.this.cmrRepositoryDefinition;
			final PlatformIdent platformIdent = AgentPropertyForm.this.platformIdent;

			// Get runtime information data
			RuntimeInformationData runtimeDataTemplate = new RuntimeInformationData();
			runtimeDataTemplate.setPlatformIdent(platformIdent.getId());
			final RuntimeInformationData runtimeData = (RuntimeInformationData) cmrRepositoryDefinition.getGlobalDataAccessService().getLastDataObject(runtimeDataTemplate);

			// Get system information data
			SystemInformationData systemDataTemplate = new SystemInformationData();
			systemDataTemplate.setPlatformIdent(platformIdent.getId());
			final SystemInformationData systemData = (SystemInformationData) cmrRepositoryDefinition.getGlobalDataAccessService().getLastDataObject(systemDataTemplate);

			refreshAgentStatusData();

			if (OnlineStatus.ONLINE.equals(cmrRepositoryDefinition.getOnlineStatus())) {
				SafeExecutor.asyncExec(new Runnable() {

					@Override
					public void run() {
						form.setBusy(true);
						form.setMessage(null, IMessageProvider.NONE);
						form.setText(platformIdent.getAgentName());

						if (AgentConnection.CONNECTED.equals(agentStatusData.getAgentConnection())) {
							form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_ACTIVE));
						} else {
							form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_NOT_ACTIVE));
						}
						form.setMessage(null, IMessageProvider.NONE);
						updateAgentProperties(agentStatusData, cmrRepositoryDefinition, platformIdent, runtimeData, systemData);
						mainComposite.setVisible(true);
						form.getBody().layout(true, true);
						form.setBusy(false);
					}
				}, form, mainComposite);
			} else {
				SafeExecutor.asyncExec(new Runnable() {

					public void run() {
						form.setBusy(true);
						form.setText(null);
						form.setMessage("Please select an Agent to see its properties.", IMessageProvider.INFORMATION);
						form.getBody().layout(true, true);

						updateAgentProperties(null, null, null, null, null);

						mainComposite.setVisible(true);
						form.getBody().layout(true, true);
						form.setBusy(false);
					}
				}, form, mainComposite);

			}
			return Status.OK_STATUS;
		}

		/**
		 * Refreshes {@link AgentStatusData}.
		 */
		private void refreshAgentStatusData() {
			if (OnlineStatus.ONLINE.equals(cmrRepositoryDefinition.getOnlineStatus())) {
				Map<PlatformIdent, AgentStatusData> statusMap = cmrRepositoryDefinition.getGlobalDataAccessService().getAgentsOverview();
				// find platformIdent with same Id, because the PlatformIdent objects differs
				for (Entry<PlatformIdent, AgentStatusData> entry : statusMap.entrySet()) {
					if (platformIdent.getId().equals(entry.getKey().getId())) {
						agentStatusData = statusMap.get(entry.getKey());
						return;
					}
				}

			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		updateAgentPropertiesJob.cancel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (!selection.isEmpty() && (selection instanceof StructuredSelection)) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof AgentLeaf) {
				if (!ObjectUtils.equals(agentStatusData, ((AgentLeaf) firstElement).getAgentStatusData()) || !ObjectUtils.equals(platformIdent, ((AgentLeaf) firstElement).getPlatformIdent())) {
					agentStatusData = ((AgentLeaf) firstElement).getAgentStatusData();
					cmrRepositoryDefinition = ((AgentLeaf) firstElement).getCmrRepositoryDefinition();
					platformIdent = ((AgentLeaf) firstElement).getPlatformIdent();
				}
				refresh();
				return;
			}
		}
		if (null != agentStatusData || null != cmrRepositoryDefinition || null != platformIdent) {
			agentStatusData = null; // NOPMD
			cmrRepositoryDefinition = null; // NOPMD
			platformIdent = null; // NOPMD
			refresh();
		}
	}
}