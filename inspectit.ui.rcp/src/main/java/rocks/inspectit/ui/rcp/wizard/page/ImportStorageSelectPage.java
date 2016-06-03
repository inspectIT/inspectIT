package rocks.inspectit.ui.rcp.wizard.page;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import rocks.inspectit.shared.cs.storage.StorageFileType;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Wizard page for selection of the storage to import.
 *
 * @author Ivan Senic
 *
 */
public class ImportStorageSelectPage extends SelectFileWizardPage {

	/**
	 * Default wizard page message.
	 */
	private static final String DEFAULT_MESSAGE = "Select a file to import and a destination";

	/**
	 * List of available CMR repositories.
	 */
	private final List<CmrRepositoryDefinition> cmrRepositoryList;

	/**
	 * Button for selecting if storage should be imported locally.
	 */
	private Button locallyButton;

	/**
	 * Combo for selecting the CMR.
	 */
	private Combo cmrCombo;

	/**
	 * Default constructor.
	 */
	public ImportStorageSelectPage() {
		super("Import Storage", DEFAULT_MESSAGE, new String[] { "*" + StorageFileType.ZIP_STORAGE_FILE.getExtension() }, "", SWT.OPEN);
		cmrRepositoryList = InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		Composite main = (Composite) getControl();

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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (!super.isPageComplete()) {
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
