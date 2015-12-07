/****/
package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.IContainerExpression;
import info.novatec.inspectit.ci.business.expression.impl.AndExpression;
import info.novatec.inspectit.ci.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.expression.impl.NotExpression;
import info.novatec.inspectit.ci.business.expression.impl.OrExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.impl.IMatchingRuleProvider;
import info.novatec.inspectit.ci.business.valuesource.impl.HttpParameterValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.MethodParameterValueSource;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.ci.DelegatingManagedForm;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.page.ValidatorKey;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.BooleanExpressionType;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import info.novatec.inspectit.rcp.ci.form.part.business.OperandTreeContentProvider.TreeInput;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractRuleEditingElement.IRuleEditingElementModifiedListener;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.validation.TreeItemControlDecoration;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/***
 *
 * @author Alexander Wert
 *
 */
public class AdvancedMatchingRulesPart extends MasterDetailsBlock implements IMatchingRulesPart, ISelectionChangedListener, IValidatorRegistry, IRuleEditingElementModifiedListener {

	/**
	 * {@link IManagedForm} used for this master details block. We use a
	 * {@link DelegatingManagedForm} instance as this {@link MasterDetailsBlock} is embedded into
	 * another master details block, which would cause some problems with selections when using the
	 * same managed form for both master details blocks.
	 */
	private IManagedForm managedForm;

	/**
	 * Section control.
	 */
	private Section section;

	/**
	 * Tree viewer for displaying the advanced expression tree.
	 */
	private TreeViewer treeViewer;

	/**
	 * The root expression.
	 */
	private AbstractExpression rootExpression;

	/**
	 * {@link MenuAction} providing a menu for expression creation. This action is used in the
	 * toolbar (cannot be used in a context menu at the same time).
	 */
	private MenuAction createToolbarAction;

	/**
	 * {@link MenuAction} providing a sub-menu for expression creation. This action is used in the
	 * context menu (cannot be used in a toolbar at the same time).
	 */
	private MenuAction createContextMenuAction;

	/**
	 * Action for removal of an expression.
	 */
	private RemoveExpressionAction removeAction;

	/**
	 * Context menu manager.
	 */
	private MenuManager menuManager;

	/**
	 * Toolbar manager.
	 */
	private ToolBarManager toolBarManager;

	/**
	 * Label holding the description text.
	 */
	private Label descriptionLabel;

	/**
	 * Indicates whether this form part is editable or not.
	 */
	private boolean editable;

	/**
	 * Section title text.
	 */
	private final String title;
	/**
	 * Dirty state.
	 */
	private boolean dirty = false;

	/**
	 * Provider and receiver of the {@link AbstractExpression} instance edited in this form part.
	 */
	IMatchingRuleProvider ruleProvider;

	/**
	 * The {@link IValidatorRegistry} instance to delegate validator events to.
	 */
	private final IValidatorRegistry validatorRegistry;

	/**
	 * {@link TableEditor}s to handle the validation decoration on tree elements.
	 */
	private final Map<TreeItem, TreeItemControlDecoration<AbstractExpression>> treeItemControlDecorations = new HashMap<>();

	/**
	 * Content provider for the tree.
	 */
	private final OperandTreeContentProvider treeContentProvider = new OperandTreeContentProvider();

	/**
	 * {@link ValidationControlDecoration} instance used to check whether at least one expression is
	 * defined in the tree.
	 */
	private ValidationControlDecoration<Tree> treeValidation;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            The title of the section.
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 */
	public AdvancedMatchingRulesPart(String title, IValidatorRegistry validatorRegistry) {
		this.title = title;
		this.validatorRegistry = validatorRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContent(IManagedForm managedForm, Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();
		section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		section.setText(title);

		descriptionLabel = toolkit.createLabel(section, "");
		descriptionLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		section.setDescriptionControl(descriptionLabel);
		Composite main = toolkit.createComposite(section);
		main.setLayout(new GridLayout(1, false));
		section.setClient(main);

		super.createContent(new DelegatingManagedForm(managedForm, section), main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createMasterPart(IManagedForm managedForm, Composite parent) {
		this.managedForm = managedForm;
		sashForm.setOrientation(SWT.VERTICAL);

		FormToolkit toolkit = managedForm.getToolkit();
		Composite masterMain = toolkit.createComposite(parent);
		masterMain.setLayout(new GridLayout(1, false));

		Tree tree = toolkit.createTree(masterMain, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		treeValidation = new ValidationControlDecoration<Tree>(tree, validatorRegistry) {

			@Override
			protected boolean validate(Tree control) {
				return control.getItems() != null && control.getItems().length > 0;
			}
		};

		treeValidation.setDescriptionText("At least one expression must be defined!");
		ValidatorKey key = new ValidatorKey();
		key.setGroupName(title);
		validatorRegistry.registerValidator(key, treeValidation);

		treeViewer = new TreeViewer(tree);

		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(new OperandLabelProvider());
		treeViewer.addSelectionChangedListener(this);
		tree.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					ISelection selection = treeViewer.getSelection();
					if (selection instanceof StructuredSelection && !selection.isEmpty()) {
						AbstractExpression expression = (AbstractExpression) ((StructuredSelection) selection).getFirstElement();
						deleteExpression(expression);
					}
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPages(DetailsPart detailsPart) {
		this.detailsPart = detailsPart;
		detailsPart.registerPage(StringMatchingExpression.class, new AdvancedMatchingRulesDetails(this));
		detailsPart.registerPage(BooleanExpression.class, new AdvancedMatchingRulesDetails(this));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		toolBarManager = new ToolBarManager(SWT.HORIZONTAL);

		createToolbarAction = new MenuAction("Add", InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
		createToolbarAction.setEnabled(false);
		createContextMenuAction = new MenuAction("Add", InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
		createContextMenuAction.setEnabled(false);
		for (BooleanExpressionType boolenExprType : BooleanExpressionType.values()) {
			createToolbarAction.addAction(new CreateExpressionAction(boolenExprType));
			createContextMenuAction.addAction(new CreateExpressionAction(boolenExprType));
		}
		createToolbarAction.addContributionItem(new Separator());
		createContextMenuAction.addContributionItem(new Separator());
		for (MatchingRuleType matchingRuleType : MatchingRuleType.values()) {
			createToolbarAction.addAction(new CreateExpressionAction(matchingRuleType));
			createContextMenuAction.addAction(new CreateExpressionAction(matchingRuleType));
		}

		removeAction = new RemoveExpressionAction();
		removeAction.setEnabled(false);
		toolBarManager.add(removeAction);
		toolBarManager.add(createToolbarAction);

		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);
		toolBarManager.update(true);
		createToolbarAction.setRunTask(new MenuAction.ToolbarDropDownTask(toolbar));

		menuManager = new MenuManager();
		menuManager.add(createContextMenuAction);
		menuManager.add(removeAction);
		Control control = treeViewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);
		menuManager.update();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initContent(IMatchingRuleProvider ruleProvider) {
		this.ruleProvider = ruleProvider;
		rootExpression = ruleProvider.getMatchingRuleExpression();
		setTreeInput();
		treeViewer.expandAll();
		if (null != rootExpression) {
			treeViewer.setSelection(new StructuredSelection(rootExpression));
		}
		resizeColumns();
		performInitialValidation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		section.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return dirty || detailsPart.isDirty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			dirty = false;
		}
		detailsPart.commit(onSave);

		if (null != ruleProvider) {
			AbstractExpression expression = constructMatchingRuleExpression();
			ruleProvider.setMatchingRuleExpression(expression);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof AbstractExpression) {
			rootExpression = (AbstractExpression) input;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		treeViewer.getTree().setFocus();
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
		treeViewer.refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDescriptionText(String description) {
		descriptionLabel.setText(description);
		section.layout(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markDirty() {
		if (!dirty) {
			dirty = true;
			commit(false);
			managedForm.dirtyStateChanged();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return section;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ToolBarManager getToolbarManager() {
		return toolBarManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelection() instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) event.getSelection();
			if (selection.isEmpty() && null == rootExpression) {
				createToolbarAction.setEnabled(true);
				createContextMenuAction.setEnabled(true);
				removeAction.setEnabled(false);
			} else if (selection.isEmpty() && null != rootExpression) {
				createToolbarAction.setEnabled(false);
				createContextMenuAction.setEnabled(false);
				removeAction.setEnabled(false);
			} else if (!selection.isEmpty()) {
				AbstractExpression expression = (AbstractExpression) selection.getFirstElement();
				boolean createEnabled = (expression instanceof IContainerExpression) && ((IContainerExpression) expression).canAddOperand();
				createToolbarAction.setEnabled(createEnabled);
				createContextMenuAction.setEnabled(createEnabled);
				removeAction.setEnabled(true);
			}
			managedForm.fireSelectionChanged(this, selection);
			menuManager.update(true);
			toolBarManager.update(true);
		}
	}

	/**
	 * Gets {@link #editable}.
	 *
	 * @return {@link #editable}
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Sets {@link #editable}.
	 *
	 * @param editable
	 *            New value for {@link #editable}
	 */
	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;
		setEnabledState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		validatorRegistry.validationStateChanged(valid, validationControlDecoration);
		updateValidationMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<ValidatorKey, ValidationControlDecoration<?>> getValidationControlDecorators() {
		return validatorRegistry.getValidationControlDecorators();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerValidator(ValidatorKey controlId, ValidationControlDecoration<?> validator) {
		validatorRegistry.registerValidator(controlId, validator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performInitialValidation() {
		validatorRegistry.performInitialValidation();
		updateValidationMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterValidators(Set<ValidatorKey> keys) {
		validatorRegistry.unregisterValidators(keys);
		updateValidationMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contentModified() {
		updateTree();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void elementDisposed(AbstractRuleEditingElement<?> ruleComposite) {
		updateTree();
	}

	/**
	 * Sets the tree input and performs a validation.
	 */
	private void setTreeInput() {
		treeViewer.setInput(new TreeInput(rootExpression));
		treeValidation.executeValidation();
	}

	/**
	 * Constructs a {@link AbstractExpression} instance from the contents of this element controls.
	 *
	 * @return Returns a {@link AbstractExpression} instance.
	 */
	private AbstractExpression constructMatchingRuleExpression() {
		AbstractExpression expression = (null == rootExpression) ? new BooleanExpression(false) : rootExpression;
		expression.setAdvanced(true);
		return expression;
	}

	/**
	 *
	 */
	private void resizeColumns() {
		for (TreeColumn column : treeViewer.getTree().getColumns()) {
			column.pack();
		}
	}

	/**
	 * Updates tree.
	 */
	private void updateTree() {
		treeViewer.refresh();
		resizeColumns();
		markDirty();
	}

	/**
	 * Adds the given {@link AbstractExpression} instance as a child to the currently selected
	 * expression, if possible. If no expression is selected, then the passed
	 * {@link AbstractExpression} instance is inserted as root.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to add.
	 */
	private void addExpression(AbstractExpression expression) {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof StructuredSelection && !selection.isEmpty()) {
			AbstractExpression parentExpression = (AbstractExpression) ((StructuredSelection) selection).getFirstElement();
			if (parentExpression instanceof IContainerExpression) {
				((IContainerExpression) parentExpression).addOperand(expression);
			}
			updateTree();
			treeViewer.expandToLevel(expression, 0);
			treeViewer.setSelection(new StructuredSelection(expression), true);
		} else {
			rootExpression = expression;
			setTreeInput();
			updateTree();
			treeViewer.setSelection(new StructuredSelection(rootExpression), true);
		}
	}

	/**
	 * Removes the given expression from expression tree.
	 *
	 * @param expression
	 *            {@link AbstractExpression} instance to delete.
	 */
	private void deleteExpression(AbstractExpression expression) {
		AdvancedMatchingRulesDetails currentPage = (AdvancedMatchingRulesDetails) detailsPart.getCurrentPage();
		Object parentExpression = treeContentProvider.getParent(expression);

		if (null == parentExpression) {
			rootExpression = null; // NOPMD
			setTreeInput();
		}
		if (parentExpression instanceof IContainerExpression) {
			((IContainerExpression) parentExpression).removeOperand(expression);
		}
		if (null != parentExpression) {
			treeViewer.setSelection(new StructuredSelection(parentExpression), true);
		}
		if (null != currentPage) {
			currentPage.expressionDeleted(expression);
		}

		ValidatorKey key = new ValidatorKey();
		key.setAbstractExpression(expression);
		validatorRegistry.unregisterValidators(Collections.singleton(key));
		updateTree();
		updateValidationMessage();
	}

	/**
	 * Sets the enabled state of this part.
	 */
	private void setEnabledState() {
		toolBarManager.getControl().setEnabled(isEditable());
		treeViewer.getControl().getMenu().setEnabled(isEditable());
		treeViewer.getTree().setEnabled(isEditable());
	}

	/**
	 * Performs an update of the validation message for this part. Updates tree validation
	 * decorations.
	 */
	private void updateValidationMessage() {
		hideAllControlDecorations();
		Map<Long, String> expressionErrorsMap = new HashMap<Long, String>();
		for (Entry<ValidatorKey, ValidationControlDecoration<?>> entry : getValidationControlDecorators().entrySet()) {
			if (null != entry.getKey().getAbstractExpression()) {
				long expressionId = entry.getKey().getAbstractExpression().getId();
				if (!entry.getValue().isValid()) {
					if (!expressionErrorsMap.containsKey(expressionId)) {
						expressionErrorsMap.put(expressionId, "");
					}
					expressionErrorsMap.put(expressionId, expressionErrorsMap.get(expressionId) + entry.getValue().getDescriptionText() + "\n");
				}
			}

		}

		for (Entry<Long, String> errorEntry : expressionErrorsMap.entrySet()) {
			showTreeItemControlDecoration(errorEntry.getKey(), errorEntry.getValue());
		}
	}

	/**
	 * Shows the error decoration for the tree item specified by the passed expression ID.
	 *
	 * @param expressionId
	 *            Identifier of the {@link AbstractExpression} for which the error decoration shell
	 *            be shown
	 * @param message
	 *            Message to display.
	 */
	private void showTreeItemControlDecoration(Long expressionId, String message) {
		if (null == treeViewer) {
			return;
		}

		List<TreeItem> treeItems = getAllTreeItems();
		TreeItem targetTreeItem = null;
		for (TreeItem treeItem : treeItems) {
			if (((AbstractExpression) treeItem.getData()).getId() == expressionId) {
				targetTreeItem = treeItem;
				break;
			}
		}

		while (null != targetTreeItem) {
			TreeItemControlDecoration<AbstractExpression> decoration = treeItemControlDecorations.get(targetTreeItem);
			if (null == decoration) {
				decoration = attachDecoration(targetTreeItem);
			}
			decoration.show();
			decoration.setDescriptionText(message);
			targetTreeItem = targetTreeItem.getParentItem();
			message = "One of the child items has an error.";
		}
	}

	/**
	 * Attaches a decoration control to the specified tree item with the passed message.
	 *
	 * @param treeItem
	 *            {@link TreeItem} to attach the decoration to
	 * @return attached {@link TreeItemControlDecoration} instance
	 */
	private TreeItemControlDecoration<AbstractExpression> attachDecoration(final TreeItem treeItem) {
		final TreeItemControlDecoration<AbstractExpression> decoration = new TreeItemControlDecoration<AbstractExpression>(treeItem);
		decoration.setDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				// in any case hide, dispose and remove
				treeItemControlDecorations.remove(treeItem);
				decoration.dispose();
			}
		});
		treeItemControlDecorations.put(treeItem, decoration);
		return decoration;
	}

	/**
	 * Returns all tree items (also the nested ones) of the tree associated with the
	 * {@link #treeViewer}.
	 *
	 * @return List of all tree items.
	 */
	private List<TreeItem> getAllTreeItems() {
		List<TreeItem> treeItems = new ArrayList<>();
		for (TreeItem item : treeViewer.getTree().getItems()) {
			treeItems.add(item);
		}

		int i = 0;
		while (i < treeItems.size()) {
			TreeItem parent = treeItems.get(i);
			for (TreeItem item : parent.getItems()) {
				treeItems.add(item);
			}
			i++;
		}
		return treeItems;
	}

	/**
	 * Hides all error decorations.
	 */
	private void hideAllControlDecorations() {
		if (null == treeViewer) {
			return;
		}

		// remove if it's there
		for (TreeItemControlDecoration<AbstractExpression> decoration : treeItemControlDecorations.values()) {
			decoration.hide();
		}
	}

	/**
	 * Label provider for the tree viewer that displays the expression tree.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static final class OperandLabelProvider extends StyledCellIndexLabelProvider {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			StyledString styledString = new StyledString();
			if (element instanceof AndExpression) {
				styledString.append("AND");
				styledString.append(" (" + ((AndExpression) element).getNumberOfChildExpressions() + ")", StyledString.QUALIFIER_STYLER);
			} else if (element instanceof OrExpression) {
				styledString.append("OR");
				styledString.append(" (" + ((OrExpression) element).getNumberOfChildExpressions() + ")", StyledString.QUALIFIER_STYLER);
			} else if (element instanceof NotExpression) {
				styledString.append("NOT");
			} else if (element instanceof BooleanExpression) {
				styledString.append("Boolean");
				styledString.append(" (" + ((BooleanExpression) element).isValue() + ")", StyledString.QUALIFIER_STYLER);
			} else if (element instanceof StringMatchingExpression) {
				StringMatchingExpression stringMatchingExpression = (StringMatchingExpression) element;
				MatchingRuleType type = MatchingRulesEditingElementFactory.getMatchingRuleType(stringMatchingExpression);
				styledString.append(type.toString());
				switch (type) {
				case HTTP_PARAMETER:
					styledString.append(" (Parameter \"" + ((HttpParameterValueSource) stringMatchingExpression.getStringValueSource()).getParameterName() + "\" "
							+ stringMatchingExpression.getMatchingType().toString() + " \"" + stringMatchingExpression.getSnippet() + "\")", StyledString.QUALIFIER_STYLER);
					break;
				case HTTP_URI:
					styledString.append(" (URI " + stringMatchingExpression.getMatchingType().toString() + " \"" + stringMatchingExpression.getSnippet() + "\")", StyledString.QUALIFIER_STYLER);
					break;
				case IP:
					styledString.append(" (IP " + stringMatchingExpression.getMatchingType().toString() + " \"" + stringMatchingExpression.getSnippet() + "\")", StyledString.QUALIFIER_STYLER);
					break;
				case METHOD_SIGNATURE:
					styledString.append(" (Method signature " + stringMatchingExpression.getMatchingType().toString() + " \"" + stringMatchingExpression.getSnippet() + "\")",
							StyledString.QUALIFIER_STYLER);
					break;
				case METHOD_PARAMETER:
					String methodSignature = ((MethodParameterValueSource) stringMatchingExpression.getStringValueSource()).getMethodSignature();
					int startFromIndex = methodSignature.lastIndexOf('.', methodSignature.indexOf('('));
					String method = methodSignature.substring(startFromIndex + 1);
					int parameterIndex = ((MethodParameterValueSource) stringMatchingExpression.getStringValueSource()).getParameterIndex();
					styledString.append(" (Parameter " + parameterIndex + " of method \"" + method + "\" " + stringMatchingExpression.getMatchingType().toString() + " \""
							+ stringMatchingExpression.getSnippet() + "\")", StyledString.QUALIFIER_STYLER);
					break;
				default:
					break;
				}
			}
			return styledString;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			if (element instanceof AbstractExpression) {
				String imageKey = MatchingRulesEditingElementFactory.getRulesExpressionType((AbstractExpression) element).getImageKey();
				return InspectIT.getDefault().getImage(imageKey);
			}
			return super.getColumnImage(element, index);
		}
	}

	/**
	 * This action creates an expression depending on the type of {@link BooleanExpressionType} or
	 * {@link MatchingRuleType}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class CreateExpressionAction extends Action {
		/**
		 * {@link IRulesExpressionType}.
		 */
		private final IRulesExpressionType type;

		/**
		 * Constructor.
		 *
		 * @param type
		 *            {@link IRulesExpressionType} describing which type of expression to create.
		 */
		CreateExpressionAction(IRulesExpressionType type) {
			this.type = type;
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(type.getImageKey()));
			setText(type.toString());
		}

		@Override
		public void run() {
			AbstractExpression expression = MatchingRulesEditingElementFactory.createExpression(type);
			addExpression(expression);
		}
	}

	/**
	 * This action removes an expression from the expression tree.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class RemoveExpressionAction extends Action {

		/**
		 * Constructor.
		 */
		RemoveExpressionAction() {
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DELETE));
			setText("Delete (DEL)");
		}

		@Override
		public void run() {
			ISelection selection = treeViewer.getSelection();
			if (selection instanceof StructuredSelection && !selection.isEmpty()) {
				AbstractExpression expression = (AbstractExpression) ((StructuredSelection) selection).getFirstElement();
				deleteExpression(expression);
			}
		}
	}
}
