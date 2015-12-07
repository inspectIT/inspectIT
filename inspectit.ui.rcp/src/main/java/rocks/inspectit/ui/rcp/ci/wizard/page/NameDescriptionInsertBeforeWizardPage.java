package rocks.inspectit.ui.rcp.ci.wizard.page;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;

/**
 * Wizard page for defining name, description and an insertion order field. Can be used in multiple
 * wizards.
 *
 * @author Alexander Wert
 *
 */
public class NameDescriptionInsertBeforeWizardPage extends DefineNameAndDescriptionWizardPage {
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
		super(title, defaultMessage);
		this.existingItems = existingItems;
		this.insertBeforeToolTip = insertBeforeToolTip;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (!super.isPageComplete()) {
			return false;
		}
		if (alreadyExists(getName())) {
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
	@Override
	protected void setPageMessage() {
		if (getName().isEmpty()) {
			setMessage("No value for the name entered", ERROR);
			return;
		}
		if (alreadyExists(getName())) {
			setMessage("An item with this name already exists!", ERROR);
			return;
		}
		super.setPageMessage();
	}

	/**
	 * Returns the selected index to insert the item before.
	 *
	 * @return Returns the selected index to insert the item before.
	 */
	public int getInsertedBeforeIndex() {
		return insertBeforeComboBox.getSelectionIndex();
	}

}
