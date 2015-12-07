package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.PatternMatchingType;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.impl.StringValueSource;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Abstract class for all {@link AbstractRuleEditingElement} sub-classes based on string matching.
 *
 * @author Alexander Wert
 *
 */
public abstract class AbstractStringMatchingRuleEditingElement extends AbstractRuleEditingElement {

	/**
	 * Description text for the string matching row.
	 */
	private static final String DESCRIPTION = "Specify string matching strategy by selecting string matching type and target snippet.\n" + "For the matching type \""
			+ PatternMatchingType.REGEX.toString() + "\", the target snippet must be a valid regular expression.\n" + "For all other matching types no wildcards are allowed!";

	/**
	 * The default pattern matching type.
	 */
	protected static final PatternMatchingType DEFAULT_MATCHING_TYPE = PatternMatchingType.EQUALS;

	/**
	 * Matching type property.
	 */
	protected PatternMatchingType matchingType = DEFAULT_MATCHING_TYPE;

	/**
	 * Snippet text property.
	 */
	protected String snippetText = "";

	/**
	 * Combo box for selecting the {@link #matchingType} property.
	 */
	protected Combo patternMatchingTypeComboBox;

	/**
	 * Text editing field for specifying the {@link #snippetText} property.
	 */
	protected Text stringSnippetText;

	/**
	 * The label for the string source.
	 */
	private final String sourceName;

	/**
	 * Label for the pattern matching type.
	 */
	private Label patternMatchingTypeLabel;

	/**
	 * Dummy label to fill the grid layout.
	 */
	private Label fillLabel;

	/**
	 * Label holding the info text.
	 */
	private Label infoLabel;

	/**
	 * Constructor.
	 *
	 * @param name
	 *            Name of the editing element.
	 * @param description
	 *            Description text for the specific {@link AbstractRuleEditingElement} instance.
	 * @param sourceLabel
	 *            The label string for the string source
	 * @param useSearchInTrace
	 *            Indicates whether the searchInDepth sub-element shell be used in this editing
	 *            element.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 */
	public AbstractStringMatchingRuleEditingElement(String name, String description, String sourceLabel, boolean useSearchInTrace, boolean editable) {
		super(name, description, useSearchInTrace, editable);
		this.sourceName = sourceLabel;
	}

	@Override
	protected void createSpecificElements(final Composite parent, FormToolkit toolkit) {
		fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		patternMatchingTypeLabel = toolkit.createLabel(parent, sourceName);
		patternMatchingTypeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		patternMatchingTypeComboBox = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		patternMatchingTypeComboBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		String[] items = new String[PatternMatchingType.values().length];
		for (int i = 0; i < PatternMatchingType.values().length; i++) {
			items[i] = PatternMatchingType.values()[i].toString();
		}
		patternMatchingTypeComboBox.setItems(items);

		for (int i = 0; i < PatternMatchingType.values().length; i++) {
			if (PatternMatchingType.values()[i].equals(DEFAULT_MATCHING_TYPE)) {
				patternMatchingTypeComboBox.select(i);
			}
		}

		patternMatchingTypeComboBox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = patternMatchingTypeComboBox.getSelectionIndex();
				matchingType = PatternMatchingType.values()[idx];
				notifyModifyListeners();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		stringSnippetText = new Text(parent, SWT.BORDER);
		stringSnippetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		stringSnippetText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				snippetText = ((Text) e.getSource()).getText();
				notifyModifyListeners();
			}
		});

		infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(DESCRIPTION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void disposeSpecificElements() {
		patternMatchingTypeLabel.dispose();
		patternMatchingTypeComboBox.dispose();
		stringSnippetText.dispose();
		fillLabel.dispose();
		infoLabel.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setEnabledStateForSpecificElements() {
		patternMatchingTypeLabel.setEnabled(isEditable());
		patternMatchingTypeComboBox.setEnabled(isEditable());
		stringSnippetText.setEnabled(isEditable());
		fillLabel.setEnabled(isEditable());
		infoLabel.setEnabled(isEditable());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractExpression constructRuleExpression() {
		StringMatchingExpression expression = new StringMatchingExpression(matchingType, snippetText);
		fillRuleExpression(expression);
		return expression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fillRuleExpression(AbstractExpression expression) {
		StringMatchingExpression strMatchingExpression = (StringMatchingExpression) expression;
		strMatchingExpression.setMatchingType(matchingType);
		strMatchingExpression.setSnippet(snippetText);
		strMatchingExpression.setStringValueSource(getStringValueSource());
		if (isSearchInDepth()) {
			strMatchingExpression.setSearchNodeInTrace(true);
			strMatchingExpression.setMaxSearchDepth(getSearchDepth());
		}
	}

	@Override
	protected void executeSpecificInitialization(AbstractExpression expression) {
		if (isValidExpression(expression)) {
			super.executeSpecificInitialization(expression);
			StringMatchingExpression strMatchingExpression = ((StringMatchingExpression) expression);
			matchingType = strMatchingExpression.getMatchingType();
			for (int i = 0; i < PatternMatchingType.values().length; i++) {
				if (PatternMatchingType.values()[i].equals(matchingType)) {
					patternMatchingTypeComboBox.select(i);
				}
			}
			snippetText = strMatchingExpression.getSnippet();
			stringSnippetText.setText(snippetText);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getNumRows() {
		return 1;
	}

	/**
	 * Gets {@link #matchingType}.
	 *
	 * @return {@link #matchingType}
	 */
	public PatternMatchingType getMatchingType() {
		return matchingType;
	}

	/**
	 * Gets {@link #snippetText}.
	 *
	 * @return {@link #snippetText}
	 */
	public String getSnippetText() {
		return snippetText;
	}

	/**
	 *
	 * @return An instance of the {@link StringValueSource} depending on the sub-class of this
	 *         class.
	 */
	protected abstract StringValueSource getStringValueSource();

	/**
	 * Returns true, if the given expression is a valid expression for this editing element.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to check.
	 * @return true, if the given expression is a valid expression for this editing element.
	 *         Otherwise, false.
	 */
	protected abstract boolean isValidExpression(AbstractExpression expression);

}
