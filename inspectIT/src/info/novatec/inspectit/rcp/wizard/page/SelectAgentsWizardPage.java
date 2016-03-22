package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Wizard page for selecting the agents.
 * 
 * @author Ivan Senic
 * 
 */
public class SelectAgentsWizardPage extends WizardPage {

	/**
	 * Default wizard message.
	 */
	private static final String DEFAULT_MESSAGE = "Selected Agent(s)";

	/**
	 * List of available agents on the server.
	 */
	private List<? extends PlatformIdent> agentList;

	/**
	 * Agent selection table.
	 */
	private Table agentSelection;

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * If any agent should be used.
	 */
	private Button allAgents;

	/**
	 * If specific agents should be used.
	 */
	private Button specificAgents;

	/**
	 * Cmr to get Agents from.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Collection of agents that will be automatically selected in the wizard.
	 */
	private Collection<PlatformIdent> autoSelectedAgents;

	/**
	 * Default constructor.
	 */
	public SelectAgentsWizardPage() {
		this(DEFAULT_MESSAGE);
	}

	/**
	 * This constructor sets the wizard page message.
	 * 
	 * @param message
	 *            Wizard page message.
	 */
	public SelectAgentsWizardPage(String message) {
		this(message, Collections.<PlatformIdent> emptyList());
	}

	/**
	 * This constructor sets the wizard page message and provides possibility to specify the agents
	 * that will be preselected if they are available on the repository.
	 * 
	 * @param message
	 *            Wizard page message.
	 * @param autoSelectedAgents
	 *            Collection of agents that will be automatically selected in the wizard.
	 */
	public SelectAgentsWizardPage(String message, Collection<PlatformIdent> autoSelectedAgents) {
		super("Select Agent(s)");
		this.setTitle("Select Agent(s)");
		this.setMessage(message);
		this.autoSelectedAgents = autoSelectedAgents;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (null != allAgents && !allAgents.isDisposed() && allAgents.getSelection()) {
			return true;
		} else {
			boolean agentSelected = false;
			if (null != agentSelection && !agentSelection.isDisposed()) {
				for (TableItem tableItem : agentSelection.getItems()) {
					if (tableItem.getChecked()) {
						agentSelected = true;
						break;
					}
				}
			}
			if (!agentSelected) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Returns if all agents should be used.
	 * 
	 * @return Returns if all agents should be used.
	 */
	public boolean isAllAgents() {
		return allAgents.getSelection();
	}

	/**
	 * @return Returns list of Agent IDs to be involved in copy to buffer request.
	 */
	public List<Long> getSelectedAgents() {
		if (allAgents.getSelection()) {
			List<Long> returnList = new ArrayList<Long>();
			for (PlatformIdent agent : agentList) {
				returnList.add(agent.getId());
			}
			return returnList;
		} else {
			int index = 0;
			List<Long> returnList = new ArrayList<Long>();
			for (TableItem tableItem : agentSelection.getItems()) {
				if (tableItem.getChecked()) {
					returnList.add(agentList.get(index).getId());
				}
				index++;
			}
			return returnList;
		}
	}

	/**
	 * Sets the repository. Needed to be called before the page is displayed to the user.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public void setCmrRepositoryDefinition(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (!ObjectUtils.equals(cmrRepositoryDefinition, this.cmrRepositoryDefinition)) {
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			for (Control control : main.getChildren()) {
				control.dispose();
			}

			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				Job getAgentsJob = new Job("Loading agents information..") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						final Map<PlatformIdent, AgentStatusData> agentMap = cmrRepositoryDefinition.getGlobalDataAccessService().getAgentsOverview();
						agentList = new ArrayList<PlatformIdent>(agentMap.keySet());
						Collections.sort(agentList, new Comparator<PlatformIdent>() {
							@Override
							public int compare(PlatformIdent a1, PlatformIdent a2) {
								return a1.getAgentName().compareToIgnoreCase(a2.getAgentName());
							}
						});
						SafeExecutor.asyncExec(new Runnable() {
							@Override
							public void run() {
								main.setLayout(new GridLayout(1, false));

								allAgents = new Button(main, SWT.RADIO);
								allAgents.setText("All agent(s)");
								allAgents.setSelection(true);

								specificAgents = new Button(main, SWT.RADIO);
								specificAgents.setText("Select specific Agent(s)");

								boolean preSelectedAgentsActive = false;
								agentSelection = new Table(main, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
								for (PlatformIdent platformIdent : agentList) {
									AgentStatusData agentStatusData = agentMap.get(platformIdent);
									TableItem tableItem = new TableItem(agentSelection, SWT.NONE);
									tableItem.setText(TextFormatter.getAgentDescription(platformIdent, agentStatusData));
									tableItem.setImage(ImageFormatter.getAgentImage(agentStatusData));
									if (CollectionUtils.isNotEmpty(autoSelectedAgents) && autoSelectedAgents.contains(platformIdent)) {
										tableItem.setChecked(true);
										preSelectedAgentsActive = true;
									}
								}
								agentSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
								agentSelection.setEnabled(false);

								Listener pageCompletedListener = new Listener() {

									@Override
									public void handleEvent(Event event) {
										setPageComplete(isPageComplete());
									}
								};
								agentSelection.addListener(SWT.Selection, pageCompletedListener);
								allAgents.addListener(SWT.Selection, pageCompletedListener);
								specificAgents.addListener(SWT.Selection, pageCompletedListener);

								Listener agentsSelectionListener = new Listener() {

									@Override
									public void handleEvent(Event event) {
										if (allAgents.getSelection()) {
											agentSelection.setEnabled(false);
										} else {
											agentSelection.setEnabled(true);
										}
									}
								};
								allAgents.addListener(SWT.Selection, agentsSelectionListener);
								specificAgents.addListener(SWT.Selection, agentsSelectionListener);

								if (preSelectedAgentsActive) {
									specificAgents.setSelection(true);
									allAgents.setSelection(false);
									agentSelection.setEnabled(true);
								}

								main.layout();
							}
						}, main, allAgents, agentSelection, specificAgents);
						return Status.OK_STATUS;
					}
				};
				getAgentsJob.schedule();
			} else {
				main.setLayout(new GridLayout(2, false));

				new Label(main, SWT.NONE).setImage(Display.getDefault().getSystemImage(SWT.ERROR));
				Label text = new Label(main, SWT.WRAP);
				text.setText("Selected repository is currently offline. Action can not be performed.");
				main.layout();
			}
		}
	}

}
