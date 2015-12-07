package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.IContainerExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.page.ValidatorKey;
import info.novatec.inspectit.rcp.ci.testers.BusinessContextTester;
import info.novatec.inspectit.rcp.ci.wizard.CreateBusinessTransactionWizard;
import info.novatec.inspectit.rcp.dialog.EditNameDescriptionDialog;
import info.novatec.inspectit.rcp.validation.TableItemControlDecoration;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
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
public class BusinessTransactionMasterBlock extends MasterDetailsBlock implements IFormPart, ISelectionChangedListener, IPropertyListener, IValidatorRegistry {

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
	 * {@link TableEditor}s to handle the validation decoration on table rows.
	 */
	private final List<TableItemControlDecoration<BusinessTransactionDefinition>> tableItemControlDecorations = new ArrayList<>();

	/**
	 * {@link ValidationControlDecoration} instances.
	 */
	private final Map<ValidatorKey, ValidationControlDecoration<?>> validationControlDecorators = new HashMap<>();

	/**
	 * A set of keys of the validation error messages currently shown on this page.
	 */
	private final Set<BusinessTransactionDefinition> validationErrorMessageKeys = new HashSet<>();

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
		toolkit.setBorderStyle(SWT.BORDER);
		Composite mainComposite = toolkit.createComposite(parent);
		mainComposite.setLayout(new GridLayout(1, false));
		Table table = toolkit.createTable(mainComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		tableViewer = new TableViewer(table);
		toolkit.setBorderStyle(borderStyle);

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

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new BusinessTransactionLabelProvider());
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setVisible(true);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addSelectionChangedListener(this);

		table.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					deleteSelectedBusinessTransactions();
				} else if (e.keyCode == SWT.F2) {
					editSelectedBusinessTransaction();
				}
			}
		});

		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		this.managedForm.getMessageManager().addMessage(this, "Pay attention to the order of business transaction definitions!", null, IMessageProvider.NONE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPages(DetailsPart detailsPart) {
		this.detailsPart = detailsPart;
		detailsPart.registerPage(BusinessTransactionDefinition.class, new BusinessTransactionDetailsPage(formPage, this));
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

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				updateActionsEnabledStatus(selection);
			}
		});

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
	public void selectionChanged(SelectionChangedEvent event) {
		if (!event.getSelection().isEmpty() && event.getSource() == tableViewer) {
			managedForm.commit(false);
			StructuredSelection structuredSelection = (StructuredSelection) event.getSelection();
			selectedBusinessTransactionId = ((BusinessTransactionDefinition) structuredSelection.getFirstElement()).getId();
			managedForm.fireSelectionChanged(this, structuredSelection.size() == 1 ? event.getSelection() : StructuredSelection.EMPTY);
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
			// update only if this page has been already initialized
			if (null != managedForm) {
				clearValidators();
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
	 * {@inheritDoc}
	 */
	@Override
	public void registerValidator(ValidatorKey key, ValidationControlDecoration<?> validator) {
		if (null == key.getBusinessTransactionDefinition()) {
			BusinessTransactionDefinition businessTxDef = getCorrespondingBusinessTransaction(key);
			key.setBusinessTransactionDefinition(businessTxDef);
		}
		getValidationControlDecorators().put(key, validator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performInitialValidation() {
		updateValidationMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		updateValidationMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<ValidatorKey, ValidationControlDecoration<?>> getValidationControlDecorators() {
		return validationControlDecorators;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterValidators(Set<ValidatorKey> keys) {
		for (ValidatorKey key : keys) {
			if (key.getControlIndex() < 0 && null != key.getAbstractExpression()) {
				removeValidatorsForExpressions(key.getAbstractExpression());
			} else if (key.getControlIndex() < 0 && null != key.getBusinessTransactionDefinition()) {
				Set<ValidatorKey> keysToRemove = new HashSet<>();
				for (Entry<ValidatorKey, ValidationControlDecoration<?>> entry : getValidationControlDecorators().entrySet()) {
					if (null != entry.getKey().getBusinessTransactionDefinition() && entry.getKey().getBusinessTransactionDefinition().equals(key.getBusinessTransactionDefinition())) {
						keysToRemove.add(entry.getKey());
					}
				}
				for (ValidatorKey keyToRemove : keysToRemove) {
					getValidationControlDecorators().remove(keyToRemove);
				}
			} else if (key.getControlIndex() >= 0) {
				getValidationControlDecorators().remove(key);
				updateValidationMessage();
			}
		}

		updateValidationMessage();
	}

	/**
	 * Clears all validators.
	 */
	protected void clearValidators() {
		managedForm.getForm().getMessageManager().removeAllMessages();
		getValidationControlDecorators().clear();
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
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					tableViewer.setInput(getApplication().getBusinessTransactionDefinitions());
					tableViewer.refresh();
					tableViewer.setSelection(selection, true);
					tableViewer.getTable().setFocus();
					tableViewer.getTable().layout();
				}
			});
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
		ApplicationDefinition application = getApplication();
		int currentIndex = application.getBusinessTransactionDefinitions().indexOf(businessTransaction);
		int newIndex = currentIndex;
		if (up) {
			newIndex--;
		} else {
			newIndex++;
		}
		if (newIndex >= 0 && newIndex < application.getBusinessTransactionDefinitions().size() - 1) {
			try {
				application.moveBusinessTransactionDefinition(businessTransaction, newIndex);
				tableViewer.refresh();
				for (TableItemControlDecoration<BusinessTransactionDefinition> decoration : tableItemControlDecorations) {
					decoration.hide();
				}
				markDirty();
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
		if (selection instanceof StructuredSelection && !selection.isEmpty()) {
			boolean itemsDeleted = false;
			for (Object selectedElement : ((StructuredSelection) selection).toList()) {
				BusinessTransactionDefinition businessTxDefinition = (BusinessTransactionDefinition) selectedElement;
				if (businessTxDefinition.getId() != BusinessTransactionDefinition.DEFAULT_ID) {
					getApplication().deleteBusinessTransactionDefinition(businessTxDefinition);
					removeDecoratorsForDeletedBusinessTxDefinition(businessTxDefinition);
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
	 * Removes {@link ValidationControlDecoration} instances that are associated with the given
	 * {@link AbstractExpression}, either as direct belonging or as a descendant of an
	 * {@link IContainerExpression}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} identifying the {@link ValidationControlDecoration}
	 *            instances to be removed
	 */
	private void removeValidatorsForExpressions(AbstractExpression expression) {
		if (expression instanceof IContainerExpression) {
			for (AbstractExpression childExpression : ((IContainerExpression) expression).getOperands()) {
				removeValidatorsForExpressions(childExpression);
			}
		} else {
			Set<ValidatorKey> keysToRemove = new HashSet<>();
			for (Entry<ValidatorKey, ValidationControlDecoration<?>> validatorEntry : validationControlDecorators.entrySet()) {
				if (expression.equals(validatorEntry.getKey().getAbstractExpression())) {
					keysToRemove.add(validatorEntry.getKey());
				}
			}

			for (ValidatorKey key : keysToRemove) {
				validationControlDecorators.remove(key);
				managedForm.getForm().getMessageManager().removeMessage(key.hashCode());
			}
		}
	}

	/**
	 * Updates the validation error message for this page.
	 */
	private void updateValidationMessage() {
		Map<BusinessTransactionDefinition, String> shortMessages = getShortErrorMessages();
		for (Entry<BusinessTransactionDefinition, String> errorEntry : shortMessages.entrySet()) {
			managedForm.getForm().getMessageManager().addMessage(errorEntry.getKey(), errorEntry.getValue(), null, IMessageProvider.ERROR);
			validationErrorMessageKeys.add(errorEntry.getKey());
		}

		// remove obsolete messages
		Set<BusinessTransactionDefinition> keySet = shortMessages.keySet();
		for (BusinessTransactionDefinition businessTxDef : validationErrorMessageKeys) {
			if (!keySet.contains(businessTxDef)) {
				managedForm.getForm().getMessageManager().removeMessage(businessTxDef);
				hideTableItemControlDecoration(businessTxDef.getId());
			}
		}

		Map<BusinessTransactionDefinition, String> fullMessages = getFullErrorMessages(shortMessages.keySet());
		for (Entry<BusinessTransactionDefinition, String> errorEntry : fullMessages.entrySet()) {
			showTableItemControlDecoration(errorEntry.getKey().getId(), errorEntry.getValue());
		}
	}

	/**
	 * Creates aggregated short messages grouped by business transaction definitions.
	 *
	 * @return A Map of {@link BusinessTransactionDefinition} to corresponding validation error
	 *         messages.
	 */
	private Map<BusinessTransactionDefinition, String> getShortErrorMessages() {
		Map<BusinessTransactionDefinition, Integer> businessTxErrorsMap = new HashMap<>();
		for (Entry<ValidatorKey, ValidationControlDecoration<?>> entry : getValidationControlDecorators().entrySet()) {
			BusinessTransactionDefinition businessTxDefinition = entry.getKey().getBusinessTransactionDefinition();
			if (!entry.getValue().isValid() && null != businessTxDefinition) {
				if (!businessTxErrorsMap.containsKey(businessTxDefinition)) {
					businessTxErrorsMap.put(businessTxDefinition, 0);
				}
				businessTxErrorsMap.put(businessTxDefinition, businessTxErrorsMap.get(businessTxDefinition) + 1);
			}
		}
		Map<BusinessTransactionDefinition, String> result = new HashMap<>();
		for (Entry<BusinessTransactionDefinition, Integer> errorEntry : businessTxErrorsMap.entrySet()) {
			String errorsText = errorEntry.getValue() == 1 ? "One field contains a validation error" : errorEntry.getValue() + " fields contain validation errors";
			String message = "Business transaction definition '" + errorEntry.getKey().getBusinessTransactionDefinitionName() + "' (" + errorsText + ")";
			result.put(errorEntry.getKey(), message);
		}
		return result;
	}

	/**
	 * Creates an aggregated full messages grouped by groups for the passed set of
	 * {@link BusinessTransactionDefinition} instances.
	 *
	 * @param businessTxDefinitionsOfInterest
	 *            a set of {@link BusinessTransactionDefinition} instances for which the full
	 *            messages shell be created.
	 *
	 * @return A Map of {@link BusinessTransactionDefinition} to corresponding full validation error
	 *         messages.
	 */
	private Map<BusinessTransactionDefinition, String> getFullErrorMessages(Set<BusinessTransactionDefinition> businessTxDefinitionsOfInterest) {
		Map<BusinessTransactionDefinition, String> result = new HashMap<>();
		for (BusinessTransactionDefinition businessTxDef : businessTxDefinitionsOfInterest) {
			Map<Integer, Integer> errorCountPerExpression = new HashMap<>();
			Map<Integer, String> elementNames = new HashMap<>();
			for (Entry<ValidatorKey, ValidationControlDecoration<?>> validatorEntry : getValidationControlDecorators().entrySet()) {
				if (businessTxDef.equals(validatorEntry.getKey().getBusinessTransactionDefinition())) {
					if (!validatorEntry.getValue().isValid()) {
						if (!errorCountPerExpression.containsKey(validatorEntry.getKey().getGroupId())) {
							errorCountPerExpression.put(validatorEntry.getKey().getGroupId(), 0);
						}
						if (!elementNames.containsKey(validatorEntry.getKey().getGroupId())) {
							elementNames.put(validatorEntry.getKey().getGroupId(), validatorEntry.getKey().getGroupName());
						}
						errorCountPerExpression.put(validatorEntry.getKey().getGroupId(), errorCountPerExpression.get(validatorEntry.getKey().getGroupId()) + 1);
					}
				}
			}
			StringBuilder messageBuilder = new StringBuilder();
			for (Entry<Integer, Integer> errorCountEntry : errorCountPerExpression.entrySet()) {
				messageBuilder.append(elementNames.get(errorCountEntry.getKey()));
				messageBuilder.append(errorCountEntry.getValue() == 1 ? " (One field contains a validation error)\n" : " (" + errorCountEntry.getValue() + " fields contain validation errors)\n");
			}
			result.put(businessTxDef, messageBuilder.toString());
		}
		return result;
	}

	/**
	 * Retrieves the {@link BusinessTransactionDefinition} from the {@link ValidatorKey} that
	 * identifies a {@link ValidationControlDecoration}.
	 *
	 * @param key
	 *            {@link ValidatorKey} instance to retrieve the
	 *            {@link BusinessTransactionDefinition} from
	 * @return The retrieved {@link BusinessTransactionDefinition}.
	 */
	private BusinessTransactionDefinition getCorrespondingBusinessTransaction(ValidatorKey key) {
		if (null != key.getBusinessTransactionDefinition()) {
			return key.getBusinessTransactionDefinition();
		} else if (null != key.getAbstractExpression()) {
			for (BusinessTransactionDefinition bTxDef : applicationDefinition.getBusinessTransactionDefinitions()) {
				AbstractExpression currentExpression = bTxDef.getMatchingRuleExpression();
				if (currentExpression.equals(key.getAbstractExpression())
						|| (currentExpression instanceof IContainerExpression && isDescendantOf(key.getAbstractExpression(), (IContainerExpression) currentExpression))) {
					return bTxDef;
				}
			}
		}
		try {
			return applicationDefinition.getBusinessTransactionDefinition(selectedBusinessTransactionId);
		} catch (BusinessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Recursively checks whether the given {@link AbstractExpression} instance is a descendant of
	 * the passed {@link IContainerExpression} instance.
	 *
	 * @param expression
	 *            {@link AbstractExpression} instance
	 * @param containerExpression
	 *            {@link IContainerExpression} instance
	 * @return true, if the given {@link AbstractExpression} instance is a descendant of the passed
	 *         {@link IContainerExpression} instance. Otherwise, false.
	 */
	private boolean isDescendantOf(AbstractExpression expression, IContainerExpression containerExpression) {
		for (AbstractExpression currentExpression : containerExpression.getOperands()) {
			if (currentExpression.equals(expression)) {
				return true;
			}
			if (currentExpression instanceof IContainerExpression && isDescendantOf(expression, (IContainerExpression) currentExpression)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes all decorators for a deleted {@link BusinessTransactionDefinition} instance.
	 *
	 * @param businessTxDefinition
	 *            deleted {@link BusinessTransactionDefinition}
	 */
	private void removeDecoratorsForDeletedBusinessTxDefinition(BusinessTransactionDefinition businessTxDefinition) {
		Set<ValidatorKey> keysToRemove = new HashSet<>();
		for (ValidatorKey key : getValidationControlDecorators().keySet()) {
			if (businessTxDefinition.equals(key.getBusinessTransactionDefinition())) {
				keysToRemove.add(key);
			}
		}
		for (ValidatorKey key : keysToRemove) {
			getValidationControlDecorators().remove(key);
		}
	}

	/**
	 * Shows the error decoration for the {@link BusinessTransactionDefinition}.
	 *
	 * @param businessTransactionDefinitionId
	 *            identifier of the {@link BusinessTransactionDefinition}.
	 * @param message
	 *            Message to display.
	 * @return
	 */
	private void showTableItemControlDecoration(int businessTransactionDefinitionId, String message) {
		if (null != tableViewer) {
			// first check if we have it, if so shown
			for (TableItemControlDecoration<BusinessTransactionDefinition> decoration : tableItemControlDecorations) {
				if (businessTransactionDefinitionId == decoration.getData().getId()) { // NOPMD
					decoration.show();
					decoration.setDescriptionText(message);
					return;
				}
			}

			// if not find appropriate table item to place it
			for (TableItem tableItem : tableViewer.getTable().getItems()) {
				if (((BusinessTransactionDefinition) tableItem.getData()).getId() == businessTransactionDefinitionId) { // NOPMD
					final TableItemControlDecoration<BusinessTransactionDefinition> decoration = new TableItemControlDecoration<BusinessTransactionDefinition>(tableItem);
					decoration.setDisposeListener(new DisposeListener() {
						@Override
						public void widgetDisposed(DisposeEvent e) {
							tableItemControlDecorations.remove(decoration);
							decoration.hide();
							decoration.dispose();
						}
					});
					decoration.show();
					decoration.setDescriptionText(message);
					tableItemControlDecorations.add(decoration);
					return;
				}
			}
		}
	}

	/**
	 * Hides the error decoration for the {@link BusinessTransactionDefinition}.
	 *
	 * @param businessTransactionDefinitionId
	 *            identifier of the {@link BusinessTransactionDefinition}.
	 */
	private void hideTableItemControlDecoration(int businessTransactionDefinitionId) {
		if (null != tableViewer) {
			for (TableItemControlDecoration<BusinessTransactionDefinition> decoration : tableItemControlDecorations) {
				if (businessTransactionDefinitionId == decoration.getData().getId()) { // NOPMD
					decoration.hide();
					return;
				}
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
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_EDIT));
			setText("Edit (F2)");
		}

		@Override
		public void run() {
			editSelectedBusinessTransaction();
		}
	}
}
