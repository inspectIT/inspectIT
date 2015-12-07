package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.cmr.configuration.business.BusinessTransactionDefinition;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IMatchingRule;
import info.novatec.inspectit.cmr.configuration.business.expression.Expression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * THis abstract class forms the basis for all editing elements allowing to view and modify specific
 * instances of {@link Expression} as part of a {@link IMatchingRule} for an
 * {@link IApplicationDefinition} or a {@link BusinessTransactionDefinition}.
 * 
 * @author Alexander Wert
 *
 */
public abstract class AbstractRuleEditingElement {

	/**
	 * Listeners that are notified when this element is modified or disposed.
	 */
	protected List<RuleEditingElementModifiedListener> modifyListeners = new ArrayList<RuleEditingElementModifiedListener>();

	/**
	 * Switch for the search in depth property of the {@link Expression}.
	 */
	private boolean searchInDepth = false;

	/**
	 * Search depth property when {@link #searchInDepth} is enabled.
	 */
	private int searchDepth = -1;

	/**
	 * Check box to select {@link #searchInDepth} property.
	 */
	private Button searchInTraceCheckBox;

	/**
	 * Spinner for configuring the {@link #searchDepth} property.
	 */
	private Spinner depthSpinner;

	/**
	 * Main section.
	 */
	private Section section;

	/**
	 * Label for the {@link #searchInDepth} property.
	 */
	private Label depthLabel;

	/**
	 * Name of the editing element.
	 */
	private String name;

	/**
	 * Indicates whether the searchInDepth sub-element shell be used in this editing element.
	 */
	private boolean useSearchInDepthComposite;

	/**
	 * Background color of the title.
	 */
	private Color titleBackgroundColor;

	/**
	 * Indicates whether listeners shell be notified in the current state.
	 */
	private boolean notificationActive = true;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            Name of the editing element.
	 * @param useSearchInDepthComposite
	 *            Indicates whether the searchInDepth sub-element shell be used in this editing
	 *            element.
	 */
	public AbstractRuleEditingElement(String name, boolean useSearchInDepthComposite) {
		this.name = name;
		this.useSearchInDepthComposite = useSearchInDepthComposite;
	}

	/**
	 * Creates controls for this editing element.
	 * 
	 * @param parent
	 *            parent {@link Composite}.
	 * @param toolkit
	 *            {@link FormToolkit} to use for the creation of controls.
	 */
	public void createControls(Composite parent, FormToolkit toolkit) {
		section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		section.setText(name);

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		toolBarManager.add(new DeleteAction());
		toolBarManager.update(true);
		section.setTextClient(toolbar);

		if (null != titleBackgroundColor) {
			section.setTitleBarBackground(titleBackgroundColor);
		}

		Composite ruleComposite = toolkit.createComposite(section);

		ruleComposite.setLayout(new GridLayout(1, false));
		ruleComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		section.setClient(ruleComposite);

		createSpecificElements(ruleComposite);
		if (useSearchInDepthComposite) {
			Composite searchDepthContainer = new Composite(ruleComposite, SWT.NONE);
			searchDepthContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			searchDepthContainer.setLayout(new GridLayout(3, false));

			searchInTraceCheckBox = new Button(searchDepthContainer, SWT.CHECK);
			searchInTraceCheckBox.setText("search in trace");
			searchInTraceCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			depthLabel = new Label(searchDepthContainer, SWT.NONE);
			depthLabel.setText("maximum search depth: ");
			depthLabel.setEnabled(false);

			depthSpinner = new Spinner(searchDepthContainer, SWT.BORDER);
			depthSpinner.setMinimum(-1);
			depthSpinner.setMaximum(Integer.MAX_VALUE);
			depthSpinner.setSelection(-1);
			depthSpinner.setIncrement(1);
			depthSpinner.setPageIncrement(100);
			depthSpinner.setEnabled(false);
			depthSpinner.setToolTipText("A value of -1 means that no limit for the search depth is used!");
			depthSpinner.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					searchDepth = depthSpinner.getSelection();
					notifyModifyListeners();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}

			});
			searchInTraceCheckBox.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (searchInTraceCheckBox.getSelection()) {
						depthLabel.setEnabled(true);
						depthSpinner.setEnabled(true);
						searchInDepth = true;
						searchDepth = depthSpinner.getSelection();
					} else {
						depthLabel.setEnabled(false);
						depthSpinner.setEnabled(false);
						searchInDepth = false;
					}
					notifyModifyListeners();

				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	/**
	 * Creates specific controls of this editing element.
	 * 
	 * @param parent
	 *            parent {@link Composite}.
	 */
	protected abstract void createSpecificElements(Composite parent);

	/**
	 * Constructs an {@link Expression} from the contents of the editing element controls.
	 * 
	 * @return an {@link Expression}.
	 */
	public abstract Expression constructRuleExpression();

	/**
	 * Gets {@link #searchInDepth}.
	 * 
	 * @return {@link #searchInDepth}
	 */
	public boolean isSearchInDepth() {
		return searchInDepth;
	}

	/**
	 * Gets {@link #searchDepth}.
	 * 
	 * @return {@link #searchDepth}
	 */
	public int getSearchDepth() {
		return searchDepth;
	}

	/**
	 * Adds a {@link RuleEditingElementModifiedListener} to this editing element.
	 * 
	 * @param listener
	 *            {@link RuleEditingElementModifiedListener} to add.
	 */
	public void addModifyListener(RuleEditingElementModifiedListener listener) {
		modifyListeners.add(listener);
	}

	/**
	 * Notifies all modify listeners about a modification of the controls content.
	 */
	protected void notifyModifyListeners() {
		if (notificationActive) {
			for (RuleEditingElementModifiedListener listener : modifyListeners) {
				listener.contentModified();
			}
		}
	}

	/**
	 * Notifies all modify listeners about the disposition of this editing element.
	 */
	protected void notifyDisposed() {
		if (notificationActive) {
			for (RuleEditingElementModifiedListener listener : modifyListeners) {
				listener.elementDisposed(this);
			}
		}
	}

	/**
	 * Initializes the content of the controls comprised in this editing element.
	 * 
	 * @param expression
	 *            An {@link Expression} determining the content.
	 */
	public void initialize(Expression expression) {
		notificationActive = false;
		executeSpecificInitialization(expression);
		notificationActive = true;
	}

	/**
	 * Executes sub-class specific initialization of the controls.
	 * 
	 * @param expression
	 *            An {@link Expression} determining the content.
	 */
	protected void executeSpecificInitialization(Expression expression) {
		if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).isSearchNodeInTrace() && useSearchInDepthComposite) {
			searchDepth = ((StringMatchingExpression) expression).getMaxSearchDepth();
			searchInDepth = true;
			searchInTraceCheckBox.setSelection(true);

			depthSpinner.setSelection(searchDepth);
			depthSpinner.setEnabled(true);
			depthLabel.setEnabled(true);
		}
	}

	/**
	 * Gets {@link #titleBackgroundColor}.
	 * 
	 * @return {@link #titleBackgroundColor}
	 */
	public Color getTitleBackgroundColor() {
		return titleBackgroundColor;
	}

	/**
	 * Sets {@link #titleBackgroundColor}.
	 * 
	 * @param titleBackgroundColor
	 *            New value for {@link #titleBackgroundColor}
	 */
	public void setTitleBackgroundColor(Color titleBackgroundColor) {
		this.titleBackgroundColor = titleBackgroundColor;
	}

	/**
	 * Marks the receiver as visible if the argument is true, and marks it invisible otherwise.
	 * 
	 * @param visible
	 *            the new visibility state
	 */
	public void setVisible(boolean visible) {
		section.setVisible(visible);
	}

	/**
	 * 
	 * @return Returns the main control of this editing element.
	 */
	public Control getControl() {
		return section;
	}

	/**
	 * This action disposes this editing element.
	 * 
	 * @author Alexander Wert
	 *
	 */
	private class DeleteAction extends Action {
		@Override
		public void run() {
			section.dispose();
			notifyDisposed();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DELETE);
		}
	}

}
