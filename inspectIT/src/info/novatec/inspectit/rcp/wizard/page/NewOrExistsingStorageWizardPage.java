package info.novatec.inspectit.rcp.wizard.page;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Should a new or existing storage should be used.
 * 
 * @author Ivan Senic
 * 
 */
public class NewOrExistsingStorageWizardPage extends WizardPage {

	/**
	 * Use enw storage button.
	 */
	private Button newStorageButton;

	/**
	 * Default constructor.
	 */
	public NewOrExistsingStorageWizardPage() {
		super("Select Storage");
		this.setTitle("Select Storage");
		this.setMessage("Should a new storage should be created, or existing one should be used.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, true));

		newStorageButton = new Button(main, SWT.RADIO);
		newStorageButton.setText("Create new storage");
		newStorageButton.setSelection(true);

		new Button(main, SWT.RADIO).setText("Use existing storage");

		setControl(main);
	}

	/**
	 * Should new storage be used.
	 * 
	 * @return Should new storage be used.
	 */
	public boolean useNewStorage() {
		return newStorageButton.getSelection();
	}

}
