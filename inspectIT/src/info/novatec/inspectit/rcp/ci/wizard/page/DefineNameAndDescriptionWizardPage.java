package info.novatec.inspectit.rcp.ci.wizard.page;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for defining name and description. Can be used in multiple wizards.
 * 
 * @author Ivan Senic
 * 
 */
public class DefineNameAndDescriptionWizardPage extends WizardPage {

	/**
	 * Default message.
	 */
	private final String defaultMessage;

	/**
	 * Name box.
	 */
	private Text nameBox;

	/**
	 * Description box.
	 */
	private Text descriptionBox;

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *            Title of the page.
	 * @param defaultMessage
	 *            Default message for the page.
	 */
	public DefineNameAndDescriptionWizardPage(String title, String defaultMessage) {
		super(title);
		setTitle(title);
		setMessage(defaultMessage);
		this.defaultMessage = defaultMessage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Name:");
		nameBox = new Text(main, SWT.BORDER);
		nameBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label descLabel = new Label(main, SWT.LEFT);
		descLabel.setText("Description:");
		descLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		descriptionBox = new Text(main, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		descriptionBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Listener pageCompletionListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				setPageMessage();
			}
		};
		nameBox.addListener(SWT.Modify, pageCompletionListener);

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		return !nameBox.getText().isEmpty();
	}

	/**
	 * Sets the message based on the page selections.
	 */
	private void setPageMessage() {
		if (nameBox.getText().isEmpty()) {
			setMessage("No value for the name entered", ERROR);
			return;
		}
		setMessage(defaultMessage);
	}

	/**
	 * @return Returns defined name.
	 */
	public String getName() {
		return nameBox.getText();
	}

	/**
	 * @return Returns defined description.
	 */
	public String getDescription() {
		return descriptionBox.getText();
	}

}
