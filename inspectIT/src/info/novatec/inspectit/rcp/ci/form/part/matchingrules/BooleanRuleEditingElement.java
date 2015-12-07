/**
 *
 */
package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.BooleanExpression;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Alexander Wert
 *
 */
public class BooleanRuleEditingElement extends AbstractRuleEditingElement {

	/**
	 * Description text for the Boolean rule.
	 */
	private static final String DESCRIPTION = "The evaluation of this rule is independent of any measurement data.\n"
			+ "Depending on the specified value, this expression is either always evaluated true or always to false.";

	/**
	 * Dummy label to fill the first column.
	 */
	private Label fillLabel;

	/**
	 * Label holding the text for the string source.
	 */
	private Label valueLabel;

	/**
	 * Combo box for the selection of the boolean value.
	 */
	private Combo valueComboBox;

	/**
	 * The boolean value for the rule.
	 */
	private boolean value;

	/**
	 * Constructor.
	 *
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 */
	public BooleanRuleEditingElement(boolean editable) {
		super("Boolean Expression", DESCRIPTION, false, editable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(Composite parent, FormToolkit toolkit) {
		fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		valueLabel = toolkit.createLabel(parent, "Value:");
		valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		valueComboBox = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		valueComboBox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		valueComboBox.setItems(new String[] { "true", "false" });

		valueComboBox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = valueComboBox.getSelectionIndex();

				if (idx == 0) {
					value = true;
				} else {
					value = false;
				}
				notifyModifyListeners();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void disposeSpecificElements() {
		fillLabel.dispose();
		valueLabel.dispose();
		valueComboBox.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getNumRows() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractExpression constructRuleExpression() {
		return new BooleanExpression(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fillRuleExpression(AbstractExpression expression) {
		BooleanExpression bExpression = (BooleanExpression) expression;
		bExpression.setValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void executeSpecificInitialization(AbstractExpression expression) {
		if (expression instanceof BooleanExpression) {
			super.executeSpecificInitialization(expression);
			value = ((BooleanExpression) expression).isValue();
			if (value) {
				valueComboBox.select(0);
			} else {
				valueComboBox.select(1);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setEnabledStateForSpecificElements() {
		fillLabel.setEnabled(isEditable());
		valueLabel.setEnabled(isEditable());
		valueComboBox.setEnabled(isEditable());
	}

}
