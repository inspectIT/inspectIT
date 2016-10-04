package rocks.inspectit.ui.rcp.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Selection Dialog for opening Alert Invocation Sequence view.
 *
 * @author Alexander Wert
 *
 */
public class AlertSelectionDialog extends TitleAreaDialog {

	/**
	 * Default message used when no title is defined.
	 */
	private static final String DEFAULT_MESSAGE = "Select CMR Repository and Alert ID.";

	/**
	 * Default title used when no message is defined.
	 */
	private static final String DEFAULT_TITLE = "Open Alert";

	/**
	 * Combo box for selecting online CMR.
	 */
	private Combo cmrCombo;

	/**
	 * Input field for alertID.
	 */
	private Text alertIDText;

	/**
	 * Selected CMR.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Selected alert id.
	 */
	private String alertId;

	/**
	 * List of online CMRs.
	 */
	List<CmrRepositoryDefinition> onlineCMRs = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param parentShell
	 *            The parent shell
	 * @param initialRepositoryDefinition
	 *            Initially selected CMR. Can be null.
	 * @param onlineCMRs
	 *            List of online CMRs.
	 */
	public AlertSelectionDialog(Shell parentShell, CmrRepositoryDefinition initialRepositoryDefinition, List<CmrRepositoryDefinition> onlineCMRs) {
		super(parentShell);
		cmrRepositoryDefinition = initialRepositoryDefinition;
		this.onlineCMRs = onlineCMRs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle(DEFAULT_TITLE);
		this.setMessage(DEFAULT_MESSAGE, IMessageProvider.INFORMATION);
		this.setTitleImage(InspectIT.getDefault().getImage(InspectITImages.IMG_WIZBAN_EDIT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gridData);

		Label cmrLabel = new Label(main, SWT.LEFT);
		cmrLabel.setText("CMR Repository:");

		cmrCombo = new Combo(main, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmrCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		cmrCombo.setItems(getOnlineCmrNames());
		cmrCombo.select(getCurrentSelectedCMRIndex());

		Label alertIDLabel = new Label(main, SWT.LEFT);
		alertIDLabel.setText("AlertId:");

		alertIDText = new Text(main, SWT.BORDER);
		alertIDText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		applyDialogFont(main);
		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			alertId = alertIDText.getText();
			cmrRepositoryDefinition = getCMRFromCombo();
		} else {
			alertId = null; // NOPMD
			alertId = null; // NOPMD
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DEFAULT_TITLE);
	}

	/**
	 * Retrieves an array of names for online CMRs.
	 *
	 * @return An array of names for online CMRs.
	 */
	private String[] getOnlineCmrNames() {
		String[] names = new String[onlineCMRs.size()];
		for (int i = 0; i < onlineCMRs.size(); i++) {
			names[i] = onlineCMRs.get(i).getName();
		}
		return names;
	}

	/**
	 * Returns the index of the currently selected CMR.
	 *
	 * @return The index of the currently selected CMR.
	 */
	private int getCurrentSelectedCMRIndex() {
		if (null == cmrRepositoryDefinition) {
			return 0;
		}
		for (int i = 0; i < onlineCMRs.size(); i++) {
			if (onlineCMRs.get(i).equals(cmrRepositoryDefinition)) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Retrieves the CMR object from the combo selection.
	 *
	 * @return The selected {@link CmrRepositoryDefinition} instance.
	 */
	private CmrRepositoryDefinition getCMRFromCombo() {
		int index = cmrCombo.getSelectionIndex();
		return onlineCMRs.get(index);
	}

	/**
	 * Gets {@link #cmrRepositoryDefinition}.
	 *
	 * @return {@link #cmrRepositoryDefinition}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return this.cmrRepositoryDefinition;
	}

	/**
	 * Gets {@link #alertId}.
	 *
	 * @return {@link #alertId}
	 */
	public String getAlertId() {
		return this.alertId;
	}

}
