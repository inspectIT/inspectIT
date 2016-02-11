package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.storage.StorageFileType;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for selection of the storage to import.
 * 
 * @author Ivan Senic
 * 
 */
public class ImportStorageSelectPage extends WizardPage {

	/**
	 * Default wizard page message.
	 */
	private static final String DEFAULT_MESSAGE = "Select a file to import and a destination";

	/**
	 * List of available CMR repositories.
	 */
	private List<CmrRepositoryDefinition> cmrRepositoryList;

	/**
	 * Button for selecting if storage should be imported locally.
	 */
	private Button locallyButton;

	/**
	 * Combo for selecting the CMR.
	 */
	private Combo cmrCombo;

	/**
	 * Text box where the file name will be displayed.
	 */
	private Text fileText;

	/**
	 * Default constructor.
	 */
	public ImportStorageSelectPage() {
		super("Import Storage");
		this.setTitle("Import Storage");
		this.setMessage(DEFAULT_MESSAGE);
		cmrRepositoryList = InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(3, false));

		new Label(main, SWT.NONE).setText("File:");

		fileText = new Text(main, SWT.READ_ONLY | SWT.BORDER);
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Button select = new Button(main, SWT.PUSH);
		select.setText("Select");
		select.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				fileDialog.setText("Select File to Import");
				fileDialog.setFilterExtensions(new String[] { "*" + StorageFileType.ZIP_STORAGE_FILE.getExtension() });
				String file = fileDialog.open();
				if (null != file) {
					fileText.setText(file);
				}
			}
		});

		Label lbl = new Label(main, SWT.NONE);
		lbl.setText("Import to:");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		locallyButton = new Button(main, SWT.RADIO);
		locallyButton.setSelection(true);
		locallyButton.setText("Local machine");
		locallyButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		locallyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cmrCombo.setEnabled(!locallyButton.getSelection());
			}
		});

		new Label(main, SWT.NONE);
		final Button cmrButton = new Button(main, SWT.RADIO);
		cmrButton.setText("CMR Repository");
		cmrButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		new Label(main, SWT.NONE);
		cmrCombo = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryList) {
			cmrCombo.add(cmrRepositoryDefinition.getName());
		}
		cmrCombo.setEnabled(false);
		cmrCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Listener pageCompleteListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				if (fileText.getText().isEmpty()) {
					setMessage("No file selected", ERROR);
					return;
				} else if (!locallyButton.getSelection()) {
					if (cmrCombo.getSelectionIndex() == -1) {
						setMessage("No CMR Repository selected", ERROR);
						return;
					} else {
						CmrRepositoryDefinition cmrRepositoryDefinition = cmrRepositoryList.get(cmrCombo.getSelectionIndex());
						if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
							setMessage("Selected CMR Repository is offline.", ERROR);
							return;
						}
					}
				}
				setMessage(DEFAULT_MESSAGE);
			}
		};

		select.addListener(SWT.Selection, pageCompleteListener);
		locallyButton.addListener(SWT.Selection, pageCompleteListener);
		cmrCombo.addListener(SWT.Selection, pageCompleteListener);

		select.forceFocus();

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (fileText.getText().isEmpty()) {
			return false;
		}
		if (!locallyButton.getSelection()) {
			if (cmrCombo.getSelectionIndex() == -1) {
				return false;
			}
			CmrRepositoryDefinition cmrRepositoryDefinition = cmrRepositoryList.get(cmrCombo.getSelectionIndex());
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return Returns the selected file name.
	 */
	public String getFileName() {
		return fileText.getText();
	}

	/**
	 * @return If storage should be imported locally.
	 */
	public boolean isImportLocally() {
		return locallyButton.getSelection();
	}

	/**
	 * @return Returns {@link CmrRepositoryDefinition} if any is selected for import.
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		if (-1 != cmrCombo.getSelectionIndex()) {
			return cmrRepositoryList.get(cmrCombo.getSelectionIndex());
		}
		return null;
	}
}
