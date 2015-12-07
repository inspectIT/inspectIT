package info.novatec.inspectit.rcp.ci.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for defining name, description and an insertion order field. Can be used in multiple
 * wizards.
 *
 * @author Alexander Wert
 *
 */
public class NameDescriptionInsertBeforeWizardPage extends WizardPage {

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
	 * Insertion location combo box.
	 */
	private Combo insertBeforeComboBox;

	/**
	 * List of items defining the possibilities where the new item can be inserted to.
	 */
	private final String[] existingItems;

	/**
	 * Tool tip text for the insertion field.
	 */
	private final String insertBeforeToolTip;

	/**
	 * Default constructor.
	 *
	 * @param title
	 *            Title of the page.
	 * @param defaultMessage
	 *            Default message for the page.
	 * @param existingItems
	 *            List of items defining the possibilities where the new item can be inserted to.
	 * @param insertBeforeToolTip
	 *            Tool tip text for the insertion field.
	 */
	public NameDescriptionInsertBeforeWizardPage(String title, String defaultMessage, String[] existingItems, String insertBeforeToolTip) {
		super(title);
		setTitle(title);
		setMessage(defaultMessage);
		this.defaultMessage = defaultMessage;
		this.existingItems = existingItems;
		this.insertBeforeToolTip = insertBeforeToolTip;
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

		Label insertBeforeLabel = new Label(main, SWT.LEFT);
		insertBeforeLabel.setText("Insert Before:");
		insertBeforeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Composite insertBeforeComposite = new Composite(main, SWT.NONE);
		insertBeforeComposite.setLayout(new GridLayout(2, false));
		insertBeforeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		insertBeforeComboBox = new Combo(insertBeforeComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);

		insertBeforeComboBox.setItems(existingItems);
		insertBeforeComboBox.select(existingItems.length - 1);
		insertBeforeComboBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label insertBeforeInfo = new Label(insertBeforeComposite, SWT.LEFT);
		insertBeforeInfo.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		insertBeforeInfo.setToolTipText(insertBeforeToolTip);

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
		if (nameBox.getText().isEmpty()) {
			return false;
		}
		if (alreadyExists(nameBox.getText())) {
			return false;
		}
		return true;
	}

	/**
	 * Indicates whether an element with such a name already exists.
	 *
	 * @param name
	 *            name to check
	 * @return true, if an element with the same name already exists.
	 */
	private boolean alreadyExists(String name) {
		for (String item : existingItems) {
			if (item.equals(name)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Sets the message based on the page selections.
	 */
	private void setPageMessage() {
		if (nameBox.getText().isEmpty()) {
			setMessage("No value for the name entered", ERROR);
			return;
		}
		if (alreadyExists(nameBox.getText())) {
			setMessage("An item with this name already exists!", ERROR);
			return;
		}
		setMessage(defaultMessage);
	}

	/**
	 * @return Returns defined name.
	 */
	@Override
	public String getName() {
		return nameBox.getText();
	}

	/**
	 * @return Returns defined description.
	 */
	@Override
	public String getDescription() {
		return descriptionBox.getText();
	}

	public int getInsertedBeforeIndex() {
		return insertBeforeComboBox.getSelectionIndex();
	}

}
