package rocks.inspectit.ui.rcp.ci.wizard.page;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceExportData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.wizard.page.SelectFileWizardPage;

/**
 * Configuration interface import page. Extension of the {@link SelectFileWizardPage} where all the
 * needed import information are defined.
 *
 * @author Ivan Senic
 *
 */
public class ImportCiSelectWizardPage extends SelectFileWizardPage {

	/**
	 * Default wizard page message.
	 */
	private static final String DEFAULT_MESSAGE = "Select a file to import and a destination";

	/**
	 * List of available CMR repositories.
	 */
	private final List<CmrRepositoryDefinition> cmrRepositoryList;

	/**
	 * Pre-selected repository.
	 */
	private final CmrRepositoryDefinition repositoryDefinition;

	/**
	 * Combo for selecting CMR.
	 */
	private Combo cmrCombo;

	/**
	 * Default constructor.
	 *
	 * @param pageName
	 *            Page name
	 * @param preSelectedRepository
	 *            Pre-selected repository, can be <code>null</code>
	 */
	public ImportCiSelectWizardPage(String pageName, CmrRepositoryDefinition preSelectedRepository) {
		super(pageName, DEFAULT_MESSAGE, new String[] { "*" + ConfigurationInterfaceExportData.FILE_EXTENSION }, "", SWT.OPEN);
		this.cmrRepositoryList = InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions();
		this.repositoryDefinition = preSelectedRepository;
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

		cmrCombo = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryList) {
			cmrCombo.add(cmrRepositoryDefinition.getName());
		}
		cmrCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Listener pageCompleteListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				if (fileText.getText().isEmpty()) {
					setMessage("No file selected", ERROR);
					return;
				}

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
				setMessage(DEFAULT_MESSAGE);
			}
		};

		select.addListener(SWT.Selection, pageCompleteListener);
		cmrCombo.addListener(SWT.Selection, pageCompleteListener);

		if (null != repositoryDefinition) {
			cmrCombo.select(cmrCombo.indexOf(repositoryDefinition.getName()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (!super.isPageComplete()) {
			return false;
		}

		if (cmrCombo.getSelectionIndex() == -1) {
			return false;
		}
		CmrRepositoryDefinition cmrRepositoryDefinition = cmrRepositoryList.get(cmrCombo.getSelectionIndex());
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) { // NOPMD
			return false;
		}
		return true;
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
