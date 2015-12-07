package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This abstract class forms the basis for all editing elements allowing to view and modify specific
 * instances of {@link AbstractExpression} for an {@link ApplicationDefinition} or a
 * {@link BusinessTransactionDefinition}.
 *
 * @author Alexander Wert
 *
 */
public abstract class AbstractRuleEditingElement {
	/**
	 * Description text for the search in trace row.
	 */
	private static final String SEARCH_IN_TRACE_INFO_TEXT = "If disabled, only the root node of the call tree is evaluated against the specified condition.\n"
			+ "If enabled, the call tree (trace) is searched up to the specified maximum depth for a tree node that matches the specified condition.";

	/**
	 * Number of columns in the grid layout of the main composite.
	 */
	public static final int NUM_GRID_COLUMNS = 7;

	/**
	 * The resource manager is used for the images etc.
	 */
	private final LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * Listeners that are notified when this element is modified or disposed.
	 */
	protected List<RuleEditingElementModifiedListener> modifyListeners = new ArrayList<RuleEditingElementModifiedListener>();

	/**
	 * Switch for the search in depth property of the {@link AbstractExpression}.
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
	 * Label for the {@link #searchInDepth} property.
	 */
	private Label depthLabel;

	/**
	 * Name of the editing element.
	 */
	private final String name;

	/**
	 * Description text for the specific {@link AbstractRuleEditingElement} instance.
	 */
	private final String description;

	/**
	 * Indicates whether the searchInDepth sub-element shell be used in this editing element.
	 */
	private final boolean useSearchInDepthComposite;

	/**
	 * Label containing an Icon for heading.
	 */
	private Label icon;

	/**
	 * Heading text.
	 */
	private FormText headingText;

	/**
	 * FormText containing delete button.
	 */
	private FormText deleteText;

	/**
	 * Dummy label to fill the grid layout.
	 */
	private Label searchInTraceFillLabel;

	/**
	 * Info Label for the search in trace row.
	 */
	private Label searchInTraceInfoLabel;

	/**
	 * Info Label for the specific {@link AbstractRuleEditingElement} instance.
	 */
	private Label infoLabel;

	/**
	 * Indicates whether listeners shell be notified in the current state.
	 */
	private boolean notificationActive = true;

	/**
	 * Indicates whether this form part is editable or not.
	 */
	private final boolean editable;

	/**
	 * Constructor.
	 *
	 * @param name
	 *            Name of the editing element.
	 * @param description
	 *            Description text for the specific {@link AbstractRuleEditingElement} instance.
	 * @param useSearchInDepthComposite
	 *            Indicates whether the searchInDepth sub-element shell be used in this editing
	 *            element.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 */
	public AbstractRuleEditingElement(String name, String description, boolean useSearchInDepthComposite, boolean editable) {
		this.editable = editable;
		this.name = name;
		this.description = description;
		this.useSearchInDepthComposite = useSearchInDepthComposite;
	}

	/**
	 * Creates controls for this editing element.
	 *
	 * @param parent
	 *            parent {@link Composite}.
	 * @param toolkit
	 *            {@link FormToolkit} to use for the creation of controls.
	 * @param disposable
	 *            specifies whether the self-delete button shell be created for this editing
	 *            element.
	 */
	public void createControls(Composite parent, FormToolkit toolkit, boolean disposable) {
		icon = toolkit.createLabel(parent, "");
		icon.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		icon.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_PROPERTIES));
		icon.setToolTipText(description);

		headingText = toolkit.createFormText(parent, false);
		headingText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_GRID_COLUMNS - 3, 1));
		headingText.setText("<form><p><b>" + name + "</b></p></form>", true, false);

		infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(description);

		if (disposable) {
			deleteText = toolkit.createFormText(parent, false);
			deleteText.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			deleteText.setText("<form><p><a href=\"delete\"><img href=\"deleteImg\" /></a></p></form>", true, false);
			deleteText.setImage("deleteImg", InspectIT.getDefault().getImage(InspectITImages.IMG_DELETE));
			deleteText.addHyperlinkListener(new IHyperlinkListener() {
				@Override
				public void linkExited(HyperlinkEvent e) {
				}

				@Override
				public void linkEntered(HyperlinkEvent e) {
				}

				@Override
				public void linkActivated(HyperlinkEvent e) {
					dispose();
				}
			});
		} else {
			deleteText = toolkit.createFormText(parent, false);
			deleteText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		}
		createSpecificElements(parent, toolkit);

		if (useSearchInDepthComposite) {
			searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));

			searchInTraceCheckBox = toolkit.createButton(parent, "search in trace", SWT.CHECK);
			searchInTraceCheckBox.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

			depthLabel = toolkit.createLabel(parent, "maximum search depth: ");
			depthLabel.setEnabled(false);
			depthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

			depthSpinner = new Spinner(parent, SWT.BORDER);
			depthSpinner.setMinimum(-1);
			depthSpinner.setMaximum(Integer.MAX_VALUE);
			depthSpinner.setSelection(-1);
			depthSpinner.setIncrement(1);
			depthSpinner.setPageIncrement(100);
			depthSpinner.setEnabled(false);
			depthSpinner.setToolTipText("A value of -1 means that no limit for the search depth is used!");
			depthSpinner.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			depthSpinner.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchDepth = depthSpinner.getSelection();
					notifyModifyListeners();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
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
				}
			});
			searchInTraceInfoLabel = toolkit.createLabel(parent, "");
			searchInTraceInfoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			searchInTraceInfoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			searchInTraceInfoLabel.setToolTipText(SEARCH_IN_TRACE_INFO_TEXT);
		} else {
			searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, NUM_GRID_COLUMNS, 1));
		}
		setEnabledState();
	}

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
	 * Initializes the content of the controls comprised in this editing element.
	 *
	 * @param expression
	 *            An {@link AbstractExpression} determining the content.
	 */
	public void initialize(AbstractExpression expression) {
		notificationActive = false;
		executeSpecificInitialization(expression);
		notificationActive = true;
	}

	/**
	 *
	 */
	public void dispose() {
		disposeSpecificElements();
		deleteText.dispose();
		headingText.dispose();
		icon.dispose();
		if (useSearchInDepthComposite) {
			searchInTraceCheckBox.dispose();
			depthLabel.dispose();
			depthSpinner.dispose();
			searchInTraceInfoLabel.dispose();
		}
		searchInTraceFillLabel.dispose();
		notifyDisposed();
		infoLabel.dispose();
		resourceManager.dispose();
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
	 * Executes sub-class specific initialization of the controls.
	 *
	 * @param expression
	 *            An {@link AbstractExpression} determining the content.
	 */
	protected void executeSpecificInitialization(AbstractExpression expression) {
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
	 * Sets enabled state for common controls of the {@link AbstractRuleEditingElement} class.
	 */
	private void setEnabledState() {
		deleteText.setEnabled(isEditable());
		headingText.setEnabled(isEditable());
		icon.setEnabled(isEditable());
		if (useSearchInDepthComposite) {
			searchInTraceCheckBox.setEnabled(isEditable());
			depthLabel.setEnabled(isEditable() && searchInTraceCheckBox.getSelection());
			depthSpinner.setEnabled(isEditable() && searchInTraceCheckBox.getSelection());
			searchInTraceInfoLabel.setEnabled(isEditable());
		}
		searchInTraceFillLabel.setEnabled(isEditable());
		infoLabel.setEnabled(isEditable());
		setEnabledStateForSpecificElements();
	}

	/**
	 * Constructs an {@link AbstractExpression} from the contents of the editing element controls.
	 *
	 * @return an {@link AbstractExpression}.
	 */
	public abstract AbstractExpression constructRuleExpression();

	/**
	 * Fills the given {@link AbstractExpression} from the contents of the editing element controls.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to fill.
	 */
	public abstract void fillRuleExpression(AbstractExpression expression);

	/**
	 * Sets the enabled states for the elements of the specific implementation of this class.
	 */
	protected abstract void setEnabledStateForSpecificElements();

	/**
	 * Creates specific controls of this editing element.
	 *
	 * @param parent
	 *            parent {@link Composite}.
	 * @param toolkit
	 *            {@link FormToolkit} to use for the creation of controls.
	 */
	protected abstract void createSpecificElements(Composite parent, FormToolkit toolkit);

	/**
	 * Disposes specific controls of this editing element.
	 */
	protected abstract void disposeSpecificElements();

	/**
	 * Returns the number of rows the body of the corresponding editing element spans.
	 *
	 * @return Returns the number of rows the body of the corresponding editing element spans.
	 */
	protected abstract int getNumRows();

	/**
	 * Interface for listeners that are modified when an instance of a
	 * {@link AbstractRuleEditingElement} is modified or disposed.
	 *
	 * @author Alexander Wert
	 *
	 */
	public interface RuleEditingElementModifiedListener {
		/**
		 * Contents have been modified.
		 */
		void contentModified();

		/**
		 * Element has been disposed.
		 *
		 * @param ruleComposite
		 *            {@link AbstractRuleEditingElement} to be disposed.
		 */
		void elementDisposed(AbstractRuleEditingElement ruleComposite);
	}
}
