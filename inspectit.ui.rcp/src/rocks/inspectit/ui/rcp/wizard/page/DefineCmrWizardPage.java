package rocks.inspectit.ui.rcp.wizard.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Wizard page for definition of the new or existing {@link CmrRepositoryDefinition}.
 * 
 * @author Ivan Senic
 * 
 */
public class DefineCmrWizardPage extends WizardPage {

	/**
	 * Default page message.
	 */
	private static final String DEFAULT_MESSAGE = "Define the information for the CMR Repository";

	/**
	 * Name tex box.
	 */
	private Text nameBox;

	/**
	 * IP tex box.
	 */
	private Text ipBox;

	/**
	 * Port tex box.
	 */
	private Text portBox;

	/**
	 * Description box.
	 */
	private Text descriptionBox;

	/**
	 * Repository to edit if edit mode is on.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * List of existing repositories to check if the same one already exists.
	 */
	private List<CmrRepositoryDefinition> existingRepositories;

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *            title for the wizard page
	 */
	public DefineCmrWizardPage(String title) {
		this(title, null);
	}

	/**
	 * Secondary constructor for editing existing CMR.
	 * 
	 * @param title
	 *            title for the wizard page
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to edit
	 * 
	 */
	public DefineCmrWizardPage(String title, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(title);
		this.setTitle(title);
		this.setMessage(DEFAULT_MESSAGE);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.existingRepositories = new ArrayList<CmrRepositoryDefinition>(InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions());
		if (null != cmrRepositoryDefinition) {
			this.existingRepositories.remove(cmrRepositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(4, false));

		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Server name:");
		nameBox = new Text(main, SWT.BORDER);
		nameBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		Label ipLabel = new Label(main, SWT.LEFT);
		ipLabel.setText("IP Address:");
		ipBox = new Text(main, SWT.BORDER);
		ipBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ipBox.setText(CmrRepositoryDefinition.DEFAULT_IP);

		Label portLabel = new Label(main, SWT.LEFT);
		portLabel.setText("Port:");
		portBox = new Text(main, SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		portBox.setLayoutData(gd);
		portBox.setText(String.valueOf(CmrRepositoryDefinition.DEFAULT_PORT));
		portBox.setTextLimit(5);
		portBox.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (portBox.getText().length() > String.valueOf(CmrRepositoryDefinition.DEFAULT_PORT).length()) {
					main.layout();
				}
			}
		});

		Label descLabel = new Label(main, SWT.LEFT);
		descLabel.setText("Description:");
		descLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		descriptionBox = new Text(main, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd.widthHint = 300;
		descriptionBox.setLayoutData(gd);

		Listener pageCompletionListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				setPageMessage();
			}
		};

		nameBox.addListener(SWT.Modify, pageCompletionListener);
		ipBox.addListener(SWT.Modify, pageCompletionListener);
		portBox.addListener(SWT.Modify, pageCompletionListener);

		if (null != cmrRepositoryDefinition) {
			nameBox.setText(cmrRepositoryDefinition.getName());
			ipBox.setText(cmrRepositoryDefinition.getIp());
			portBox.setText(String.valueOf(cmrRepositoryDefinition.getPort()));
			descriptionBox.setText(TextFormatter.emptyStringIfNull(cmrRepositoryDefinition.getDescription()));
		}

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (nameBox.getText().isEmpty()) {
			return false;
		}
		if (ipBox.getText().trim().isEmpty()) {
			return false;
		}
		if (portBox.getText().trim().isEmpty()) {
			return false;
		} else {
			try {
				Integer.parseInt(portBox.getText().trim());
			} catch (NumberFormatException e) {
				return false;
			}
		}

		String ip = ipBox.getText().trim();
		int port = Integer.parseInt(portBox.getText().trim());
		for (CmrRepositoryDefinition cmrRepositoryDefinition : existingRepositories) {
			if (Objects.equals(ip, cmrRepositoryDefinition.getIp()) && port == cmrRepositoryDefinition.getPort()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return Returns the defined {@link CmrRepositoryDefinition}.
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		CmrRepositoryDefinition cmrRepositoryDefinition = new CmrRepositoryDefinition(ipBox.getText().trim(), Integer.parseInt(portBox.getText()), nameBox.getText().trim());
		if (!descriptionBox.getText().trim().isEmpty()) {
			cmrRepositoryDefinition.setDescription(descriptionBox.getText().trim());
		} else {
			cmrRepositoryDefinition.setDescription("");
		}
		return cmrRepositoryDefinition;
	}

	/**
	 * Sets the message based on the page selections.
	 */
	private void setPageMessage() {
		if (nameBox.getText().isEmpty()) {
			setMessage("No value for the CMR name entered", ERROR);
			return;
		}
		if (ipBox.getText().trim().isEmpty()) {
			setMessage("No value for the CMR IP address entered", ERROR);
			return;
		}
		if (portBox.getText().trim().isEmpty()) {
			setMessage("No value for the CMR port entered", ERROR);
			return;
		} else {
			try {
				Integer.parseInt(portBox.getText().trim());
			} catch (NumberFormatException e) {
				setMessage("The port is not in a valid number format", ERROR);
				return;
			}
		}

		String ip = ipBox.getText().trim();
		int port = Integer.parseInt(portBox.getText().trim());
		for (CmrRepositoryDefinition cmrRepositoryDefinition : existingRepositories) {
			if (Objects.equals(ip, cmrRepositoryDefinition.getIp()) && port == cmrRepositoryDefinition.getPort()) {
				setMessage("The repository with given IP address and port already exists", ERROR);
				return;
			}
		}

		setMessage(DEFAULT_MESSAGE);
	}

}
