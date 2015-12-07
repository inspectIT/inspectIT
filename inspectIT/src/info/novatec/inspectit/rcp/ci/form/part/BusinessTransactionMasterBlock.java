/**
 *
 */
package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import info.novatec.inspectit.rcp.ci.testers.BusinessContextTester;
import info.novatec.inspectit.rcp.ci.wizard.CreateBusinessTransactionWizard;
import info.novatec.inspectit.rcp.dialog.EditNameDescriptionDialog;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Master-Details part for managing business transaction definitions.
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionMasterBlock extends MasterDetailsBlock implements IFormPart, ISelectionChangedListener, IPropertyListener {

	/**
	 * Property tester for selected {@link BusinessTransactionDefinition} instances.
	 */
	private final BusinessContextTester propertyTester = new BusinessContextTester();

	/**
	 * Table viewer in the form.
	 */
	protected TableViewer tableViewer;

	/**
	 * Managed form to report to.
	 */
	private IManagedForm managedForm;

	/**
	 * {@link FormPage} section belongs to.
	 */
	private final FormPage formPage;

	/**
	 * {@link ApplicationDefinition} for which {@link BusinessTransactionDefinition} instances are
	 * modified.
	 */
	private ApplicationDefinition applicationDefinition;

	/**
	 * The currently selected {@link BusinessTransactionDefinition} instance.
	 */
	private BusinessTransactionDefinition selectedBusinessTransaction;

	/**
	 * Dirty state.
	 */
	private boolean dirty;

	/**
	 * Create business transaction action.
	 */
	private CreateBusinessTransactionAction createAction;

	/**
	 * Move business transaction up action.
	 */
	private MoveUpAction moveUpAction;

	/**
	 * Move business transaction down action.
	 */
	private MoveDownAction moveDownAction;

	/**
	 * Delete business transaction action.
	 */
	private DeleteBusinessTransactionAction deleteAction;

	/**
	 * Edit business transaction action.
	 */
	private EditAction editAction;

	/**
	 * Constructor.
	 *
	 * @param formPage
	 *            The {@link FormPage} to create this master block for.
	 */
	public BusinessTransactionMasterBlock(FormPage formPage) {
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);
		ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) formPage.getEditor().getEditorInput();
		applicationDefinition = input.getApplication();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContent(IManagedForm managedForm, Composite parent) {
		super.createContent(managedForm, parent);
		sashForm.setOrientation(SWT.VERTICAL);
		sashForm.setWeights(new int[] { 3, 4 });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {

		managedForm.addPart(this);
		FormToolkit toolkit = managedForm.getToolkit();

		int borderStyle = toolkit.getBorderStyle();
		toolkit.setBorderStyle(SWT.NO);
		Table table = toolkit.createTable(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		tableViewer = new TableViewer(table);
		toolkit.setBorderStyle(borderStyle);

		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		viewerColumn.getColumn().setMoveable(true);
		viewerColumn.getColumn().setResizable(true);
		viewerColumn.getColumn().setText("Definition Name");
		viewerColumn.getColumn().setWidth(250);
		viewerColumn.getColumn().setToolTipText("Definition Name.");

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		viewerColumn.getColumn().setMoveable(true);
		viewerColumn.getColumn().setResizable(true);
		viewerColumn.getColumn().setText("Business Transaction Name");
		viewerColumn.getColumn().setWidth(250);
		viewerColumn.getColumn().setToolTipText("Business Transaction Name.");

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		viewerColumn.getColumn().setMoveable(true);
		viewerColumn.getColumn().setResizable(true);
		viewerColumn.getColumn().setText("Name Extraction");
		viewerColumn.getColumn().setWidth(100);
		viewerColumn.getColumn().setToolTipText("Indicates whether the name of the business transaction will be extracted dynamically.");

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		viewerColumn.getColumn().setMoveable(true);
		viewerColumn.getColumn().setResizable(true);
		viewerColumn.getColumn().setText("Description");
		viewerColumn.getColumn().setWidth(400);
		viewerColumn.getColumn().setToolTipText("Description.");

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new BusinessTransactionLabelProvider());

		tableViewer.setComparator(getViewerComparator());
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setVisible(true);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addSelectionChangedListener(this);

		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);

		this.managedForm.getMessageManager().addMessage(this, "Pay attention to the order of business transaction definitions!", null, IMessageProvider.INFORMATION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPages(DetailsPart detailsPart) {
		this.detailsPart = detailsPart;
		detailsPart.registerPage(BusinessTransactionDefinition.class, new BusinessTransactionDetailsPage(formPage));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {

		createAction = new CreateBusinessTransactionAction();
		createAction.setEnabled(true);
		moveUpAction = new MoveUpAction();
		moveUpAction.setEnabled(false);
		moveDownAction = new MoveDownAction();
		moveDownAction.setEnabled(false);
		deleteAction = new DeleteBusinessTransactionAction();
		deleteAction.setEnabled(false);
		editAction = new EditAction();
		editAction.setEnabled(false);

		// toolbar
		IToolBarManager toolBarManager = managedForm.getForm().getToolBarManager();
		toolBarManager.add(createAction);
		toolBarManager.add(moveUpAction);
		toolBarManager.add(moveDownAction);
		toolBarManager.add(editAction);
		toolBarManager.add(deleteAction);
		toolBarManager.update(true);

		if (applicationDefinition.getId() == ApplicationDefinition.DEFAULT_ID) {
			((ToolBarManager) toolBarManager).getControl().setEnabled(false);
		} else {
			// menu
			MenuManager menuManager = new MenuManager();
			menuManager.add(createAction);
			menuManager.add(moveUpAction);
			menuManager.add(moveDownAction);
			menuManager.add(editAction);
			menuManager.add(deleteAction);
			formPage.getSite().registerContextMenu(menuManager, tableViewer);
			Control control = tableViewer.getControl();
			Menu menu = menuManager.createContextMenu(control);
			control.setMenu(menu);
		}

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				updateActionsEnabledStatus(selection);
			}

		});

		updateContent(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		this.formPage.getEditor().removePropertyListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			dirty = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStale() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (!event.getSelection().isEmpty() && event.getSource() == tableViewer) {
			StructuredSelection structuredSelection = (StructuredSelection) event.getSelection();
			selectedBusinessTransaction = (BusinessTransactionDefinition) structuredSelection.getFirstElement();
			if (structuredSelection.size() != 1) {
				managedForm.fireSelectionChanged(this, new StructuredSelection());
			} else {
				managedForm.fireSelectionChanged(this, event.getSelection());
			}
			tableViewer.refresh(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) formPage.getEditor().getEditorInput();
			applicationDefinition = input.getApplication();
			updateContent((null != selectedBusinessTransaction) ? new StructuredSelection(selectedBusinessTransaction) : new StructuredSelection());
		}
	}

	/**
	 * Updates the enabled state of all menu and toolbar actions.
	 *
	 * @param selection
	 *            Selection of business transactions
	 */
	private void updateActionsEnabledStatus(ISelection selection) {
		boolean noneSelected = true;
		if (selection instanceof StructuredSelection) {
			StructuredSelection sSelection = (StructuredSelection) selection;
			if (sSelection.size() == 1) {
				ApplicationDefinition application = getApplication();
				boolean isDefaultBTx = propertyTester.test(sSelection.getFirstElement(), BusinessContextTester.IS_DEFAULT_BTX_PROPERTY, new Object[] { application }, null);
				boolean canMoveUp = propertyTester.test(sSelection.getFirstElement(), BusinessContextTester.CAN_MOVE_UP_PROPERTY, new Object[] { application }, null);
				boolean canMoveDown = propertyTester.test(sSelection.getFirstElement(), BusinessContextTester.CAN_MOVE_DOWN_PROPERTY, new Object[] { application }, null);

				moveUpAction.setEnabled(canMoveUp && !isDefaultBTx);
				moveDownAction.setEnabled(canMoveDown && !isDefaultBTx);
				deleteAction.setEnabled(!isDefaultBTx);
				editAction.setEnabled(!isDefaultBTx);
				noneSelected = false;
			} else if (sSelection.size() > 1) {
				boolean containsDefaultBTx = false;
				ApplicationDefinition application = getApplication();
				for (Object element : sSelection.toList()) {
					if (propertyTester.test(element, BusinessContextTester.IS_DEFAULT_BTX_PROPERTY, new Object[] { application }, null)) {
						containsDefaultBTx = true;
						break;
					}
				}
				moveUpAction.setEnabled(false);
				moveDownAction.setEnabled(false);
				editAction.setEnabled(false);
				deleteAction.setEnabled(!containsDefaultBTx);
				noneSelected = false;
			}
		}
		if (noneSelected) {
			moveUpAction.setEnabled(false);
			moveDownAction.setEnabled(false);
			deleteAction.setEnabled(false);
			editAction.setEnabled(false);
		}
	}

	/**
	 * Retrieves current {@link ApplicationDefinition} instance under modification.
	 *
	 * @return Returns current {@link ApplicationDefinition} instance under modification.
	 */
	private ApplicationDefinition getApplication() {
		return applicationDefinition;
	}

	/**
	 * Updates the content of the table view.
	 *
	 * @param selection
	 *            selection to be applied to the table view. If null, no selection will be applied.
	 */
	protected void updateContent(final ISelection selection) {
		if (null != tableViewer) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					tableViewer.setInput(getApplication().getBusinessTransactionDefinitions());
					if (null != selection) {
						tableViewer.setSelection(selection, true);
						tableViewer.getTable().setFocus();
					} else if (tableViewer.getTable().getItemCount() > 0) {
						ISelection firstSelection = new StructuredSelection(tableViewer.getElementAt(0));
						tableViewer.setSelection(firstSelection, true);
						tableViewer.getTable().setFocus();
					}
					tableViewer.getTable().layout();
				}
			});
		}
	}

	/**
	 * Returns comparator to be used in the tree viewer. Default implementation returns a comparator
	 * based on the {@link Comparable} interface, which means no-comparator.
	 *
	 * Sub-class can override.
	 *
	 * @return Returns comparator to be used in the tree viewer.
	 */
	protected ViewerComparator getViewerComparator() {
		// just compare based on the comparable interface
		return new ViewerComparator() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof Comparable && e2 instanceof Comparable) {
					return ((Comparable) e1).compareTo(e2);
				}
				return 0;
			}
		};
	}

	/**
	 * Checks if we are in dirty state.
	 */
	private void markDirty() {
		if (!dirty) {
			dirty = true;
			managedForm.dirtyStateChanged();
		}
	}

	/**
	 * Moves a {@link BusinessTransactionDefinition} in the parent list contained in the
	 * corresponding {@link ApplicationDefinition}.
	 *
	 * @param businessTransaction
	 *            {@link BusinessTransactionDefinition} instance to move.
	 * @param up
	 *            indicates whether to move up or down. If true moves up, otherwise down.
	 */
	private synchronized void moveBusinessTransaction(BusinessTransactionDefinition businessTransaction, boolean up) {
		ApplicationDefinition application = getApplication();
		int currentIndex = application.getBusinessTransactionDefinitions().indexOf(businessTransaction);
		int newIndex = currentIndex;
		if (up) {
			newIndex--;
		} else {
			newIndex++;
		}
		if (newIndex < 0 || newIndex >= application.getBusinessTransactionDefinitions().size() - 1) {
			return;
		}

		try {
			application.moveBusinessTransactionDefinition(businessTransaction, newIndex);
			markDirty();
		} catch (BusinessException e) {
			InspectIT.getDefault().createErrorDialog(
					"Moving of the business transaction definition '" + businessTransaction.getBusinessTransactionDefinitionName() + "' failed due to the following exception.", e, -1);
		}

	}

	/**
	 * Label provider for the business transaction table view.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class BusinessTransactionLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Unicode checkmark character.
		 */
		private static final String CHECK_MARK = "\u2713";

		/**
		 * Empty.
		 */
		private static final StyledString EMPTY = new StyledString();

		/**
		 * The resource manager is used for the images etc.
		 */
		private final LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			if (element instanceof BusinessTransactionDefinition) {
				BusinessTransactionDefinition bTxDef = (BusinessTransactionDefinition) element;
				String content = "";
				switch (index) {
				case 0:
					content = bTxDef.getBusinessTransactionDefinitionName();
					break;
				case 1:
					if (bTxDef.dynamicNameExtractionActive()) {
						StyledString result = new StyledString("Pattern: ");
						result.append(new StyledString(bTxDef.getNameExtractionExpression().getTargetNamePattern(), StyledString.QUALIFIER_STYLER));
						return result;
					} else {
						content = bTxDef.getBusinessTransactionDefinitionName();
					}
					break;
				case 2:
					content = bTxDef.dynamicNameExtractionActive() ? CHECK_MARK : "";
					break;
				case 3:
					content = (bTxDef.getDescription() != null) ? bTxDef.getDescription() : "";
					break;
				default:
					return EMPTY;
				}
				if (bTxDef.getId() == BusinessTransactionDefinition.DEFAULT_ID) {
					return new StyledString(content, StyledString.QUALIFIER_STYLER);
				} else {
					return new StyledString(content);
				}
			}
			return EMPTY;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			if (element instanceof BusinessTransactionDefinition) {
				BusinessTransactionDefinition bTxDef = (BusinessTransactionDefinition) element;
				switch (index) {
				case 0:
					Image image;
					if (bTxDef.getId() == BusinessTransactionDefinition.DEFAULT_ID) {
						image = InspectIT.getDefault().getImage(InspectITImages.IMG_BUSINESS_TRANSACTION_GREY);
					} else {
						image = InspectIT.getDefault().getImage(InspectITImages.IMG_BUSINESS_TRANSACTION);
					}

					if (bTxDef.dynamicNameExtractionActive()) {
						image = ImageFormatter.getOverlayedEditorImage(image, resourceManager, 0.6, InspectIT.getDefault().getImage(InspectITImages.IMG_ASTERISK));
					}
					return image;
				default:
					return super.getColumnImage(element, index);
				}
			}
			return super.getColumnImage(element, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			super.dispose();
			resourceManager.dispose();
		}
	}

	/**
	 * This action creates a new {@link BusinessTransactionDefinition}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private final class CreateBusinessTransactionAction extends Action {

		/**
		 * Constructor.
		 */
		CreateBusinessTransactionAction() {
			setId(this.getClass().getSimpleName() + hashCode());
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD);
		}

		@Override
		public String getText() {
			return "Create";
		}

		@Override
		public void run() {
			CreateBusinessTransactionWizard wizard = new CreateBusinessTransactionWizard(getApplication());
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			dialog.setTitle(wizard.getWindowTitle());
			int returnCode = dialog.open();
			if (returnCode == Dialog.OK) {
				BusinessTransactionDefinition businessTX = wizard.getNewBusinessTransaction();
				StructuredSelection newSelection = new StructuredSelection(businessTX);
				updateContent(newSelection);
				markDirty();
			}

		}
	}

	/**
	 * This action deletes a {@link BusinessTransactionDefinition}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private final class DeleteBusinessTransactionAction extends Action {

		/**
		 * Constructor.
		 */
		DeleteBusinessTransactionAction() {
			setId(this.getClass().getSimpleName() + hashCode());
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DELETE);
		}

		@Override
		public String getText() {
			return "Delete";
		}

		@Override
		public void run() {
			ISelection selection = tableViewer.getSelection();
			if (selection instanceof StructuredSelection) {
				for (Object selectedElement : ((StructuredSelection) selection).toList()) {
					getApplication().deleteBusinessTransactionDefinition((BusinessTransactionDefinition) selectedElement);
				}
				updateContent(null);
				managedForm.fireSelectionChanged(BusinessTransactionMasterBlock.this, StructuredSelection.EMPTY);
				markDirty();
			}
		}

	}

	/**
	 * This action moves up a {@link BusinessTransactionDefinition}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private final class MoveUpAction extends Action {

		/**
		 * Constructor.
		 */
		MoveUpAction() {
			setId(this.getClass().getSimpleName() + hashCode());
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_PREVIOUS);
		}

		@Override
		public String getText() {
			return "Move Up";
		}

		@Override
		public void run() {
			ISelection selection = tableViewer.getSelection();
			if (selection instanceof StructuredSelection) {
				BusinessTransactionDefinition businessTX = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
				moveBusinessTransaction(businessTX, true);
				StructuredSelection newSelection = new StructuredSelection(businessTX);
				updateContent(newSelection);
			}
		}

	}

	/**
	 * This action moves down a {@link BusinessTransactionDefinition}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private final class MoveDownAction extends Action {

		/**
		 * Constructor.
		 */
		MoveDownAction() {
			setId(this.getClass().getSimpleName() + hashCode());
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_NEXT);
		}

		@Override
		public String getText() {
			return "Move Down";
		}

		@Override
		public void run() {
			ISelection selection = tableViewer.getSelection();
			if (selection instanceof StructuredSelection) {
				BusinessTransactionDefinition businessTX = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
				moveBusinessTransaction(businessTX, false);
				StructuredSelection newSelection = new StructuredSelection(businessTX);
				updateContent(newSelection);
			}
		}
	}

	/**
	 * This action opens up a wizard to change the name and description of a
	 * {@link BusinessTransactionDefinition}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private final class EditAction extends Action {

		/**
		 * Constructor.
		 */
		EditAction() {
			setId(this.getClass().getSimpleName() + hashCode());
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_EDIT);
		}

		@Override
		public String getText() {
			return "Edit";
		}

		@Override
		public void run() {
			ISelection selection = tableViewer.getSelection();
			if (selection instanceof StructuredSelection) {
				BusinessTransactionDefinition businessTransactionDef = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
				ApplicationDefinition appDefinition = getApplication();
				String[] existingBusinessTxNames = new String[appDefinition.getBusinessTransactionDefinitions().size()];
				int i = 0;
				for (BusinessTransactionDefinition businessTx : appDefinition.getBusinessTransactionDefinitions()) {
					existingBusinessTxNames[i] = businessTx.getBusinessTransactionDefinitionName();
					i++;
				}
				EditNameDescriptionDialog dialog = new EditNameDescriptionDialog(Display.getCurrent().getActiveShell(), businessTransactionDef.getBusinessTransactionDefinitionName(),
						businessTransactionDef.getDescription(), "Edit Business Transaction", "Enter new business transaction name and/or description", existingBusinessTxNames);
				if (Dialog.OK == dialog.open()) {
					businessTransactionDef.setBusinessTransactionDefinitionName(dialog.getName());
					if (StringUtils.isNotBlank(dialog.getDescription())) {
						businessTransactionDef.setDescription(dialog.getDescription());
					}
					updateContent(new StructuredSelection(businessTransactionDef));
					markDirty();
				}
			}
		}
	}
}
