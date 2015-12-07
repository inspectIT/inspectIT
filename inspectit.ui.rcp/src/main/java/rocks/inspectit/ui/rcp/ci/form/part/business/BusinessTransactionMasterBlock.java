package rocks.inspectit.ui.rcp.ci.form.part.business;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import rocks.inspectit.ui.rcp.ci.testers.BusinessContextTester;
import rocks.inspectit.ui.rcp.ci.wizard.CreateBusinessTransactionWizard;
import rocks.inspectit.ui.rcp.dialog.EditNameDescriptionDialog;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.util.SafeExecutor;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.TableItemControlDecorationManager;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * Master-Details part for managing business transaction definitions.
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionMasterBlock extends MasterDetailsBlock implements IFormPart, IPropertyListener {

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
	 * The identifier of the currently selected {@link BusinessTransactionDefinition} instance.
	 */
	private int selectedBusinessTransactionId = BusinessTransactionDefinition.DEFAULT_ID;

	/**
	 * Dirty state.
	 */
	private boolean dirty;

	/**
	 * Move business transaction up action.
	 */
	private MoveAction moveUpAction;

	/**
	 * Move business transaction down action.
	 */
	private MoveAction moveDownAction;

	/**
	 * Delete business transaction action.
	 */
	private DeleteBusinessTransactionAction deleteAction;

	/**
	 * Edit business transaction action.
	 */
	private EditAction editAction;

	/**
	 * {@link TableItemControlDecorationManager} instance for managing table item decorations.
	 */
	private final TableItemControlDecorationManager tableControlDecorationManager = new TableItemControlDecorationManager();

	/**
	 * Validation manager.
	 */
	private final BusinessTransactionValidationManager validationManager;

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
		validationManager = new BusinessTransactionValidationManager();
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
		FormToolkit toolkit = managedForm.getToolkit();

		int borderStyle = toolkit.getBorderStyle();
		toolkit.setBorderStyle(SWT.BORDER);
		Composite mainComposite = toolkit.createComposite(parent);
		mainComposite.setLayout(new GridLayout(1, false));

		Table table = toolkit.createTable(mainComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		toolkit.setBorderStyle(borderStyle);
		tableViewer = new TableViewer(table);
		createColumns();
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new BusinessTransactionLabelProvider());
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setVisible(true);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection structuredSelection = (StructuredSelection) event.getSelection();
				updateActionsEnabledStatus(structuredSelection);
				if (!event.getSelection().isEmpty()) {
					selectedBusinessTransactionId = ((BusinessTransactionDefinition) structuredSelection.getFirstElement()).getId();
				}
				managedForm.fireSelectionChanged(BusinessTransactionMasterBlock.this, structuredSelection);
			}
		});

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					deleteSelectedBusinessTransactions();
				} else if (e.keyCode == SWT.F2) {
					editSelectedBusinessTransaction();
				}
			}
		});

		managedForm.addPart(this);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		this.managedForm.getMessageManager().addMessage(this, "Pay attention to the order of business transaction definitions!", null, IMessageProvider.NONE);
	}

	/**
	 * Creates table columns.
	 */
	private void createColumns() {
		String[] columnNames = { "Definition Name", "Business Transaction Name", "Name Extraction", "Description" };
		int[] columnWidths = { 250, 250, 100, 400 };
		String[] toolTips = { "The name of the business transaction definition. This name is also used for the mapped business transactions if dynamic name extraction is not enabled.",
				"The name of the actual mapped business transaction. This name equals to the Definition Name if dynamic name extraction is not enabled, otherwise the name is dynamically extracted according to the specified pattern.",
				"Indicates whether the name of the business transaction will be extracted dynamically.", "Description." };
		for (int i = 0; i < columnNames.length; i++) {
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText(columnNames[i]);
			viewerColumn.getColumn().setWidth(columnWidths[i]);
			viewerColumn.getColumn().setToolTipText(toolTips[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPages(DetailsPart detailsPart) {
		this.detailsPart = detailsPart;
		detailsPart.registerPage(BusinessTransactionDefinition.class, new BusinessTransactionDetailsPage(formPage, validationManager));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		CreateBusinessTransactionAction createAction = new CreateBusinessTransactionAction();
		createAction.setEnabled(true);
		moveUpAction = new MoveAction(true);
		moveDownAction = new MoveAction(false);
		deleteAction = new DeleteBusinessTransactionAction();
		editAction = new EditAction();
		updateActionsEnabledStatus(StructuredSelection.EMPTY);

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

		updateContent(StructuredSelection.EMPTY);
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
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) formPage.getEditor().getEditorInput();
			applicationDefinition = input.getApplication();
			// update only if this page has been already initialized
			if (null != managedForm) {
				try {
					BusinessTransactionDefinition selectedBusinessTransaction = applicationDefinition.getBusinessTransactionDefinition(selectedBusinessTransactionId);
					updateContent(new StructuredSelection(selectedBusinessTransaction));
				} catch (BusinessException e) {
					updateContent(StructuredSelection.EMPTY);
				}
			}
		} else if (propId == IEditorPart.PROP_DIRTY) {
			if (null != tableViewer) {
				tableViewer.refresh();
			}
		}
	}

	/**
	 * Updates the enabled state of all menu and toolbar actions.
	 *
	 * @param selection
	 *            Selection of business transactions
	 */
	private void updateActionsEnabledStatus(ISelection selection) {
		if (selection.isEmpty()) {
			moveUpAction.setEnabled(false);
			moveDownAction.setEnabled(false);
			deleteAction.setEnabled(false);
			editAction.setEnabled(false);
		} else if (selection instanceof StructuredSelection) {
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
			}
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
			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					tableViewer.setInput(getApplication().getBusinessTransactionDefinitions());
					tableViewer.refresh();
					tableViewer.setSelection(selection, true);
					tableViewer.getTable().layout();
					validationManager.updateValidationMessagesForBusinessTransactions(getApplication().getBusinessTransactionDefinitions());
				}
			}, tableViewer.getTable());
		}
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
	private void moveBusinessTransaction(BusinessTransactionDefinition businessTransaction, boolean up) {
		final ApplicationDefinition application = getApplication();
		int currentIndex = application.getBusinessTransactionDefinitions().indexOf(businessTransaction);
		int newIndex = currentIndex;
		if (up) {
			newIndex--;
		} else {
			newIndex++;
		}
		if ((newIndex >= 0) && (newIndex < (application.getBusinessTransactionDefinitions().size() - 1))) {
			try {
				application.moveBusinessTransactionDefinition(businessTransaction, newIndex);
				tableViewer.refresh();
				markDirty();
				StructuredSelection newSelection = new StructuredSelection(businessTransaction);
				updateContent(newSelection);
			} catch (BusinessException e) {
				InspectIT.getDefault().createErrorDialog(
						"Moving of the business transaction definition '" + businessTransaction.getBusinessTransactionDefinitionName() + "' failed due to the following exception.", e, -1);
			}
		}
	}

	/**
	 * Deletes selected business transaction definitions from the table.
	 */
	private void deleteSelectedBusinessTransactions() {
		ISelection selection = tableViewer.getSelection();
		if ((selection instanceof StructuredSelection) && !selection.isEmpty()) {
			boolean itemsDeleted = false;
			for (Object selectedElement : ((StructuredSelection) selection).toList()) {
				BusinessTransactionDefinition businessTxDefinition = (BusinessTransactionDefinition) selectedElement;
				if (businessTxDefinition.getId() != BusinessTransactionDefinition.DEFAULT_ID) {
					getApplication().deleteBusinessTransactionDefinition(businessTxDefinition);
					validationManager.validationStatesRemoved(businessTxDefinition);
					itemsDeleted = true;
				}
			}
			if (itemsDeleted) {
				updateContent(StructuredSelection.EMPTY);
				managedForm.fireSelectionChanged(BusinessTransactionMasterBlock.this, StructuredSelection.EMPTY);
				markDirty();
			}
		}
	}

	/**
	 * Start modification of the (first) selected business transaction definition.
	 */
	private void editSelectedBusinessTransaction() {
		ISelection selection = tableViewer.getSelection();
		if (selection instanceof StructuredSelection) {
			BusinessTransactionDefinition businessTransactionDef = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
			if (businessTransactionDef.getId() == BusinessTransactionDefinition.DEFAULT_ID) {
				return;
			}
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
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
			setText("Create");
		}

		@Override
		public void run() {
			CreateBusinessTransactionWizard wizard = new CreateBusinessTransactionWizard(getApplication());
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			dialog.setTitle(wizard.getWindowTitle());
			if (dialog.open() == Dialog.OK) {
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
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DELETE));
			setText("Delete (DEL)");
		}

		@Override
		public void run() {
			deleteSelectedBusinessTransactions();
		}

	}

	/**
	 * This action moves up or down a {@link BusinessTransactionDefinition}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private final class MoveAction extends Action {
		/**
		 * defines the direction. If true, the item will be moved up, otherwise down
		 */
		private final boolean up;

		/**
		 * Constructor.
		 *
		 * @param up
		 *            defines the direction. If true, the item will be moved up, otherwise down.
		 */
		MoveAction(boolean up) {
			this.up = up;
			setId(this.getClass().getSimpleName() + hashCode());
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(up ? InspectITImages.IMG_PREVIOUS : InspectITImages.IMG_NEXT));
			setText(up ? "Move Up" : "Move Down");
		}

		@Override
		public void run() {
			ISelection selection = tableViewer.getSelection();
			if (selection instanceof StructuredSelection) {
				BusinessTransactionDefinition businessTX = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
				moveBusinessTransaction(businessTX, up);
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
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_EDIT));
			setText("Edit (F2)");
		}

		@Override
		public void run() {
			editSelectedBusinessTransaction();
		}
	}

	/**
	 * Validation manager for business transaction page.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class BusinessTransactionValidationManager extends AbstractValidationManager<BusinessTransactionDefinition> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void notifyUpstream(BusinessTransactionDefinition businessTxDefinition, Set<ValidationState> states) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void showMessage(BusinessTransactionDefinition businessTxDefinition, Set<ValidationState> states) {
			String errorsText = TextFormatter.getValidationErrorsCountText(states, "business transaction");
			if (null != errorsText) {
				String concatenatedMessage = TextFormatter.getValidationConcatenatedMessage(states);
				String message = businessTxDefinition.getBusinessTransactionDefinitionName() + " (" + errorsText + ")";
				managedForm.getForm().getMessageManager().addMessage(businessTxDefinition, message, null, IMessageProvider.ERROR);
				tableControlDecorationManager.showTableItemControlDecoration(tableViewer, businessTxDefinition, concatenatedMessage);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void hideMessage(BusinessTransactionDefinition businessTxDefinition) {
			managedForm.getForm().getMessageManager().removeMessage(businessTxDefinition);
			tableControlDecorationManager.hideTableItemControlDecoration(tableViewer, businessTxDefinition);
		}

		/**
		 * Updates messages for the given set of business transactions.
		 *
		 * @param businessTransactions
		 *            set of business transaction for which the validation messages shall be
		 *            updated.
		 */
		private void updateValidationMessagesForBusinessTransactions(Collection<BusinessTransactionDefinition> businessTransactions) {
			for (BusinessTransactionDefinition businessTxDefinition : businessTransactions) {
				if (isValid(businessTxDefinition)) {
					hideMessage(businessTxDefinition);
				} else {
					showMessage(businessTxDefinition, getValidationErrorStates(businessTxDefinition));
				}
			}
		}

		/**
		 * Checks whether the passed business transcation has validation errors.
		 *
		 * @param businessTransactions
		 *            the {@link BusinessTransactionDefinition} instance to check for
		 * @return returns true, if the given business transaction has validation errors
		 */
		private boolean isValid(BusinessTransactionDefinition businessTransactions) {
			if (null == getValidationErrorStates(businessTransactions)) {
				return true;
			}
			for (ValidationState state : getValidationErrorStates(businessTransactions)) {
				if (!state.isValid()) {
					return false;
				}
			}
			return true;
		}

	}
}
