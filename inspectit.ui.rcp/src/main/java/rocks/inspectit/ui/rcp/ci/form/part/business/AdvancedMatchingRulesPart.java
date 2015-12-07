package rocks.inspectit.ui.rcp.ci.form.part.business;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

import com.google.common.base.Objects;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.IContainerExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.AndExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.BooleanExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.NotExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.OrExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.impl.IMatchingRuleProvider;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodParameterValueSource;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.action.MenuAction;
import rocks.inspectit.ui.rcp.ci.DirtyStateDelegatingManagedForm;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.BooleanExpressionType;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.ci.form.part.business.OperandTreeContentProvider.TreeInput;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.util.RemoveSelection;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.IControlValidationListener;
import rocks.inspectit.ui.rcp.validation.TreeItemControlDecorationManager;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/***
 *
 * @author Alexander Wert
 *
 */
public class AdvancedMatchingRulesPart extends MasterDetailsBlock implements IMatchingRulesPart, ISelectionChangedListener, IDetailsModifiedListener<AbstractExpression> {

	/**
	 * {@link IManagedForm} used for this master details block. We use a
	 * {@link DirtyStateDelegatingManagedForm} instance as this {@link MasterDetailsBlock} is
	 * embedded into another master details block, which would cause some problems with selections
	 * when using the same managed form for both master details blocks.
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
	private IMatchingRuleProvider ruleProvider;

	/**
	 * Validation manager that is responsible for showing control validation messages and delegating
	 * these messages to parent UI elements.
	 */
	private final DelegatingValidationManager validationManager;

	/**
	 * {@link TreeItemControlDecorationManager} instance for managin tree decorations.
	 */
	private final TreeItemControlDecorationManager treeControlDecorationManager = new TreeItemControlDecorationManager();

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
	 * @param validationManager
	 *            {@link AbstractValidationManager} instance of the parent UI element to be notified
	 *            on validation state changes.
	 */
	public AdvancedMatchingRulesPart(String title, AbstractValidationManager<AbstractExpression> validationManager) {
		this.title = title;
		this.validationManager = new DelegatingValidationManager(validationManager);
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

		super.createContent(new DirtyStateDelegatingManagedForm(managedForm, section), main);
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

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(new OperandLabelProvider());
		treeViewer.addSelectionChangedListener(this);
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					ISelection selection = treeViewer.getSelection();
					if ((selection instanceof StructuredSelection) && !selection.isEmpty()) {
						AbstractExpression expression = (AbstractExpression) ((StructuredSelection) selection).getFirstElement();
						deleteExpression(expression);
					}
				}
			}
		});
		setTreeInput(new BooleanExpression(false));
		treeValidation = new ValidationControlDecoration<Tree>(tree, validationManager) {

			@Override
			protected boolean validate(Tree control) {
				if (ArrayUtils.isEmpty(control.getItems())) {
					return false;
				} else {
					for (TreeItem childItem : control.getItems()) {
						if (hasLeafElement(childItem)) {
							return true;
						}
					}
					return false;
				}
			}

			/**
			 * Checks whether the given TreeItem contains a leaf node that is not a container
			 * expression
			 *
			 * @param item
			 *            item to check
			 * @return true, if the sub-tree under the given item contains a leaf node that is not a
			 *         container expression
			 */
			private boolean hasLeafElement(TreeItem item) {
				if (ArrayUtils.isEmpty(item.getItems()) && !(item.getData() instanceof IContainerExpression)) {
					return true;
				} else if (ArrayUtils.isNotEmpty(item.getItems())) {
					for (TreeItem childItem : item.getItems()) {
						if (hasLeafElement(childItem)) {
							return true;
						}
					}
				}
				return false;
			}
		};
		treeValidation.setDescriptionText("At least one leaf expression must be defined!");
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
		if (!Objects.equal(this.ruleProvider, ruleProvider)) {
			this.ruleProvider = ruleProvider;
			setTreeInput(ruleProvider.getMatchingRuleExpression());

			if (null != rootExpression) {
				treeViewer.setSelection(new StructuredSelection(rootExpression));
			}
			resizeColumns();
		}
		treeValidation.executeValidation(true);
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
		treeViewer.refresh(true);
		resizeColumns();
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
			if (selection.isEmpty() && (null == rootExpression)) {
				createToolbarAction.setEnabled(true);
				createContextMenuAction.setEnabled(true);
				removeAction.setEnabled(false);
			} else if (selection.isEmpty() && (null != rootExpression)) {
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
	public void contentModified(AbstractExpression modifiedElement) {
		contentChanged();
	}

	/**
	 * Gets {@link #validationManager}.
	 *
	 * @return {@link #validationManager}
	 */
	public AbstractValidationManager<AbstractExpression> getValidationManager() {
		return validationManager;
	}

	/**
	 * Refreshes the contents of this part.
	 */
	private void contentChanged() {
		treeViewer.refresh(true);
		resizeColumns();
		markDirty();
		commit(false);
	}

	/**
	 * Sets the tree input and performs a validation.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to be used for the tree input.
	 */
	private void setTreeInput(AbstractExpression expression) {
		this.rootExpression = expression;
		treeViewer.setInput(new TreeInput(expression));
		if (null != treeValidation) {
			treeValidation.executeValidation();
		}
		treeViewer.expandAll();
		performInitialValidation(rootExpression);
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
	 * Adds the given {@link AbstractExpression} instance as a child to the currently selected
	 * expression, if possible. If no expression is selected, then the passed
	 * {@link AbstractExpression} instance is inserted as root.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to add.
	 */
	private void addExpression(AbstractExpression expression) {
		ISelection selection = treeViewer.getSelection();
		if ((selection instanceof StructuredSelection) && !selection.isEmpty()) {
			AbstractExpression parentExpression = (AbstractExpression) ((StructuredSelection) selection).getFirstElement();
			if (parentExpression instanceof IContainerExpression) {
				((IContainerExpression) parentExpression).addOperand(expression);
			}
			setTreeInput(rootExpression);
			treeViewer.expandToLevel(expression, 0);
			treeViewer.setSelection(new StructuredSelection(expression), true);
			treeValidation.executeValidation();
		} else {
			setTreeInput(expression);
			contentChanged();
			treeViewer.setSelection(new StructuredSelection(rootExpression), true);
		}
		contentChanged();
	}

	/**
	 * Removes the given expression from expression tree.
	 *
	 * @param expression
	 *            {@link AbstractExpression} instance to delete.
	 */
	private void deleteExpression(AbstractExpression expression) {
		Object parentExpression = treeContentProvider.getParent(expression);

		if (null == parentExpression) {
			setTreeInput(null);
		} else if (parentExpression instanceof IContainerExpression) {
			((IContainerExpression) parentExpression).removeOperand(expression);
			setTreeInput(rootExpression);
			treeViewer.setSelection(new StructuredSelection(parentExpression), true);
		}

		RemoveSelection removeSelection = new RemoveSelection(Collections.singletonList(expression));
		managedForm.fireSelectionChanged(this, removeSelection);

		// notify validation registry to update validation decorations and validation messages due
		// to the removal of this expression
		validationManager.validationStatesRemoved(expression);
		contentChanged();
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
	 * Performs initial validation of the tree elements without the need to create corresponding
	 * editing controls.
	 *
	 * @param expression
	 *            {@link AbstractExpression} instance to validate.
	 */
	private void performInitialValidation(AbstractExpression expression) {
		if (expression instanceof IContainerExpression) {
			for (AbstractExpression child : ((IContainerExpression) expression).getOperands()) {
				performInitialValidation(child);
			}
		} else if (expression instanceof StringMatchingExpression) {
			Set<ValidationState> validationStates = AbstractRuleEditingElement.validate((StringMatchingExpression) expression);
			for (ValidationState state : validationStates) {
				validationManager.validationStateChanged(expression, state);
			}
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
			if ((selection instanceof StructuredSelection) && !selection.isEmpty()) {
				final AbstractExpression expression = (AbstractExpression) ((StructuredSelection) selection).getFirstElement();
				deleteExpression(expression);
			}
		}
	}

	/**
	 * This class is an implementation of the {@link AbstractValidationManager} that delegates
	 * changes directly to a parent {@link AbstractValidationManager} instance and in addition shows
	 * validation messages in the tree viewer.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class DelegatingValidationManager extends AbstractValidationManager<AbstractExpression> implements IControlValidationListener {

		/**
		 * The {@link AbstractValidationManager} instance to delegate validation state changes to.
		 */
		private final AbstractValidationManager<AbstractExpression> delegateTo;

		/**
		 * Constructor.
		 *
		 * @param delegateTo
		 *            the {@link AbstractValidationManager} instance to delegate validation state
		 *            changes to.
		 */
		DelegatingValidationManager(AbstractValidationManager<AbstractExpression> delegateTo) {
			this.delegateTo = delegateTo;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validationStateChanged(AbstractExpression key, ValidationState newState) {
			if (null != delegateTo) {
				delegateTo.validationStateChanged(key, newState);
			}
			super.validationStateChanged(key, newState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validationStatesRemoved(AbstractExpression expression) {
			if (expression instanceof IContainerExpression) {
				for (AbstractExpression child : ((IContainerExpression) expression).getOperands()) {
					validationStatesRemoved(child);
				}
			} else {
				super.validationStatesRemoved(expression);
				if (null != delegateTo) {
					delegateTo.validationStatesRemoved(expression);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void notifyUpstream(AbstractExpression expression, Set<ValidationState> states) {
			// nothing to do here
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void showMessage(AbstractExpression expression, Set<ValidationState> states) {
			String message = "";
			for (ValidationState state : states) {
				message += state.getMessage() + "\n";
			}
			treeControlDecorationManager.showTreeItemControlDecoration(treeViewer, expression, message);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void hideMessage(AbstractExpression expression) {
			treeControlDecorationManager.hideTreeItemControlDecoration(treeViewer, expression);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
			if (null != delegateTo) {
				delegateTo.validationStateChanged(MatchingRulesEditingElementFactory.InvalidExpression.getInstance(),
						new ValidationState("emptyTree", valid, validationControlDecoration.getDescriptionText()));
			}
		}

	}
}
