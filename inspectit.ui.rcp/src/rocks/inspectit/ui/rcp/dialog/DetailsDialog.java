package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.composite.BreadcrumbTitleComposite;
import info.novatec.inspectit.rcp.details.DetailsGenerationFactory;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.util.AccessibleArrowImage;
import info.novatec.inspectit.rcp.util.ClipboardUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * The dialog that displays details of the element.
 * 
 * @author Ivan Senic
 * 
 */
public class DetailsDialog extends Dialog {

	/**
	 * Maps of commands IDs and labels to display related to navigation.
	 */
	private static final Map<String, String> NAVIGATE_COMMANDS_IDS = new HashMap<>();

	/**
	 * Maps of commands IDs and labels to display related to navigation.
	 */
	private static final Map<String, String> ACTION_COMMANDS_IDS = new HashMap<>();

	static {
		NAVIGATE_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.navigateToAggregatedSqlData", "Aggregated SQL Data");
		NAVIGATE_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.navigateToAggregatedTimerData", "Aggregated Timer Data");
		NAVIGATE_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.navigateToInvocations", "Invocation(s)");
		NAVIGATE_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.navigateToStartMethodInvocations", "Only This Method Invocation(s)");
		NAVIGATE_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.navigateToSingleExceptionType", "Exception Type");
		NAVIGATE_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.navigateToGroupedExceptionType", "Grouped Exception View");

		ACTION_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.copySqlQuery", "Copy SQL Query");
		ACTION_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.copyLogMessage", "Copy Log Message");
		ACTION_COMMANDS_IDS.put("info.novatec.inspectit.rcp.commands.displayInChart", "Display in Chart");
		ACTION_COMMANDS_IDS.put("org.eclipse.ui.file.save", "Save to Server");
	}

	/**
	 * Data to display the details for.
	 */
	private DefaultData defaultData;

	/**
	 * Repository definition data belongs to.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * {@link ICommandService} for dealing with the commands to be displayed on the workflow menu.
	 */
	private ICommandService commandService;

	/**
	 * Command that will be executed when dialog is closed.
	 */
	private Command commandOnClose;

	/**
	 * Arrow image to display next to the command links.
	 */
	private Image arrow;

	/**
	 * {@link BreadcrumbTitleComposite} for displaying on the top.
	 */
	private BreadcrumbTitleComposite breadcrumbTitleComposite;

	/**
	 * List of created {@link DetailsTable}s.
	 */
	private List<DetailsTable> detailTables;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            {@link Shell} to create dialog on.
	 * @param defaultData
	 *            {@link DefaultData} to create details for.
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition} data belongs to.
	 */
	public DetailsDialog(Shell parentShell, DefaultData defaultData, RepositoryDefinition repositoryDefinition) {
		super(parentShell);
		this.defaultData = defaultData;
		this.repositoryDefinition = repositoryDefinition;
		commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		arrow = new AccessibleArrowImage(true).createImage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Details");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite content = (Composite) super.createDialogArea(parent);
		content.setLayout(new FillLayout());
		((GridData) content.getLayoutData()).widthHint = 1050;

		ManagedForm managedForm = new ManagedForm(content);
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		managedForm.getToolkit().decorateFormHeading(form.getForm());

		breadcrumbTitleComposite = new BreadcrumbTitleComposite(form.getForm().getHead(), SWT.NONE);
		setDataForBreadcrumbTitleComposite();
		form.setHeadClient(breadcrumbTitleComposite);

		Composite main = form.getBody();
		TableWrapLayout mainLayout = new TableWrapLayout();
		mainLayout.numColumns = 2;
		main.setLayout(mainLayout);

		// info section
		Composite info = toolkit.createComposite(main);
		info.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		TableWrapLayout infoLayout = new TableWrapLayout();
		infoLayout.verticalSpacing = 20;
		info.setLayout(infoLayout);

		DetailsGenerationFactory generationFactory = InspectIT.getService(DetailsGenerationFactory.class);
		detailTables = generationFactory.createDetailComposites(defaultData, repositoryDefinition, info, toolkit);
		if (CollectionUtils.isNotEmpty(detailTables)) {
			for (DetailsTable detailsTable : detailTables) {
				detailsTable.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
				managedForm.addPart(detailsTable);
			}
		}

		// menu section
		Composite navigation = toolkit.createComposite(main);
		navigation.setData(defaultData);
		navigation.setLayoutData(new TableWrapData(TableWrapData.FILL));
		navigation.setLayout(new GridLayout(1, false));

		// navigate stuff
		Section navigateSection = toolkit.createSection(navigation, Section.TITLE_BAR);
		navigateSection.setText("Navigate To");
		navigateSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Composite navigateComposite = toolkit.createComposite(navigateSection);
		navigateComposite.setLayout(new GridLayout(2, false));
		navigateSection.setClient(navigateComposite);

		createLinks(navigateComposite, toolkit, NAVIGATE_COMMANDS_IDS);

		// actions stuff
		Section actionsSection = toolkit.createSection(navigation, Section.TITLE_BAR);
		actionsSection.setText("Actions");
		actionsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Composite actionsComposite = toolkit.createComposite(actionsSection);
		actionsComposite.setLayout(new GridLayout(2, false));
		actionsSection.setClient(actionsComposite);

		// copy action manually
		toolkit.createLabel(actionsComposite, "", SWT.NONE).setImage(arrow);
		Hyperlink copyLink = toolkit.createHyperlink(actionsComposite, "Copy", SWT.WRAP);
		copyLink.addHyperlinkListener(new CopyHyperlinkListener());
		copyLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createLinks(actionsComposite, toolkit, ACTION_COMMANDS_IDS);

		return parent;
	}

	/**
	 * Creates links for given commands on the parent composite.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit} to use.
	 * @param commandMap
	 *            IDs of the commands to display. Only active ones will be displayed.
	 */
	private void createLinks(Composite parent, FormToolkit toolkit, Map<String, String> commandMap) {
		for (Map.Entry<String, String> entry : commandMap.entrySet()) {
			Command command = commandService.getCommand(entry.getKey());
			if (command.isDefined() && null != command.getHandler() && command.getHandler().isEnabled()) {
				toolkit.createLabel(parent, "", SWT.NONE).setImage(arrow);

				Hyperlink link = toolkit.createHyperlink(parent, entry.getValue(), SWT.WRAP);
				link.addHyperlinkListener(new CommandHyperlinkListener(command));
				link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			}
		}
	}

	/**
	 * Sets data for the {@link BreadcrumbTitleComposite}.
	 */
	private void setDataForBreadcrumbTitleComposite() {
		// repository
		breadcrumbTitleComposite.setRepositoryDefinition(repositoryDefinition);

		// agent
		PlatformIdent platformIdent = repositoryDefinition.getCachedDataService().getPlatformIdentForId(defaultData.getPlatformIdent());
		breadcrumbTitleComposite.setAgent(TextFormatter.getAgentDescription(platformIdent), InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT));

		// sensor info
		if (0 != defaultData.getSensorTypeIdent()) {
			SensorTypeIdent sensorTypeIdent = repositoryDefinition.getCachedDataService().getSensorTypeIdentForId(defaultData.getSensorTypeIdent());
			String fqn = sensorTypeIdent.getFullyQualifiedClassName();
			SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(fqn);
			if (null != sensorTypeEnum) {
				breadcrumbTitleComposite.setGroup(sensorTypeEnum.getDisplayName(), sensorTypeEnum.getImage());
			}
		}

		breadcrumbTitleComposite.setView("Details", null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean close() {
		arrow.dispose();
		breadcrumbTitleComposite.dispose();
		return super.close();
	}

	/**
	 * Gets {@link #commandOnClose}.
	 * 
	 * @return {@link #commandOnClose}
	 */
	public Command getCommandOnClose() {
		return commandOnClose;
	}

	/**
	 * {@link HyperlinkAdapter} for setting the correct {@link DetailsDialog#commandOnClose} when
	 * clicked.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CommandHyperlinkListener extends HyperlinkAdapter {

		/**
		 * Command.
		 */
		private Command command;

		/**
		 * Default constructor.
		 * 
		 * @param command
		 *            Command.
		 */
		public CommandHyperlinkListener(Command command) {
			this.command = command;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void linkActivated(HyperlinkEvent e) {
			DetailsDialog.this.commandOnClose = command;
			DetailsDialog.this.close();
		}
	}

	/**
	 * Hyperlink listener for the copy operation.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CopyHyperlinkListener extends HyperlinkAdapter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void linkActivated(HyperlinkEvent e) {
			StringBuilder sb = new StringBuilder();

			sb.append(breadcrumbTitleComposite.getCopyString());
			sb.append("\n\n");

			for (DetailsTable table : detailTables) {
				sb.append(table.getCopyString());
				sb.append('\n');
			}

			ClipboardUtil.textToClipboard(getShell().getDisplay(), sb.toString());
		}
	}

}
