/**
 *
 */
package rocks.inspectit.ui.rcp.wizard.page;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Reusable wizard page for selecting file.
 *
 * @author Ivan Senic
 *
 */
public class SelectFileWizardPage extends WizardPage {

	/**
	 * Default message.
	 */
	private final String defaultMessage;

	/**
	 * Filter extensions array.
	 */
	private String[] filterExtensions = new String[] {};

	/**
	 * Default file name in the selection.
	 */
	private String defaultFileName = "";

	/**
	 * Save or open flag.
	 */
	private final int flag;

	/**
	 * Text for displaying the file name.
	 */
	protected Text fileText;

	/**
	 * Select file button.
	 */
	protected Button select;

	/**
	 *
	 * @param pageName
	 *            Page name
	 * @param message
	 *            Page message
	 * @param filterExtensions
	 *            Extensions to filter. Can be <code>null</code> to denote no filtering.
	 * @param defaultFileName
	 *            File name displayed in the selection box, if <code>null</code> then no proposal is
	 *            given to the user.
	 * @param flag
	 *            {@link SWT#SAVE} or {@link SWT#OPEN}
	 */
	public SelectFileWizardPage(String pageName, String message, String[] filterExtensions, String defaultFileName, int flag) {
		super(pageName);
		setTitle(pageName);
		setMessage(message);
		this.defaultMessage = message;

		if (null != filterExtensions) {
			this.filterExtensions = filterExtensions;
		}

		if (null != defaultFileName) {
			this.defaultFileName = defaultFileName;
		}

		this.flag = flag;
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

		select = new Button(main, SWT.PUSH);
		select.setText("Select");
		select.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), flag);
				if (flag == SWT.SAVE) {
					fileDialog.setOverwrite(true);
				}
				fileDialog.setText("Select File");
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.setFileName(defaultFileName);
				String file = fileDialog.open();
				if (null != file) {
					fileText.setText(file);
				}
				setPageComplete(isPageComplete());
				if (fileText.getText().isEmpty()) {
					setMessage("No file selected", ERROR);
				} else {
					setMessage(defaultMessage);
				}
			}
		});
		select.forceFocus();

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		return !fileText.getText().isEmpty();
	}

	/**
	 * @return Returns the selected file name.
	 */
	public String getFileName() {
		return fileText.getText();
	}

}
