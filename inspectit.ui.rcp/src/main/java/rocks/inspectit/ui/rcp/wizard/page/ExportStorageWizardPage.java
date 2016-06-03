package rocks.inspectit.ui.rcp.wizard.page;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import rocks.inspectit.shared.cs.storage.IStorageData;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageFileType;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.composite.StorageInfoComposite;

/**
 * Page for exporting storage.
 *
 * @author Ivan Senic
 *
 */
public class ExportStorageWizardPage extends SelectFileWizardPage {

	/**
	 * Default message for wizard page.
	 */
	private static final String DEFAULT_MESSAGE = "Select where to export storage";

	/**
	 * Storage to export.
	 */
	private final IStorageData storageData;

	/**
	 * Default constructor.
	 *
	 * @param storageData
	 *            Storage to export.
	 */
	public ExportStorageWizardPage(IStorageData storageData) {
		super("Export Storage", DEFAULT_MESSAGE, new String[] { "*" + StorageFileType.ZIP_STORAGE_FILE.getExtension() }, storageData.getName(), SWT.SAVE);
		this.storageData = storageData;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		Composite main = (Composite) getControl();

		StorageInfoComposite storageInfoComposite = new StorageInfoComposite(main, SWT.NONE, true, storageData);
		storageInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		LocalStorageData localStorageData = null;
		if (storageData instanceof LocalStorageData) {
			localStorageData = (LocalStorageData) storageData;
		} else if (storageData instanceof StorageData) {
			localStorageData = InspectIT.getDefault().getInspectITStorageManager().getLocalDataForStorage((StorageData) storageData);
		}
		boolean notDownloaded = (null == localStorageData || !localStorageData.isFullyDownloaded());
		if (notDownloaded) {
			Composite infoComposite = new Composite(main, SWT.NONE);
			infoComposite.setLayout(new GridLayout(2, false));
			infoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
			new Label(infoComposite, SWT.NONE).setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO));
			new Label(infoComposite, SWT.WRAP).setText("The not downloaded storage will have to be downloaded prior to export operation.");
		}
	}

}
