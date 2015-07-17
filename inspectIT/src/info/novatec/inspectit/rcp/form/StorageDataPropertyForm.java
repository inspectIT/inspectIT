package info.novatec.inspectit.rcp.form;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.handlers.AddStorageLabelHandler;
import info.novatec.inspectit.rcp.handlers.RemoveStorageLabelHandler;
import info.novatec.inspectit.rcp.provider.ILocalStorageDataProvider;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.storage.label.edit.LabelValueEditingSupport;
import info.novatec.inspectit.rcp.storage.label.edit.LabelValueEditingSupport.LabelEditListener;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Form for displaying the {@link StorageData} properties.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageDataPropertyForm implements ISelectionChangedListener {

	/**
	 * Number of max characters displayed for storage description.
	 */
	private static final int MAX_DESCRIPTION_LENGTH = 150;

	/**
	 * Storage data holding the main information about the storage.
	 */
	private IStorageData storageData;

	/**
	 * Leaf that is displayed currently.
	 */
	private IStorageDataProvider storageDataProvider;

	/**
	 * Toolkit used to create widgets.
	 */
	private FormToolkit toolkit;

	/**
	 * {@link ManagedForm}.
	 */
	private ManagedForm managedForm;

	/**
	 * Form that will be created.
	 */
	private ScrolledForm form;

	/**
	 * Label for ID.
	 */
	private Label uniqueId;

	/**
	 * Label for repository.
	 */
	private Label repository;

	/**
	 * Label for description.
	 */
	private FormText description;

	/**
	 * Label for size on disk.
	 */
	private Label sizeOnDisk;

	/**
	 * Label for storage state.
	 */
	private Label state;

	/**
	 * Table of storage labels.
	 */
	private TableViewer labelsTableViewer;

	/**
	 * Add new label button.
	 */
	private Button addNewLabel;

	/**
	 * remove labels button.
	 */
	private Button removeLabels;

	/**
	 * Main composite where widgets are.
	 */
	private Composite mainComposite;

	/**
	 * {@link TableViewerColumn} for label values. Needed for editing support.
	 */
	private TableViewerColumn valueViewerColumn;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent where form will be created.
	 */
	public StorageDataPropertyForm(Composite parent) {
		this(parent, null);
	}

	/**
	 * Secondary constructor. Set the displayed storage leaf.
	 * 
	 * @param parent
	 *            Parent where form will be created.
	 * @param storageDataProvider
	 *            {@link IStorageDataProvider} to display.
	 */
	public StorageDataPropertyForm(Composite parent, IStorageDataProvider storageDataProvider) {
		this.managedForm = new ManagedForm(parent);
		this.toolkit = managedForm.getToolkit();
		this.form = managedForm.getForm();
		this.storageDataProvider = storageDataProvider;
		if (null != storageDataProvider) {
			this.storageData = storageDataProvider.getStorageData();
		}
		initWidget();
	}

	/**
	 * Third constructor. Lets set everything.
	 * 
	 * @param parent
	 *            Parent where form will be created.
	 * @param storageDataProvider
	 *            {@link IStorageDataProvider} to display. Can be <code>null</code>.
	 * @param storageData
	 *            {@link IStorageData} to display. Can be <code>null</code>.
	 */
	public StorageDataPropertyForm(Composite parent, IStorageDataProvider storageDataProvider, IStorageData storageData) {
		this.managedForm = new ManagedForm(parent);
		this.toolkit = managedForm.getToolkit();
		this.form = managedForm.getForm();
		this.storageDataProvider = storageDataProvider;
		this.storageData = storageData;
		initWidget();
	}

	/**
	 * Instantiate the widgets.
	 */
	private void initWidget() {
		Composite body = form.getBody();
		body.setLayout(new TableWrapLayout());
		managedForm.getToolkit().decorateFormHeading(form.getForm());
		mainComposite = toolkit.createComposite(body, SWT.NONE);
		mainComposite.setLayout(new TableWrapLayout());
		mainComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// START - General section
		Section generalSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		generalSection.setText("General information");

		Composite generalComposite = toolkit.createComposite(generalSection, SWT.NONE);
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		generalComposite.setLayout(tableWrapLayout);
		generalComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		toolkit.createLabel(generalComposite, "Repository:");
		repository = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "Description:");
		description = toolkit.createFormText(generalComposite, true);
		description.setLayoutData(new TableWrapData(TableWrapData.FILL));
		description.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showStorageDescriptionBox();
			}
		});

		toolkit.createLabel(generalComposite, "Size on disk:");
		sizeOnDisk = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "State:");
		state = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "Unique ID:");
		uniqueId = toolkit.createLabel(generalComposite, null, SWT.WRAP);
		uniqueId.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		generalSection.setClient(generalComposite);
		generalSection.setLayout(new TableWrapLayout());
		generalSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - General section

		// START - Label section
		Section labelSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		labelSection.setText("Labels");

		Composite labelComposite = toolkit.createComposite(labelSection, SWT.NONE);
		tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		labelComposite.setLayout(tableWrapLayout);
		labelComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Table table = toolkit.createTable(labelComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB);
		tableWrapData.colspan = 2;
		tableWrapData.heightHint = 150;
		table.setLayoutData(tableWrapData);

		labelsTableViewer = new TableViewer(table);

		TableViewerColumn viewerColumn = new TableViewerColumn(labelsTableViewer, SWT.NONE);
		viewerColumn.getColumn().setText("Type");
		viewerColumn.getColumn().setMoveable(false);
		viewerColumn.getColumn().setResizable(true);
		viewerColumn.getColumn().setWidth(140);

		valueViewerColumn = new TableViewerColumn(labelsTableViewer, SWT.NONE);
		valueViewerColumn.getColumn().setText("Value");
		valueViewerColumn.getColumn().setMoveable(false);
		valueViewerColumn.getColumn().setResizable(true);
		valueViewerColumn.getColumn().setWidth(140);

		labelsTableViewer.setContentProvider(new ArrayContentProvider());
		labelsTableViewer.setLabelProvider(new StyledCellIndexLabelProvider() {
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof AbstractStorageLabel) {
					AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
					switch (index) {
					case 0:
						return new StyledString(TextFormatter.getLabelName(label));
					case 1:
						return new StyledString(TextFormatter.getLabelValue(label, false));
					default:
					}
				}
				return null;
			}

			@Override
			protected Image getColumnImage(Object element, int index) {
				if (index == 0 && element instanceof AbstractStorageLabel) {
					return ImageFormatter.getImageForLabel(((AbstractStorageLabel<?>) element).getStorageLabelType());
				}
				return null;
			}
		});
		labelsTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof AbstractStorageLabel && e2 instanceof AbstractStorageLabel) {
					return ((AbstractStorageLabel<?>) e1).compareTo((AbstractStorageLabel<?>) e2);
				}
				return super.compare(viewer, e1, e2);
			}
		});
		labelsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (labelsTableViewer.getSelection().isEmpty() || !isRemoteStorageDisplayed()) {
					removeLabels.setEnabled(false);
				} else {
					removeLabels.setEnabled(true);
				}
			}

		});

		addNewLabel = toolkit.createButton(labelComposite, "Add", SWT.PUSH);
		addNewLabel.setToolTipText("Add new label(s)");
		addNewLabel.setEnabled(false);
		addNewLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

				Command command = commandService.getCommand(AddStorageLabelHandler.COMMAND);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				try {
					command.executeWithChecks(executionEvent);
				} catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			}
		});

		removeLabels = toolkit.createButton(labelComposite, "Remove", SWT.PUSH);
		removeLabels.setToolTipText("Remove selected label(s)");
		removeLabels.setEnabled(false);
		removeLabels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!labelsTableViewer.getSelection().isEmpty()) {
					List<AbstractStorageLabel<?>> inputList = new ArrayList<AbstractStorageLabel<?>>();
					for (Object object : ((StructuredSelection) labelsTableViewer.getSelection()).toArray()) {
						if (object instanceof AbstractStorageLabel) {
							inputList.add((AbstractStorageLabel<?>) object);
						}
					}

					IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

					Command command = commandService.getCommand(RemoveStorageLabelHandler.COMMAND);
					ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
					IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
					context.addVariable(RemoveStorageLabelHandler.INPUT, inputList);
					try {
						command.executeWithChecks(executionEvent);
					} catch (Exception exception) {
						throw new RuntimeException(exception);
					}
				}
			}
		});

		labelSection.setClient(labelComposite);
		labelSection.setLayout(new TableWrapLayout());
		labelSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - General section

		refreshData();
	}

	/**
	 * Sets layout data for the form.
	 * 
	 * @param layoutData
	 *            LayoutData.
	 */
	public void setLayoutData(Object layoutData) {
		form.setLayoutData(layoutData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof IStorageDataProvider) {
					if (!ObjectUtils.equals(storageDataProvider, firstElement)) {
						storageDataProvider = (IStorageDataProvider) firstElement;
						storageData = storageDataProvider.getStorageData();
						final CmrRepositoryDefinition cmrRepositoryDefinition = storageDataProvider.getCmrRepositoryDefinition();
						LabelValueEditingSupport editingSupport = new LabelValueEditingSupport(labelsTableViewer, storageDataProvider.getStorageData(), cmrRepositoryDefinition);
						editingSupport.addLabelEditListener(new LabelEditListener() {

							@Override
							public void preLabelValueChange(AbstractStorageLabel<?> label) {
								if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
									try {
										cmrRepositoryDefinition.getStorageService().removeLabelFromStorage(storageDataProvider.getStorageData(), label);
									} catch (StorageException e) {
										InspectIT.getDefault().createErrorDialog("Label value can not be updated.", e, -1);
									}
								}
							}

							@Override
							public void postLabelValueChange(AbstractStorageLabel<?> label) {
								if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
									try {
										label.setId(0);
										cmrRepositoryDefinition.getStorageService().addLabelToStorage(storageDataProvider.getStorageData(), label, true);
										refreshStorageManagerView(cmrRepositoryDefinition);
									} catch (StorageException e) {
										InspectIT.getDefault().createErrorDialog("Label value can not be updated.", e, -1);
									}
								}
							}

						});
						valueViewerColumn.setEditingSupport(editingSupport);
						refreshData();
					}
					return;
				} else if (firstElement instanceof ILocalStorageDataProvider) {
					IStorageData localStorageData = ((ILocalStorageDataProvider) firstElement).getLocalStorageData();
					if (!ObjectUtils.equals(storageData, localStorageData)) {
						storageDataProvider = null; // NOPMD
						storageData = localStorageData;
						valueViewerColumn.setEditingSupport(null);
						refreshData();
					}
					return;
				}
			}
		}
		if (null != storageDataProvider || null != storageData) {
			storageDataProvider = null; // NOPMD
			storageData = null; // NOPMD
			valueViewerColumn.setEditingSupport(null);
			refreshData();
		}
	}

	/**
	 * Refresh the data after selection is changed.
	 */
	private void refreshData() {
		// refresh data asynchronously
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				form.setBusy(true);
				if (isDataExistsForDisplay()) {
					// data exists, we display info we have
					form.setText(storageData.getName());
					form.setMessage(null, IMessageProvider.NONE);
					mainComposite.setVisible(true);
					uniqueId.setText(storageData.getId());
					String desc = storageData.getDescription();
					if (null != desc) {
						if (desc.length() > MAX_DESCRIPTION_LENGTH) {
							description.setText("<form><p>" + desc.substring(0, MAX_DESCRIPTION_LENGTH) + ".. <a href=\"More\">[More]</a></p></form>", true, false);
						} else {
							description.setText(desc, false, false);
						}
					} else {
						description.setText("", false, false);
					}
					sizeOnDisk.setText(NumberFormatter.humanReadableByteCount(storageData.getDiskSize()));
					labelsTableViewer.setInput(storageData.getLabelList());
					labelsTableViewer.refresh();
					addNewLabel.setEnabled(isRemoteStorageDisplayed());

					// depending of type enable/disable widgets
					if (isRemoteStorageDisplayed()) {
						// for remote storage
						CmrRepositoryDefinition cmrRepositoryDefinition = storageDataProvider.getCmrRepositoryDefinition();
						repository.setText(cmrRepositoryDefinition.getName() + " (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")");
						state.setText(TextFormatter.getStorageStateTextualRepresentation(storageDataProvider.getStorageData().getState()));
						Image img = ImageFormatter.getImageForStorageLeaf((StorageData) storageData);
						form.setImage(img);
					} else {
						// for downloaded storage
						repository.setText("Available locally");
						state.setText("Downloaded");
						form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_DOWNLOADED));
					}
				} else {
					// nothing is selected we display the proper info
					form.setText(null);
					form.setMessage("Please select a storage to see its properties.", IMessageProvider.INFORMATION);
					mainComposite.setVisible(false);
				}

				form.getBody().layout(true, true);
				form.setBusy(false);
			}
		});
	}

	/**
	 * Shows storage description box.
	 */
	private void showStorageDescriptionBox() {
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		PopupDialog popupDialog = new PopupDialog(form.getShell(), shellStyle, true, false, false, false, false, "Storage description", "Storage description") {
			private static final int CURSOR_SIZE = 15;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				Text text = toolkit.createText(parent, null, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
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

	/**
	 * Refreshes the {@link StorageManagerView}, but only reloads the storages from given
	 * repository.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	private void refreshStorageManagerView(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
		if (viewPart instanceof StorageManagerView) {
			((StorageManagerView) viewPart).refresh(cmrRepositoryDefinition);
		}
	}

	/**
	 * @return Returns if any data exists for displaying.
	 */
	private boolean isDataExistsForDisplay() {
		return null != storageData;
	}

	/**
	 * @return Returns if the remote storage is displayed.
	 */
	private boolean isRemoteStorageDisplayed() {
		return null != storageDataProvider;
	}

	/**
	 * @return If form is disposed.
	 */
	public boolean isDisposed() {
		return form.isDisposed();
	}

	/**
	 * Disposes the form.
	 */
	public void dispose() {
		form.dispose();
	}

}
