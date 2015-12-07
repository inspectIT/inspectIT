package rocks.inspectit.ui.rcp.ci.form.part.business.rules;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.IRulesExpressionType;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;

/**
 * This class encapsulates Search in trace UI elements for rule definitions.
 * 
 * @author Alexander Wert
 *
 */
public abstract class AbstractSearchInDepthEditingElement extends AbstractRuleEditingElement<StringMatchingExpression> {

	/**
	 * Description text for the search in trace row.
	 */
	private static final String SEARCH_IN_TRACE_INFO_TEXT = "If disabled, only the root node of the call tree is evaluated against the specified condition.\n"
			+ "If enabled, the call tree (trace) is searched up to the specified maximum depth for a tree node that matches the specified condition.";

	/**
	 * Indicates whether the searchInDepth sub-element shell be used in this editing element.
	 */
	private final boolean useSearchInDepthComposite;

	/**
	 * Check box to select {@link #searchInDepth} property.
	 */
	private Button searchInTraceCheckBox;

	/**
	 * Spinner for configuring the {@link #searchDepth} property.
	 */
	private Spinner depthSpinner;

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param ruleType
	 *            the type of the matching rule
	 * @param description
	 *            Description text for the specific {@link AbstractRuleEditingElement} instance.
	 * @param useSearchInTrace
	 *            Indicates whether the searchInDepth sub-element shell be used in this editing
	 *            element.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	public AbstractSearchInDepthEditingElement(StringMatchingExpression expression, IRulesExpressionType ruleType, String description, boolean useSearchInTrace, boolean editable,
			AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		super(expression, ruleType, description, editable, upstreamValidationManager);

		this.useSearchInDepthComposite = useSearchInTrace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(Composite parent, FormToolkit toolkit) {
		if (useSearchInDepthComposite) {
			Label searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceFillLabel);

			Label searchInTraceLabel = toolkit.createLabel(parent, "Search in trace:");
			searchInTraceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceLabel);

			searchInTraceCheckBox = toolkit.createButton(parent, "Yes", SWT.CHECK);
			searchInTraceCheckBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			addControl(searchInTraceCheckBox);

			Label depthLabel = toolkit.createLabel(parent, "Maximum search depth: ");
			depthLabel.setEnabled(false);
			depthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
			addControl(depthLabel);

			depthSpinner = new Spinner(parent, SWT.BORDER);
			depthSpinner.setMinimum(-1);
			depthSpinner.setMaximum(Integer.MAX_VALUE);
			depthSpinner.setSelection(-1);
			depthSpinner.setIncrement(1);
			depthSpinner.setPageIncrement(100);
			depthSpinner.setEnabled(false);
			depthSpinner.setToolTipText("A value of -1 means that no limit for the search depth is used!");
			depthSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			depthSpinner.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					getExpression().setMaxSearchDepth(depthSpinner.getSelection());
					notifyModifyListeners();
				}
			});
			addControl(depthSpinner);

			searchInTraceCheckBox.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (searchInTraceCheckBox.getSelection()) {
						depthSpinner.setEnabled(true);
						getExpression().setSearchNodeInTrace(true);
					} else {
						depthSpinner.setEnabled(false);
						getExpression().setSearchNodeInTrace(false);
					}
					notifyModifyListeners();
				}
			});
			Label searchInTraceInfoLabel = toolkit.createLabel(parent, "");
			searchInTraceInfoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			searchInTraceInfoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			searchInTraceInfoLabel.setToolTipText(SEARCH_IN_TRACE_INFO_TEXT);
			addControl(searchInTraceInfoLabel);
		} else {
			Label searchInTraceFillLabel = toolkit.createLabel(parent, "");
			searchInTraceFillLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, NUM_GRID_COLUMNS, 1));
			addControl(searchInTraceFillLabel);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void executeSpecificInitialization(StringMatchingExpression expression) {
		if (expression.isSearchNodeInTrace() && useSearchInDepthComposite) {
			searchInTraceCheckBox.setSelection(true);
			depthSpinner.setSelection(expression.getMaxSearchDepth());
			depthSpinner.setEnabled(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateEnabledState() {
		super.updateEnabledState();
		if (useSearchInDepthComposite) {
			depthSpinner.setEnabled(isEditable() && searchInTraceCheckBox.getSelection());
		}
	}

}
