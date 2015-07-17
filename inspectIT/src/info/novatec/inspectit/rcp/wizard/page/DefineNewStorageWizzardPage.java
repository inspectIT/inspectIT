package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.storage.StorageData;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Define storage page.
 * 
 * @author Ivan Senic
 * 
 */
public class DefineNewStorageWizzardPage extends WizardPage {

	/**
	 * Default message.
	 */
	private static final String DEFAULT_MESSAGE = "Enter storage data and select the repository where it will be created";

	/**
	 * List of CMR repositories.
	 */
	private List<CmrRepositoryDefinition> cmrRepositories;

	/**
	 * Combo box for repositories.
	 */
	private Combo cmrRepositoryCombo;

	/**
	 * Box for storage name.
	 */
	private Text nameBox;

	/**
	 * Box for storage description.
	 */
	private Text descriptionBox;

	/**
	 * Button for choosing if storage should be auto finalized.
	 */
	private Button autoFinalize;

	/**
	 * {@link CmrRepositoryDefinition} that should be initially selected.
	 */
	private CmrRepositoryDefinition proposedCmrRepositoryDefinition;

	/**
	 * If auto-finalize button should be selected.
	 */
	private boolean autoFinalizeSelected;

	/**
	 * Default constructor.
	 */
	public DefineNewStorageWizzardPage() {
		super("Define New Storage");
		setTitle("Define New Storage");
		setDescription(DEFAULT_MESSAGE);
		cmrRepositories = new ArrayList<CmrRepositoryDefinition>();
		cmrRepositories.addAll(InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions());
	}

	/**
	 * This constructor will set provided {@link CmrRepositoryDefinition} as the initially selected
	 * repository to create storage to.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to create storage on.
	 */
	public DefineNewStorageWizzardPage(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this(cmrRepositoryDefinition, true);
	}

	/**
	 * This constructor will set provided {@link CmrRepositoryDefinition} as the initially selected
	 * repository to create storage to.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to create storage on.
	 * @param autoFinalizeSecelected
	 *            If auto-finalize button should be selected.
	 */
	public DefineNewStorageWizzardPage(CmrRepositoryDefinition cmrRepositoryDefinition, boolean autoFinalizeSecelected) {
		this();
		this.proposedCmrRepositoryDefinition = cmrRepositoryDefinition;
		this.autoFinalizeSelected = autoFinalizeSecelected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		Label cmrSelectLabel = new Label(main, SWT.LEFT);
		cmrSelectLabel.setText("Repository:");
		cmrRepositoryCombo = new Combo(main, SWT.READ_ONLY);
		cmrRepositoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		int i = 0;
		int index = -1;
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositories) {
			cmrRepositoryCombo.add(cmrRepositoryDefinition.getName() + " (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")");
			if (cmrRepositoryDefinition.equals(proposedCmrRepositoryDefinition)) {
				index = i;
			}
			i++;
		}
		if (index != -1) {
			cmrRepositoryCombo.select(index);
			cmrRepositoryCombo.setEnabled(false);
		}

		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Storage name:");
		nameBox = new Text(main, SWT.BORDER);
		nameBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label descLabel = new Label(main, SWT.LEFT);
		descLabel.setText("Description:");
		descLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		descriptionBox = new Text(main, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		descriptionBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		new Label(main, SWT.LEFT);
		autoFinalize = new Button(main, SWT.CHECK);
		autoFinalize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		autoFinalize.setText("Auto-finalize storage");
		autoFinalize.setToolTipText("If selected the storage will be automatically finalized after the action completes");
		autoFinalize.setSelection(autoFinalizeSelected);

		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				if (event.widget.equals(cmrRepositoryCombo) && cmrRepositoryCombo.getSelectionIndex() == -1) {
					setMessage("Repository must be selected", IMessageProvider.ERROR);
				} else if (event.widget.equals(cmrRepositoryCombo) && getSelectedRepository().getOnlineStatus() == OnlineStatus.OFFLINE) {
					setMessage("Selected repository is currently unavailable", IMessageProvider.ERROR);
				} else if (event.widget.equals(nameBox) && nameBox.getText().isEmpty()) {
					setMessage("Storage name can not be empty", IMessageProvider.ERROR);
				} else {
					setMessage(DEFAULT_MESSAGE);
				}
			}
		};

		cmrRepositoryCombo.addListener(SWT.Selection, listener);
		nameBox.addListener(SWT.Modify, listener);

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (cmrRepositoryCombo.getSelectionIndex() == -1) {
			return false;
		} else if (getSelectedRepository().getOnlineStatus() == OnlineStatus.OFFLINE) {
			return false;
		}
		if (nameBox.getText().isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * @return Returns the selected repository or null if noting is selected.
	 */
	public CmrRepositoryDefinition getSelectedRepository() {
		if (cmrRepositoryCombo.getSelectionIndex() != -1) {
			return cmrRepositories.get(cmrRepositoryCombo.getSelectionIndex());
		}
		return null;
	}

	/**
	 * @return {@link StorageData} that was defined.
	 */
	public StorageData getStorageData() {
		StorageData storageData = new StorageData();
		storageData.setName(nameBox.getText().trim());
		storageData.setDescription(descriptionBox.getText().trim());
		return storageData;
	}

	/**
	 * Returns if auto-finalize options is selected.
	 * 
	 * @return Returns if auto-finalize options is selected.
	 */
	public boolean isAutoFinalize() {
		return autoFinalize.getSelection();
	}

}
