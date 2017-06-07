package rocks.inspectit.ui.rcp.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Objects;

import rocks.inspectit.shared.all.util.ExecutorServiceUtils;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
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
	 * Selected alert.
	 */
	private Alert alert;

	/**
	 * OK button.
	 */
	private Control okButton;

	/**
	 * MApping of available alerts.
	 */
	private final Map<String, Alert> availableAlerts = new HashMap<>();

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
		if (null == initialRepositoryDefinition) {
			cmrRepositoryDefinition = onlineCMRs.get(0);
		} else {
			cmrRepositoryDefinition = initialRepositoryDefinition;
		}

		updateKnownAlerts();
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
		cmrCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				cmrRepositoryDefinition = getCMRFromCombo();
				updateKnownAlerts();
				validateInput();
			}
		});

		Label alertIDLabel = new Label(main, SWT.LEFT);
		alertIDLabel.setText("AlertId:");

		alertIDText = new Text(main, SWT.BORDER);
		alertIDText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		alertIDText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		applyDialogFont(main);
		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
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
	 * Updates the mapping of known alerts.
	 */
	private void updateKnownAlerts() {
		Future<List<Alert>> future = ExecutorServiceUtils.getExecutorService().submit(new Callable<List<Alert>>() {
			@Override
			public List<Alert> call() throws Exception {
				return cmrRepositoryDefinition.getAlertAccessService().getAlerts();
			}
		});

		List<Alert> alertList = null;

		try {
			alertList = future.get();
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("Unexpected exception occurred during an attempt to fetch existing alerts.", e, -1);
			return;
		}

		for (Alert knownAlert : alertList) {
			availableAlerts.put(knownAlert.getId(), knownAlert);
		}
	}

	/**
	 * Validates the current input.
	 */
	private void validateInput() {
		final String alertId = alertIDText.getText().trim();
		alert = StringUtils.isEmpty(alertId) ? null : availableAlerts.get(alertId); // NOPMD
		if (null == alert) {
			okButton.setEnabled(false);
			if (StringUtils.isEmpty(alertId)) {
				AlertSelectionDialog.this.setMessage("No alert id specified!", IMessageProvider.ERROR);
			} else {
				AlertSelectionDialog.this.setMessage("There is no alert with id '" + alertId + "'!", IMessageProvider.ERROR);
			}
		} else {
			okButton.setEnabled(true);
			AlertSelectionDialog.this.setMessage(
					"Selected alert:\n" + TextFormatter.getAlertDescription(alert),
					IMessageProvider.INFORMATION);
		}
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
		for (int i = 0; i < onlineCMRs.size(); i++) {
			if (Objects.equal(onlineCMRs.get(i), cmrRepositoryDefinition)) {
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
	 * Gets {@link #alert}.
	 *
	 * @return {@link #alert}
	 */
	public Alert getAlert() {
		return this.alert;
	}

}
