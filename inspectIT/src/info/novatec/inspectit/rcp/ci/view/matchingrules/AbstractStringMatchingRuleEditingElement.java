package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.ci.business.Expression;
import info.novatec.inspectit.ci.business.PatternMatchingType;
import info.novatec.inspectit.ci.business.StringMatchingExpression;
import info.novatec.inspectit.ci.business.StringValueSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract class for all {@link AbstractRuleEditingElement} sub-classes based on string matching.
 * 
 * @author Alexander Wert
 *
 */
public abstract class AbstractStringMatchingRuleEditingElement extends AbstractRuleEditingElement {
	/**
	 * The default pattern matching type.
	 */
	protected static final PatternMatchingType DEFAULT_MATCHING_TYPE = PatternMatchingType.values()[PatternMatchingType.values().length - 1];

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
	 * The name of the string source.
	 */
	private String sourceName;

	/**
	 * Default Constructor.
	 * 
	 * @param name
	 *            Name of this editing element.
	 * @param sourceName
	 *            The name of the string source.
	 * @param useSearchInTrace
	 *            Indicates whether the searchInDepth sub-element shell be used in this editing
	 *            element.
	 */
	public AbstractStringMatchingRuleEditingElement(String name, String sourceName, boolean useSearchInTrace) {
		super(name, useSearchInTrace);
		this.sourceName = sourceName;
	}

	@Override
	protected void createSpecificElements(final Composite parent) {
		Composite elementsContainer = new Composite(parent, SWT.NONE);
		elementsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		elementsContainer.setLayout(new GridLayout(3, false));
		Label label = new Label(elementsContainer, SWT.NONE);
		label.setText(sourceName);
		patternMatchingTypeComboBox = new Combo(elementsContainer, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
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

		patternMatchingTypeComboBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

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

		stringSnippetText = new Text(elementsContainer, SWT.BORDER);
		stringSnippetText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		stringSnippetText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				snippetText = ((Text) e.getSource()).getText();
				notifyModifyListeners();
			}
		});
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

	@Override
	public Expression constructRuleExpression() {
		StringMatchingExpression expression = new StringMatchingExpression(matchingType, snippetText);
		expression.setStringValueSource(getStringValueSource());
		if (isSearchInDepth()) {
			expression.setSearchNodeInTrace(true);
			expression.setMaxSearchDepth(getSearchDepth());
		}

		return expression;
	}

	@Override
	protected void executeSpecificInitialization(Expression expression) {
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
	 * 
	 * @return An instance of the {@link StringValueSource} depending on the sub-class of
	 *         this class.
	 */
	protected abstract StringValueSource getStringValueSource();

	/**
	 * Returns true, if the given expression is a valid expression for this editing element.
	 * 
	 * @param expression
	 *            {@link Expression} to check.
	 * @return true, if the given expression is a valid expression for this editing element.
	 *         Otherwise, false.
	 */
	protected abstract boolean isValidExpression(Expression expression);

}
