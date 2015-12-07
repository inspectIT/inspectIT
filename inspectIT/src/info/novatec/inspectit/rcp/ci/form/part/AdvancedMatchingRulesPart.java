/****/
package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.AndExpression;
import info.novatec.inspectit.ci.business.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.impl.HttpParameterValueSource;
import info.novatec.inspectit.ci.business.impl.NotExpression;
import info.novatec.inspectit.ci.business.impl.OrExpression;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.AbstractRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.AbstractRuleEditingElement.RuleEditingElementModifiedListener;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory.BooleanExpressionType;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/***
 *
 * @author Alexander Wert
 *
 */
public class AdvancedMatchingRulesPart extends MasterDetailsBlock implements IMatchingRulesPart, ISelectionChangedListener {

	/**
	 * {@link IManagedForm} used for this master details block. We use a
	 * {@link DelegatingManagedForm} instance as this {@link MasterDetailsBlock} is embedded into
	 * another master details block, which would cause some problems with selections when using the
	 * same managed form for both master details blocks.
	 */
	private final IManagedForm managedForm;

	/**
	 * Section control.
	 */
	private final Section section;

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
	private MenuAction createAction;

	/**
	 * {@link MenuAction} providing a sub-menu for expression creation. This action is used in the
	 * context menu (cannot be used in a toolbar at the same time).
	 */
	private MenuAction createSubMenuAction;

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
	 * Indicates whether this form part is editable or not.
	 */
	private final boolean editable;

	/**
	 * Dirty state.
	 */
	private boolean dirty = false;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            The title of the section.
	 * @param parent
	 *            parent {@link Composite}.
	 * @param managedForm
	 *            the parent {@link IManagedForm} to delegate dirty state notifications to.
	 * @param editable
	 *            if false, then this part is read-only
	 */
	public AdvancedMatchingRulesPart(String title, Composite parent, IManagedForm managedForm, boolean editable) {
		this.editable = editable;
		FormToolkit toolkit = managedForm.getToolkit();
		section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		section.setText(title);
		this.managedForm = new DelegatingManagedForm(managedForm, section);

		Composite main = toolkit.createComposite(section);
		main.setLayout(new GridLayout(1, false));
		createContent(this.managedForm, main);

		section.setClient(main);
		setEnabledState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createMasterPart(IManagedForm managedForm, Composite parent) {
		sashForm.setOrientation(SWT.VERTICAL);

		FormToolkit toolkit = managedForm.getToolkit();
		Composite masterMain = toolkit.createComposite(parent);
		masterMain.setLayout(new GridLayout(1, false));

		Tree tree = toolkit.createTree(masterMain, SWT.V_SCROLL | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		treeViewer = new TreeViewer(tree);

		TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		viewerColumn.getColumn().setMoveable(false);
		viewerColumn.getColumn().setResizable(false);
		viewerColumn.getColumn().setText("Operand");

		treeViewer.setContentProvider(new OperandTreeContentProvider());
		treeViewer.setLabelProvider(new OperandLabelProvider());
		treeViewer.addSelectionChangedListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPages(DetailsPart detailsPart) {
		this.detailsPart = detailsPart;
		detailsPart.registerPage(StringMatchingExpression.class, new RuleDetails());
		detailsPart.registerPage(BooleanExpression.class, new RuleDetails());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		toolBarManager = new ToolBarManager(SWT.HORIZONTAL);

		createAction = new MenuAction("Add", InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
		createAction.setEnabled(false);
		createSubMenuAction = new MenuAction("Add", InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
		createSubMenuAction.setEnabled(false);
		for (BooleanExpressionType boolenExprType : BooleanExpressionType.values()) {
			createAction.addAction(new CreateBooleanExpressionAction(boolenExprType));
			createSubMenuAction.addAction(new CreateBooleanExpressionAction(boolenExprType));
		}
		createAction.addContributionItem(new Separator());
		createSubMenuAction.addContributionItem(new Separator());
		for (MatchingRuleType matchingRuleType : MatchingRuleType.values()) {
			createAction.addAction(new CreateMatchingRuleAction(matchingRuleType));
			createSubMenuAction.addAction(new CreateMatchingRuleAction(matchingRuleType));
		}

		removeAction = new RemoveExpressionAction();
		removeAction.setEnabled(false);
		toolBarManager.add(removeAction);
		toolBarManager.add(createAction);

		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);
		toolBarManager.update(true);

		menuManager = new MenuManager();
		menuManager.add(createSubMenuAction);
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
	public AbstractExpression constructMatchingRuleExpression() {
		return (null == rootExpression) ? new BooleanExpression(false) : rootExpression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initContent(AbstractExpression matchingRuleExpression) {
		rootExpression = matchingRuleExpression;
		treeViewer.setInput(new TreeInput(rootExpression));
		treeViewer.expandAll();
		treeViewer.setSelection(new StructuredSelection(rootExpression));
		resizeColumns();
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
		section.setDescription(description);
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
			if (selection.isEmpty() && null == rootExpression) {
				createAction.setEnabled(true);
				createSubMenuAction.setEnabled(true);
				removeAction.setEnabled(false);
			} else if (selection.isEmpty() && null != rootExpression) {
				createAction.setEnabled(false);
				createSubMenuAction.setEnabled(false);
				removeAction.setEnabled(false);
			} else if (!selection.isEmpty()) {
				AbstractExpression expression = (AbstractExpression) selection.getFirstElement();
				boolean createEnabled = expression.getNumberOfChildExpressions() < expression.getSupportedNumberOfChildExpressions();
				createAction.setEnabled(createEnabled);
				createSubMenuAction.setEnabled(createEnabled);
				removeAction.setEnabled(true);
			}
			managedForm.fireSelectionChanged(this, selection);
			menuManager.update(true);
			toolBarManager.update(true);
			if (isDirty()) {
				updateTree();
			}
		}
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

			if (null == parentExpression) {
				rootExpression = expression;
				treeViewer.setInput(new TreeInput(rootExpression));
			} else {
				if (parentExpression instanceof AndExpression) {
					((AndExpression) parentExpression).getOperands().add(expression);
				} else if (parentExpression instanceof OrExpression) {
					((OrExpression) parentExpression).getOperands().add(expression);
				} else if (parentExpression instanceof NotExpression) {
					((NotExpression) parentExpression).setOperand(expression);
				}
				updateTree();
				treeViewer.setSelection(new StructuredSelection(expression), true);
			}
		} else if (null != selection && selection.isEmpty()) {
			rootExpression = expression;
			treeViewer.setInput(new TreeInput(rootExpression));
			updateTree();
			treeViewer.setSelection(new StructuredSelection(rootExpression), true);
		}
	}

	/**
	 * Searches parent for the target {@link AbstractExpression} beginning from the root element.
	 * This is a recursive method.
	 *
	 * @param parentCandidate
	 *            parent candidate to check
	 * @param target
	 *            target {@link AbstractExpression} instance
	 * @return the parent of the target {@link AbstractExpression} instance or null, if target is a
	 *         root element.
	 */
	private static AbstractExpression searchParent(AbstractExpression parentCandidate, AbstractExpression target) {
		if (null == parentCandidate || parentCandidate.equals(target)) {
			return null;
		}

		List<? extends AbstractExpression> children = null;
		if (parentCandidate instanceof AndExpression) {
			children = ((AndExpression) parentCandidate).getOperands();
		} else if (parentCandidate instanceof OrExpression) {
			children = ((OrExpression) parentCandidate).getOperands();
		} else if (parentCandidate instanceof NotExpression) {
			AbstractExpression operand = ((NotExpression) parentCandidate).getOperand();
			if (target.equals(operand)) {
				return parentCandidate;
			} else {
				return searchParent(operand, target);
			}
		}
		if (null == children) {
			return null;
		}
		for (AbstractExpression child : children) {
			if (target.equals(child)) {
				return parentCandidate;
			} else {
				AbstractExpression childResult = searchParent(child, target);
				if (null != childResult) {
					return childResult;
				}
			}
		}

		return null;

	}

	/**
	 * Sets the enabled state of this part.
	 */
	private void setEnabledState() {
		section.setEnabled(editable);
		toolBarManager.getControl().setEnabled(editable);
		treeViewer.getControl().getMenu().setEnabled(editable);
		treeViewer.getTree().setEnabled(editable);
	}

	/**
	 * This managed form delegates dirtyState changes to a {@link ManagedForm} form.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static class DelegatingManagedForm extends ManagedForm {
		/**
		 * The parent {@link ManagedForm}.
		 */
		private final IManagedForm parentManagedForm;

		/**
		 * Constructor.
		 *
		 * @param parentManagedForm
		 *            the parent {@link ManagedForm}.
		 * @param parent
		 *            parent {@link Composite}
		 */
		DelegatingManagedForm(IManagedForm parentManagedForm, Composite parent) {
			super(parent);
			this.parentManagedForm = parentManagedForm;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dirtyStateChanged() {
			super.dirtyStateChanged();
			parentManagedForm.dirtyStateChanged();
		}
	}

	/**
	 * Wrapper class for the tree input to avoid infinite recursion that may occur when using the
	 * same class for the input and the tree elements.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static class TreeInput {
		/**
		 * Root expression.
		 */
		private final AbstractExpression expression;

		/**
		 * Constructor.
		 *
		 * @param expression
		 *            root expression.
		 */
		TreeInput(AbstractExpression expression) {
			this.expression = expression;
		}

		/**
		 * Gets {@link #expression}.
		 *
		 * @return {@link #expression}
		 */
		public AbstractExpression getExpression() {
			return expression;
		}
	}

	/**
	 * Content provider for the tree viewer that displays the expression tree.
	 *
	 * @author Alexander Wert
	 *
	 */
	private final class OperandTreeContentProvider implements ITreeContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof TreeInput) {
				AbstractExpression expression = ((TreeInput) inputElement).getExpression();
				if (null != expression) {
					Object[] elements = new Object[1];
					elements[0] = expression;
					return elements;
				}
			}
			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof AbstractExpression) {
				if (parentElement instanceof AndExpression) {
					return ((AndExpression) parentElement).getOperands().toArray();
				} else if (parentElement instanceof OrExpression) {
					return ((OrExpression) parentElement).getOperands().toArray();
				} else if (parentElement instanceof NotExpression) {
					Object[] children = new Object[1];
					children[0] = ((NotExpression) parentElement).getOperand();
					return children;
				}
			}
			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getParent(Object element) {
			if (element instanceof AbstractExpression) {
				return searchParent(rootExpression, (AbstractExpression) element);
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof AbstractExpression) {
				if (element instanceof AndExpression) {
					return !((AndExpression) element).getOperands().isEmpty();
				} else if (element instanceof OrExpression) {
					return !((OrExpression) element).getOperands().isEmpty();
				} else if (element instanceof NotExpression) {
					return ((NotExpression) element).getOperand() != null;
				}
			}
			return false;
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
			if (element instanceof AndExpression) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_AMP);
			} else if (element instanceof OrExpression) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_OR);
			} else if (element instanceof NotExpression) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_EXCLAMATION);
			} else if (element instanceof BooleanExpression) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_CIRCLE);
			} else if (element instanceof StringMatchingExpression) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SHEET);
			}
			return super.getColumnImage(element, index);
		}
	}

	/**
	 * This action creates a boolean expression depending on the type of
	 * {@link BooleanExpressionType}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class CreateBooleanExpressionAction extends Action {
		/**
		 * {@link BooleanExpressionType}.
		 */
		private final BooleanExpressionType type;

		/**
		 * Constructor.
		 *
		 * @param type
		 *            {@link BooleanExpressionType} describing which type of boolean expression to
		 *            create.
		 */
		CreateBooleanExpressionAction(BooleanExpressionType type) {
			this.type = type;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD);
		}

		@Override
		public String getText() {
			return type.toString();
		}

		@Override
		public void run() {
			AbstractExpression expression = MatchingRulesEditingElementFactory.createExpression(type);
			addExpression(expression);
		}
	}

	/**
	 * This action creates a matching rule expression depending on the type of
	 * {@link MatchingRuleType}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class CreateMatchingRuleAction extends Action {
		/**
		 * {@link MatchingRuleType}.
		 */
		MatchingRuleType type;

		/**
		 * Constructor.
		 *
		 * @param type
		 *            {@link MatchingRuleType} describing the type of the matching rule to create.
		 */
		CreateMatchingRuleAction(MatchingRuleType type) {
			this.type = type;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD);
		}

		@Override
		public String getText() {
			return type.toString();
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
			ISelection selection = treeViewer.getSelection();
			if (selection instanceof StructuredSelection && !selection.isEmpty()) {
				AbstractExpression expression = (AbstractExpression) ((StructuredSelection) selection).getFirstElement();
				AbstractExpression parentExpression = searchParent(rootExpression, expression);
				if (null == parentExpression) {
					rootExpression = null; // NOPMD
					treeViewer.setInput(new TreeInput(rootExpression));
				}
				if (parentExpression instanceof AndExpression) {
					((AndExpression) parentExpression).getOperands().remove(expression);
				} else if (parentExpression instanceof OrExpression) {
					((OrExpression) parentExpression).getOperands().remove(expression);
				} else if (parentExpression instanceof NotExpression) {
					((NotExpression) parentExpression).setOperand(null);
				}
				if (null != parentExpression) {
					treeViewer.setSelection(new StructuredSelection(parentExpression), true);
				}
				updateTree();
			}
		}
	}

	/**
	 * Details part for individual matching rules.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class RuleDetails implements IDetailsPage {

		/**
		 * The {@link AbstractRuleEditingElement} to display and edit rule contents.
		 */
		private AbstractRuleEditingElement ruleEditingElement;

		/**
		 * The {@link IManagedForm} used for this {@link IDetailsPage}.
		 */
		private IManagedForm managedForm;

		/**
		 * Dirty state.
		 */
		private boolean dirty = false;

		/**
		 * Main composite.
		 */
		private Composite main;

		/**
		 * Current {@link AbstractExpression} instance under modification.
		 */
		private AbstractExpression currentExpression;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void initialize(IManagedForm form) {
			managedForm = form;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {

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
			if (null != currentExpression) {
				ruleEditingElement.fillRuleExpression(currentExpression);
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
		public void selectionChanged(IFormPart part, ISelection selection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			if (!selection.isEmpty()) {
				if (null != ruleEditingElement) {
					ruleEditingElement.dispose();
				}
				final AbstractExpression expression = (AbstractExpression) structuredSelection.getFirstElement();
				currentExpression = expression;
				ruleEditingElement = MatchingRulesEditingElementFactory.createRuleComposite(expression, editable);
				ruleEditingElement.addModifyListener(new RuleEditingElementModifiedListener() {

					@Override
					public void elementDisposed(AbstractRuleEditingElement ruleComposite) {
						// TODO Auto-generated method stub

					}

					@Override
					public void contentModified() {
						dirty = true;
						managedForm.dirtyStateChanged();
					}
				});
				ruleEditingElement.createControls(main, managedForm.getToolkit(), false);
				ruleEditingElement.initialize(expression);
				main.layout(true, true);

			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void createContents(Composite parent) {
			parent.setLayout(new GridLayout(1, false));
			main = managedForm.getToolkit().createComposite(parent, SWT.BORDER);
			main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			main.setLayout(new GridLayout(AbstractRuleEditingElement.NUM_GRID_COLUMNS, false));
		}

	}

}
