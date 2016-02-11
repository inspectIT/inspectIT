package info.novatec.inspectit.rcp.composite;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

/**
 * Composite that show the storage info.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageInfoComposite extends Composite {

	/**
	 * Not available string.
	 */
	private static final String NOT_AVAILABLE = "N/A";

	/**
	 * Max storage description that will be displayed.
	 */
	private static final int MAX_DESCRIPTION_LENGTH = 100;

	/**
	 * Data to display.
	 */
	private IStorageData storageData;

	/** name of the storage. */
	private Label name;
	/** description of the storage. */
	private FormText description;
	/** Label holding the size of the storage. */
	private Label size;
	/** Label describing if the storage was already downloaded. */
	private Label downloaded;
	/** Label for CMR version. */
	private Label cmrVersion;

	/**
	 * If there should be information if storage is downloaded or not.
	 */
	private boolean showDataDownloaded;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of widget to construct
	 * @param showDataDownloaded
	 *            If there should be information if storage is downloaded or not.
	 * @see Composite#Composite(Composite, int)
	 */
	public StorageInfoComposite(Composite parent, int style, boolean showDataDownloaded) {
		super(parent, style);
		this.showDataDownloaded = showDataDownloaded;
		init();
	}

	/**
	 * Secondary constructor. Displays the information from the storage data.
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of widget to construct
	 * @param showDataDownloaded
	 *            If there should be information if storage is downloaded or not.
	 * @param storageData
	 *            Data to display information for.
	 */
	public StorageInfoComposite(Composite parent, int style, boolean showDataDownloaded, IStorageData storageData) {
		this(parent, style, showDataDownloaded);
		displayStorageData(storageData);
	}

	/**
	 * Initializes the widget.
	 */
	private void init() {
		// define layout
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		Group group = new Group(this, SWT.NONE);
		group.setText("Storage Info");
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 10;
		gl.marginWidth = 10;
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(gl);

		Label label = new Label(group, SWT.NONE);
		label.setText("Name:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		name = new Label(group, SWT.WRAP);
		name.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		label = new Label(group, SWT.NONE);
		label.setText("Description:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		description = new FormText(group, SWT.NO_FOCUS | SWT.WRAP);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = 400;
		description.setLayoutData(gd);
		description.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showStorageDescriptionBox();
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText("Size on disk:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		size = new Label(group, SWT.WRAP);
		size.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		label = new Label(group, SWT.NONE);
		label.setText("CMR version:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		cmrVersion = new Label(group, SWT.WRAP);
		cmrVersion.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		if (showDataDownloaded) {
			label = new Label(group, SWT.NONE);
			label.setText("Data downloaded:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			downloaded = new Label(group, SWT.WRAP);
			downloaded.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}
	}

	/**
	 * Displays the storage data.
	 * 
	 * @param storageData
	 *            Data to display information for.
	 */
	public final void displayStorageData(IStorageData storageData) {
		this.storageData = storageData;
		if (null != storageData) {
			name.setText(storageData.getName());
			if (null != storageData.getDescription()) {
				if (storageData.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
					description.setText("<form><p>" + storageData.getDescription().substring(0, MAX_DESCRIPTION_LENGTH) + ".. <a href=\"More\">[More]</a></p></form>", true, false);
				} else {
					description.setText(storageData.getDescription(), false, false);
				}
			} else {
				description.setText("", false, false);
			}
			size.setText(NumberFormatter.humanReadableByteCount(storageData.getDiskSize()));
			if (StringUtils.isNotEmpty(storageData.getCmrVersion())) {
				cmrVersion.setText(storageData.getCmrVersion());
			}
			if (showDataDownloaded) {
				LocalStorageData localStorageData = null;
				if (storageData instanceof LocalStorageData) {
					localStorageData = (LocalStorageData) storageData;
				} else if (storageData instanceof StorageData) {
					localStorageData = InspectIT.getDefault().getInspectITStorageManager().getLocalDataForStorage((StorageData) storageData);
				}
				boolean notDownloaded = (null == localStorageData || !localStorageData.isFullyDownloaded());

				if (notDownloaded) {
					downloaded.setText("No");
				} else {
					downloaded.setText("Yes");
				}
			}
		} else {
			showDataUnavailable();
		}
		this.layout(true, true);
	}

	/**
	 * Updates the composite to display the not available info.
	 */
	public final void showDataUnavailable() {
		name.setText(NOT_AVAILABLE);
		description.setText(NOT_AVAILABLE, false, false);
		size.setText(NOT_AVAILABLE);
		cmrVersion.setText(NOT_AVAILABLE);
		if (showDataDownloaded) {
			downloaded.setText(NOT_AVAILABLE);
		}
	}

	/**
	 * Shows storage description box.
	 */
	private void showStorageDescriptionBox() {
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		PopupDialog popupDialog = new PopupDialog(getShell(), shellStyle, true, false, false, false, false, "Storage description", "Storage description") {
			private static final int CURSOR_SIZE = 15;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = 3;
				gd.verticalIndent = 3;
				text.setLayoutData(gd);
				text.setText(storageData.getDescription());
				return composite;
			}

			@Override
			protected Point getInitialLocation(Point initialSize) {
				// show popup relative to cursor
				Display display = getShell().getDisplay();
				Point location = display.getCursorLocation();
				location.x += CURSOR_SIZE;
				location.y += CURSOR_SIZE;
				return location;
			}

			@Override
			protected Point getInitialSize() {
				return new Point(400, 200);
			}
		};
		popupDialog.open();
	}

}
