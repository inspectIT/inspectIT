package rocks.inspectit.ui.rcp.ci.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;

/**
 * This class represents the dialog to choose which agents' instrumentation should be updated.
 *
 * @author Marius Oehler
 *
 */
public class InstrumentationUpdateDialog extends TitleAreaDialog {

	/**
	 * Behavior when saving a changed instrumentation and agents are affected of the change.
	 */
	public enum OnSaveBehavior {
		/**
		 * Show the dialog to the user.
		 */
		SHOW_DIALOG,

		/**
		 * Show no dialog and update all agents to the latest instrumentation.
		 */
		UPDATE_ALL_AGENTS,

		/**
		 * Show no dialog and do not update any agent.
		 */
		DO_NOTHING;
	}

	/**
	 * Default message used when no title is defined.
	 */
	private static final String DEFAULT_MESSAGE = "The following agents have pending instrumentations. Please select the agents which instrumentation should be updated.";

	/**
	 * Default title used when no message is defined.
	 */
	private static final String DEFAULT_TITLE = "Updating Agent Instrumentation";

	/**
	 * The table showing pending agents.
	 */
	private Table table;

	/**
	 * Button to select all agents.
	 */
	private Button buttonSelectAllAgents;

	/**
	 * Button to select specific agents.
	 */
	private Button buttonSelectSpecificAgents;

	/**
	 * Label of the {@link #comboAutoAction}.
	 */
	private Label labelAlwaysDo;

	/**
	 * Combo to select the default on-save action.
	 */
	private Combo comboAutoAction;

	/**
	 * Button to always show this dialog when a agent is pending after instrumentation change.
	 */
	private Button buttonAlwaysShowDialog;

	/**
	 * Map of agents which instrumentation is in a pending state.
	 */
	private Map<PlatformIdent, AgentStatusData> pendingAgents;

	/**
	 * The agents which were selected by the user to update.
	 */
	private Collection<PlatformIdent> selectedAgents;

	/**
	 * The text on the close button.
	 */
	private String closeButtonLabel;

	/**
	 * The selected agent.
	 */
	private final PlatformIdent platformIdent;

	/**
	 * Constructor.
	 *
	 * @param parentShell
	 *            the parent shell
	 * @param pendingAgents
	 *            {@link Map} of agents which instrumentation is not up-to-date
	 */
	public InstrumentationUpdateDialog(Shell parentShell, Map<PlatformIdent, AgentStatusData> pendingAgents) {
		this(parentShell, pendingAgents, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param parentShell
	 *            the parent shell
	 * @param pendingAgents
	 *            {@link Map} of agents which instrumentation is not up-to-date
	 * @param platformIdent
	 *            the selected agent
	 * @param closeButtonLabel
	 *            label of the closing button
	 */
	public InstrumentationUpdateDialog(Shell parentShell, Map<PlatformIdent, AgentStatusData> pendingAgents, PlatformIdent platformIdent, String closeButtonLabel) {
		super(parentShell);
		this.pendingAgents = pendingAgents;
		this.platformIdent = platformIdent;

		if (closeButtonLabel == null) {
			this.closeButtonLabel = IDialogConstants.CLOSE_LABEL;
		} else {
			this.closeButtonLabel = closeButtonLabel;
		}

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle(DEFAULT_TITLE);
		this.setMessage(DEFAULT_MESSAGE, IMessageProvider.INFORMATION);
		this.setTitleImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(DEFAULT_TITLE);
		newShell.setMinimumSize(525, 300);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, closeButtonLabel, false);
		createButton(parent, IDialogConstants.OK_ID, "Update", true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		boolean selectAllAgents = platformIdent == null;

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// creating checkboxes
		buttonSelectAllAgents = new Button(main, SWT.RADIO);
		buttonSelectAllAgents.setText("All agent(s)");
		buttonSelectAllAgents.setSelection(selectAllAgents);
		buttonSelectAllAgents.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.setEnabled(false);
			}
		});

		buttonSelectSpecificAgents = new Button(main, SWT.RADIO);
		buttonSelectSpecificAgents.setText("Select specific Agent(s)");
		buttonSelectSpecificAgents.setSelection(!selectAllAgents);
		buttonSelectSpecificAgents.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.setEnabled(true);
			}
		});

		// table for agents
		table = new Table(main, SWT.CHECK | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		GridData tableLayout = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableLayout.heightHint = 150;
		table.setLayoutData(tableLayout);
		table.setLinesVisible(false);
		table.setEnabled(!selectAllAgents);

		// fill the table
		updateTableContent();

		Label horizontalSeparator = new Label(main, SWT.SEPARATOR | SWT.HORIZONTAL);
		horizontalSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		// lower part to select default action
		buttonAlwaysShowDialog = new Button(main, SWT.CHECK);
		buttonAlwaysShowDialog.setText("Ask me on each instrumentation update");
		buttonAlwaysShowDialog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comboAutoAction.setEnabled(!((Button) e.getSource()).getSelection());
				labelAlwaysDo.setEnabled(!((Button) e.getSource()).getSelection());
			}
		});

		Composite composite = new Composite(main, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		labelAlwaysDo = new Label(composite, SWT.NONE);
		labelAlwaysDo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		labelAlwaysDo.setText("Always do:");

		comboAutoAction = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboAutoAction.setItems(new String[] { "Update all affected agents", "Do not update any agent" });
		comboAutoAction.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboAutoAction.setEnabled(false);

		Composite compositeInfo = new Composite(main, SWT.NONE);
		compositeInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		compositeInfo.setLayout(new GridLayout(2, false));

		Label infoIconLabel = new Label(compositeInfo, SWT.NONE);
		infoIconLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_WARNING));

		Label infoTextLabel = new Label(compositeInfo, SWT.NONE);
		infoTextLabel.setText("It can take up to 30 seconds to apply the instrumentation changes.");

		// select the combo and checkbox to the stored state
		InstrumentationUpdateDialog.OnSaveBehavior saveBehavior = PreferencesUtils.getObject(PreferencesConstants.INSTRUMENTATION_UPDATED_AUTO_ACTION);
		boolean alwaysShowDialog = saveBehavior == OnSaveBehavior.SHOW_DIALOG;
		int selectedIndex = saveBehavior == OnSaveBehavior.DO_NOTHING ? 1 : 0;

		buttonAlwaysShowDialog.setSelection(alwaysShowDialog);
		labelAlwaysDo.setEnabled(!alwaysShowDialog);
		comboAutoAction.setEnabled(!alwaysShowDialog);
		comboAutoAction.select(selectedIndex);

		applyDialogFont(main);
		return main;
	}

	/**
	 * Updates the content of the table. Creates an entry for each agent contained in the
	 * {@link #pendingAgents} map.
	 */
	private void updateTableContent() {
		table.clearAll();

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");

		Map<PlatformIdent, AgentStatusData> sortedPendingAgents = sortPendingAgentsByAgentName(pendingAgents);
		for (Entry<PlatformIdent, AgentStatusData> entry : sortedPendingAgents.entrySet()) {
			if (entry.getValue().getAgentConnection() != AgentConnection.CONNECTED) {
				continue;
			}

			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setData(entry.getKey());
			tableItem.setText(getAgentText(entry, dateFormat));
			tableItem.setImage(ImageFormatter.getAgentImage(entry.getValue()));

			if ((platformIdent != null) && entry.getKey().equals(platformIdent)) {
				tableItem.setChecked(true);
			}
		}
	}

	/**
	 * Returns a descriptive text for the given agent. It contains a date which is formated using
	 * the given {@link SimpleDateFormat}.
	 *
	 * @param entry
	 *            the agent
	 * @param format
	 *            used {@link SimpleDateFormat} to format the date
	 * @return descriptive {@link String} of the given agent
	 */
	private String getAgentText(Entry<PlatformIdent, AgentStatusData> entry, SimpleDateFormat format) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(entry.getKey().getAgentName());
		stringBuffer.append(" [");
		stringBuffer.append(entry.getKey().getVersion());
		stringBuffer.append("] - Pending since: ");
		stringBuffer.append(format.format(new Date(entry.getValue().getLastInstrumentationUpate())));

		return stringBuffer.toString();
	}

	/**
	 * Sorts the given map of platform idents on the agent's name.
	 *
	 * @param map
	 *            {@link Map} containing platform idents
	 * @return a sorted representation of the given {@link Map}
	 */
	private static Map<PlatformIdent, AgentStatusData> sortPendingAgentsByAgentName(Map<PlatformIdent, AgentStatusData> map) {
		List<Map.Entry<PlatformIdent, AgentStatusData>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<PlatformIdent, AgentStatusData>>() {
			@Override
			public int compare(Map.Entry<PlatformIdent, AgentStatusData> left, Map.Entry<PlatformIdent, AgentStatusData> right) {
				String nameLeft = left.getKey().getAgentName();
				String nameRight = right.getKey().getAgentName();
				return nameLeft.compareTo(nameRight);
			}
		});

		Map<PlatformIdent, AgentStatusData> result = new LinkedHashMap<>();
		for (Map.Entry<PlatformIdent, AgentStatusData> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void okPressed() {
		persistOnSaveBehavior();

		// save selected agents
		if (buttonSelectAllAgents.getSelection()) {
			selectedAgents = new ArrayList<>(pendingAgents.keySet());
		} else {
			selectedAgents = new ArrayList<>();
			for (TableItem item : table.getItems()) {
				if (item.getChecked()) {
					selectedAgents.add((PlatformIdent) item.getData());
				}
			}
		}

		super.okPressed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void cancelPressed() {
		persistOnSaveBehavior();
		super.cancelPressed();
	}

	/**
	 * Persists the currently selected "on save behavior" for the instrumentation update.
	 */
	private void persistOnSaveBehavior() {
		boolean askMe = buttonAlwaysShowDialog.getSelection();
		if (askMe) {
			PreferencesUtils.saveObject(PreferencesConstants.INSTRUMENTATION_UPDATED_AUTO_ACTION, OnSaveBehavior.SHOW_DIALOG, false);
		} else {
			if (comboAutoAction.getSelectionIndex() == 0) {
				PreferencesUtils.saveObject(PreferencesConstants.INSTRUMENTATION_UPDATED_AUTO_ACTION, OnSaveBehavior.UPDATE_ALL_AGENTS, false);
			} else {
				PreferencesUtils.saveObject(PreferencesConstants.INSTRUMENTATION_UPDATED_AUTO_ACTION, OnSaveBehavior.DO_NOTHING, false);
			}
		}
	}

	/**
	 * Gets {@link #selectedAgents}.
	 *
	 * @return {@link #selectedAgents}
	 */
	@SuppressWarnings("unchecked")
	public Collection<PlatformIdent> getUpdateAgents() {
		if (CollectionUtils.isEmpty(selectedAgents)) {
			return Collections.EMPTY_LIST;
		}
		return this.selectedAgents;
	}

}
