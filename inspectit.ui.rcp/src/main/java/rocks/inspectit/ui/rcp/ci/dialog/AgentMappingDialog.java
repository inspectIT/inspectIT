package rocks.inspectit.ui.rcp.ci.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.validation.IControlValidationListener;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;

/**
 * Dialog for creating new {@link AgentMapping}.
 *
 * @author Ivan Senic
 *
 */
public class AgentMappingDialog extends TitleAreaDialog implements IControlValidationListener {

	/**
	 * Agent mapping being created.
	 */
	private AgentMapping agentMapping;

	/**
	 * Possible environments to map to.
	 */
	private final List<Environment> environments;

	/**
	 * All {@link ValidationControlDecoration}s.
	 */
	private final List<ValidationControlDecoration<?>> validationControlDecorations = new ArrayList<>();

	/**
	 * OK button.
	 */
	private Button okButton;

	/**
	 * Active selection.
	 */
	private Button activeButton;

	/**
	 * Text box for name.
	 */
	private Text nameText;

	/**
	 * Text box for IP.
	 */
	private Text ipText;

	/**
	 * Combo for selecting {@link Environment}.
	 */
	private Combo environmentCombo;

	/**
	 * Text box for description.
	 */
	private StyledText descriptionText;

	/**
	 * Default constructor.
	 *
	 * @param parentShell
	 *            Shell.
	 * @param environments
	 *            Possible environments to map to.
	 */
	public AgentMappingDialog(Shell parentShell, List<Environment> environments) {
		this(parentShell, null, environments);
	}

	/**
	 * Edit mode constructor. Data will be populated with the given {@link AgentMapping}.
	 *
	 * @param parentShell
	 *            Shell.
	 * @param agentMapping
	 *            context capture to edit
	 * @param environments
	 *            Possible environments to map to.
	 */
	public AgentMappingDialog(Shell parentShell, AgentMapping agentMapping, List<Environment> environments) {
		super(parentShell);
		Assert.isLegal(CollectionUtils.isNotEmpty(environments));

		this.environments = environments;
		this.agentMapping = agentMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getTitle());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle(getTitle());
		this.setMessage("Define mapping properties", IMessageProvider.INFORMATION);
	}

	/**
	 * Defines dialog title.
	 *
	 * @return Title
	 */
	private String getTitle() {
		if (null != agentMapping) {
			return "Edit Agent Mapping";
		} else {
			return "Add Agent Mapping";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(null != agentMapping);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (null == agentMapping) {
				agentMapping = new AgentMapping();
			}

			agentMapping.setActive(activeButton.getSelection());
			agentMapping.setAgentName(nameText.getText());
			agentMapping.setIpAddress(ipText.getText());
			agentMapping.setDescription(descriptionText.getText());
			agentMapping.setEnvironmentId(environments.get(environmentCombo.getSelectionIndex()).getId());
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 10;
		main.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gd);

		// active
		activeButton = new Button(main, SWT.CHECK);
		activeButton.setSelection(true);
		activeButton.setText("Active");
		activeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		createInfoLabel(main, "If mapping is currently active. Deactivated mappings will not be considered when assigning Environment to the agent.");

		// agent name
		Label nameLabel = new Label(main, SWT.NONE);
		nameLabel.setText("Agent name:");

		nameText = new Text(main, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ValidationControlDecoration<Text> nameValidationDecoration = new ValidationControlDecoration<Text>(nameText, this) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(nameText.getText());
			}
		};
		nameValidationDecoration.setDescriptionText("Agent name must not be empty");
		nameValidationDecoration.registerListener(SWT.Modify);
		validationControlDecorations.add(nameValidationDecoration);

		createInfoLabel(main, "Name of the agent. Use wild-card '*' for matching several agent names with one mapping.");

		// ip
		Label ipLabel = new Label(main, SWT.NONE);
		ipLabel.setText("IP Address:");

		ipText = new Text(main, SWT.BORDER);
		ipText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ValidationControlDecoration<Text> ipValidationDecoration = new ValidationControlDecoration<Text>(ipText, null, this) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(ipText.getText());
			}
		};
		ipValidationDecoration.setDescriptionText("IP address of the agent must not be empty");
		ipValidationDecoration.registerListener(SWT.Modify);
		validationControlDecorations.add(ipValidationDecoration);

		createInfoLabel(main, "IP address of the agent. Use wild-card '*' for matching several IPs with one mapping. For example, 192.168.* will match all IP addresses in starting with 192.168.");

		// environment
		Label environmentLabel = new Label(main, SWT.NONE);
		environmentLabel.setText("Environment:");

		environmentCombo = new Combo(main, SWT.READ_ONLY | SWT.BORDER | SWT.FLAT);
		environmentCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (Environment environment : environments) {
			environmentCombo.add(environment.getName());
		}

		createInfoLabel(main, "IP address of the agent. Use wild-card '*' for matching several IPs with one mapping. For example, 192.168.* will match all IP addresses in starting with 192.168.");

		// description
		Label descriptionLabel = new Label(main, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		descriptionText = new StyledText(main, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		descriptionText.setAlwaysShowScrollBars(false);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumHeight = 50;
		descriptionText.setLayoutData(gd);

		createInfoLabel(main, "Optional description of the mapping.");

		if (null != agentMapping) {
			activeButton.setSelection(agentMapping.isActive());
			nameText.setText(agentMapping.getAgentName());
			ipText.setText(agentMapping.getIpAddress());
			descriptionText.setText(TextFormatter.emptyStringIfNull(agentMapping.getDescription()));
			for (Environment environment : environments) {
				if (Objects.equals(environment.getId(), agentMapping.getEnvironmentId())) {
					environmentCombo.select(environments.indexOf(environment));
					break;
				}
			}
		}

		// add the validation as last as we need to have something either selected in the combo box
		// or not if e.g. no agent mapping is available
		ValidationControlDecoration<Combo> environmentValidationDecoration = new ValidationControlDecoration<Combo>(environmentCombo, null, false, this) {
			@Override
			protected boolean validate(Combo control) {
				return environmentCombo.getSelectionIndex() >= 0;
			}
		};
		environmentValidationDecoration.registerListener(SWT.Selection);
		environmentValidationDecoration.setDescriptionText("Mapping must define exactly one environment to map agent to.");
		validationControlDecorations.add(environmentValidationDecoration);

		return main;
	}

	/**
	 * Gets {@link #agentMapping}.
	 *
	 * @return {@link #agentMapping}
	 */
	public AgentMapping getAgentMapping() {
		return agentMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		boolean allValid = true;
		for (ValidationControlDecoration<?> validation : validationControlDecorations) {
			if (!validation.isValid()) {
				allValid = false;
				break;
			}
		}

		if (null != okButton) {
			okButton.setEnabled(allValid);
		}
	}

	/**
	 * Creates info icon with given text as tool-tip.
	 *
	 * @param parent
	 *            Composite to create on.
	 * @param text
	 *            Information text.
	 */
	protected void createInfoLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
	}

}
