/**
 *
 */
package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.HttpParameterValueSource;
import info.novatec.inspectit.ci.business.impl.HttpUriValueSource;
import info.novatec.inspectit.ci.business.impl.MethodParameterValueSource;
import info.novatec.inspectit.ci.business.impl.MethodSignatureValueSource;
import info.novatec.inspectit.ci.business.impl.NameExtractionExpression;
import info.novatec.inspectit.ci.business.impl.StringValueSource;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A SectionPart for the purpose of editing the definition of dynamic extraction of business
 * transaction names.
 *
 * @author Alexander Wert
 *
 */
public class DynamicNameExtractionPart extends SectionPart {
	/**
	 * Description text.
	 */
	private static final String DESCRIPTION = "Use dynamic name extraction to determine the business transaction name dynamically from measurement data. \n\n"
			+ "Dynamic name extraction is applied on an invocation sequence only if the matching rule is evaluated to true for the corresponding invocation sequence.";

	/**
	 * Regex description text.
	 */
	private static final String REGEX_DESCRIPTION = "Specify a regular expression to extract one (or multiple) fragments from the string resulting from the above selection.\n"
			+ "Use brackets to extract string groups. Groups are numbered in the order of their occurence.\n\n"
			+ "Example:\nfor the string \"myPackage.Class.doSomething\" using regex \".*Class\\.(.*)Some(.*)\" yields the following string groups:\n" + "1: \"do\"\n" + "2: \"thing\"";

	/**
	 * Name pattern description text.
	 */
	private static final String NAME_PATTERN_DESCRIPTION = "Use the string groups extracted with the regular expression to specify a name pattern.\n\n"
			+ "Example:\nthe name pattern \"(1)-cool-(2)\" would result in the name \"do-cool-thing\"\nfor the string groups from the example above.";

	/**
	 * Description text for the search in trace row.
	 */
	private static final String SEARCH_IN_TRACE_DESCRIPTION = "If disabled, only the root node of the call tree is evaluated against the specified regular expression.\n"
			+ "If enabled, the call tree (trace) is searched up to the specified maximum depth for a tree node that matches the specified regular expression.";

	/**
	 * Number of columns in the main {@link GridLayout} of this section.
	 */
	private static final int NUM_COLUMNS = 6;

	/**
	 * Check box for enabling/disabling dynamic name extraction.
	 */
	private final Button extractNameCheckbox;

	/**
	 * List of {@link AbstractExtractNameRowComposite} instances used as children in this section.
	 */
	private final List<AbstractExtractNameRowComposite> rowComposites = new ArrayList<>(4);

	/**
	 * Label holding the info image.
	 */
	private final Label labelInfoImage;

	/**
	 * Label for the regular expression.
	 */
	private final Label regularExpressionLabel;

	/**
	 * Label holding the info image for the regular expression.
	 */
	private final Label regularExpressionLabelInfoImage;

	/**
	 * Text control for editing the regular expression.
	 */
	private final Text regularExpressionText;

	/**
	 * Label for the search depth Spinner control.
	 */
	private final Label depthLabel;

	/**
	 * Label for the name pattern.
	 */
	private final Label namePatternLabel;

	/**
	 * Label holding the info image for the name patter.
	 */
	private final Label namePatternLabelInfoImage;

	/**
	 * Text control for editing the name pattern.
	 */
	private final Text namePatternText;

	/**
	 * Check box for enabling/disabling search in trace.
	 */
	private final Button searchInTraceCheckBox;

	/**
	 * Spinner for modifying the search depth.
	 */
	private final Spinner searchDepthSpinner;

	/**
	 * Label holding the info image for the search in trace row.
	 */
	private final Label searchInTraceInfoLabelImage;

	/**
	 * Holds the state whether this section is in the initialization phase or not.
	 */
	private boolean initializationPhase = false;

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            parent {@link Composite}.
	 * @param managedForm
	 *            {@link IManagedForm} to add this part to.
	 */
	public DynamicNameExtractionPart(Composite parent, IManagedForm managedForm) {
		super(parent, managedForm.getToolkit(), Section.TITLE_BAR);
		initializationPhase = true;
		managedForm.addPart(this);
		getSection().setLayout(new GridLayout(1, false));
		getSection().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		getSection().setText("Dynamic Name Extraction");
		FormToolkit toolkit = managedForm.getToolkit();

		// section body
		Composite body = toolkit.createComposite(getSection());
		body.setLayout(new GridLayout(2, false));
		extractNameCheckbox = toolkit.createButton(body, "Extract name dynamically", SWT.CHECK);
		extractNameCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		extractNameCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setMainPartEnabled(extractNameCheckbox.getSelection());
				if (!initializationPhase) {
					markDirty();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		labelInfoImage = toolkit.createLabel(body, "");
		labelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		labelInfoImage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		labelInfoImage.setToolTipText(DESCRIPTION);

		// main content part
		Composite main = toolkit.createComposite(body);
		main.setLayout(new GridLayout(NUM_COLUMNS, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// Rows for specifying the string value source
		rowComposites.add(new HttpUriNameExtractionRowComposite(toolkit, main, true));
		rowComposites.add(new HttpParameterNameExtractionRowComposite(toolkit, main, false));
		rowComposites.add(new MethodSignatureNameExtractionRowComposite(toolkit, main, false));
		rowComposites.add(new MethodParameterNameExtractionRowComposite(toolkit, main, false));

		// one empty row for separation
		toolkit.createLabel(main, "").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS, 1));

		// regular expression, name pattern and search in trace rows
		regularExpressionLabel = toolkit.createLabel(main, "Regular Expression:");
		regularExpressionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		regularExpressionText = toolkit.createText(main, "");
		regularExpressionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS - 2, 1));
		regularExpressionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		regularExpressionLabelInfoImage = toolkit.createLabel(main, REGEX_DESCRIPTION);
		regularExpressionLabelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		regularExpressionLabelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		regularExpressionLabelInfoImage.setToolTipText(REGEX_DESCRIPTION);

		namePatternLabel = toolkit.createLabel(main, "Name Pattern:");
		namePatternLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		namePatternText = toolkit.createText(main, "");
		namePatternText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS - 2, 1));
		namePatternText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});
		namePatternLabelInfoImage = toolkit.createLabel(main, NAME_PATTERN_DESCRIPTION);
		namePatternLabelInfoImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		namePatternLabelInfoImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		namePatternLabelInfoImage.setToolTipText(NAME_PATTERN_DESCRIPTION);

		searchInTraceCheckBox = toolkit.createButton(main, "search in trace", SWT.CHECK);
		searchInTraceCheckBox.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, NUM_COLUMNS - 3, 1));
		searchInTraceCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				depthLabel.setEnabled(searchInTraceCheckBox.getSelection());
				searchDepthSpinner.setEnabled(searchInTraceCheckBox.getSelection());
				if (!initializationPhase) {
					markDirty();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		depthLabel = toolkit.createLabel(main, "maximum search depth: ");
		depthLabel.setEnabled(false);
		depthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		searchDepthSpinner = new Spinner(main, SWT.BORDER);
		searchDepthSpinner.setMinimum(-1);
		searchDepthSpinner.setMaximum(Integer.MAX_VALUE);
		searchDepthSpinner.setSelection(-1);
		searchDepthSpinner.setIncrement(1);
		searchDepthSpinner.setPageIncrement(100);
		searchDepthSpinner.setEnabled(false);
		searchDepthSpinner.setToolTipText("A value of -1 means that no limit for the search depth is used!");
		searchDepthSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		searchDepthSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!initializationPhase) {
					markDirty();
				}
			}
		});

		searchInTraceInfoLabelImage = toolkit.createLabel(main, "");
		searchInTraceInfoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		searchInTraceInfoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		searchInTraceInfoLabelImage.setToolTipText(SEARCH_IN_TRACE_DESCRIPTION);

		setMainPartEnabled(false);
		getSection().setClient(body);
		initializationPhase = false;
	}

	/**
	 * Initializes the contents of this part according to the passed
	 * {@link NameExtractionExpression}.
	 *
	 * @param nameExtractionExpression
	 *            {@link NameExtractionExpression} instance to retrieve the contents from.
	 */
	public synchronized void init(NameExtractionExpression nameExtractionExpression) {
		initializationPhase = true;
		resetContent();
		if (null != nameExtractionExpression) {
			extractNameCheckbox.setSelection(true);
			for (AbstractExtractNameRowComposite composite : rowComposites) {
				composite.initialize(nameExtractionExpression.getStringValueSource());
			}
			String regularExpression = (nameExtractionExpression.getRegularExpression() == null) ? "" : nameExtractionExpression.getRegularExpression();
			String namePattern = (nameExtractionExpression.getTargetNamePattern() == null) ? "" : nameExtractionExpression.getTargetNamePattern();
			boolean searchNodeInTrace = nameExtractionExpression.getSearchNodeInTrace();
			int maxSearchDepth = nameExtractionExpression.getMaxSearchDepth();
			regularExpressionText.setText(regularExpression);
			namePatternText.setText(namePattern);
			searchInTraceCheckBox.setSelection(searchNodeInTrace);
			if (searchNodeInTrace) {
				searchDepthSpinner.setSelection(maxSearchDepth);
				searchDepthSpinner.setEnabled(true);
			} else {
				depthLabel.setEnabled(false);
				searchDepthSpinner.setEnabled(false);
			}

			setMainPartEnabled(true);
		} else {
			resetContent();
		}
		initializationPhase = false;
	}

	/**
	 * Constructs a {@link NameExtractionExpression} instance from the contents of the controls of
	 * this part.
	 *
	 * @return a {@link NameExtractionExpression} instance
	 */
	public NameExtractionExpression constructNameExtractionExpression() {
		if (extractNameCheckbox.getSelection()) {
			NameExtractionExpression extractionExpression = new NameExtractionExpression();
			StringValueSource stringValueSource = null;
			for (AbstractExtractNameRowComposite composite : rowComposites) {
				if (composite.isActive()) {
					stringValueSource = composite.constructStringValueSource();
					break;
				}
			}
			extractionExpression.setStringValueSource(stringValueSource);
			extractionExpression.setRegularExpression(regularExpressionText.getText());
			extractionExpression.setTargetNamePattern(namePatternText.getText());
			extractionExpression.setSearchNodeInTrace(searchInTraceCheckBox.getSelection());
			extractionExpression.setMaxSearchDepth(searchDepthSpinner.getSelection());
			return extractionExpression;
		} else {
			return null;
		}
	}

	/**
	 * Sets {@link #editable}.
	 *
	 * @param editable
	 *            New value for {@link #editable}
	 */
	public void setEditable(boolean editable) {
		getSection().setEnabled(editable);
		extractNameCheckbox.setEnabled(editable);
		labelInfoImage.setEnabled(editable);
		setMainPartEnabled(editable);
	}

	/**
	 * Resets the contents of this part.
	 */
	private void resetContent() {
		for (AbstractExtractNameRowComposite composite : rowComposites) {
			composite.reset();
		}
		extractNameCheckbox.setSelection(false);
		regularExpressionText.setText("");
		namePatternText.setText("");
		searchInTraceCheckBox.setSelection(false);
		searchDepthSpinner.setSelection(-1);
		depthLabel.setEnabled(false);
		searchDepthSpinner.setEnabled(false);
		setMainPartEnabled(false);
	}

	/**
	 * Sets the enabled state of the main content part.
	 *
	 * @param enabled
	 *            enabled state
	 */
	private void setMainPartEnabled(boolean enabled) {
		boolean anyActive = false;
		for (AbstractExtractNameRowComposite composite : rowComposites) {
			if (enabled && composite.isActive()) {
				composite.setEnabled(true);
				anyActive = true;
			} else if (!enabled) {
				composite.setEnabled(false);
			}
			composite.setRadioEnabled(enabled);
		}

		if (!anyActive) {
			for (AbstractExtractNameRowComposite composite : rowComposites) {
				composite.initialize(new HttpUriValueSource());
			}
		}

		regularExpressionLabel.setEnabled(enabled);
		regularExpressionLabelInfoImage.setEnabled(enabled);
		regularExpressionText.setEnabled(enabled);

		depthLabel.setEnabled(enabled);
		namePatternLabel.setEnabled(enabled);
		namePatternLabelInfoImage.setEnabled(enabled);
		namePatternText.setEnabled(enabled);
		searchInTraceCheckBox.setEnabled(enabled);
		if (searchInTraceCheckBox.getSelection()) {
			depthLabel.setEnabled(true);
			searchDepthSpinner.setEnabled(true);
		} else {
			depthLabel.setEnabled(false);
			searchDepthSpinner.setEnabled(false);
		}
		searchInTraceInfoLabelImage.setEnabled(enabled);

		getManagedForm().getForm().layout(true, true);
	}

	/**
	 * Is notified when the selection of a row (represented by an
	 * {@link AbstractExtractNameRowComposite} instance) changes.
	 *
	 * @param selectedRow
	 *            selected {@link AbstractExtractNameRowComposite} instance.
	 */
	private void rowSelectionChanged(AbstractExtractNameRowComposite selectedRow) {
		selectedRow.setEnabled(true);
		for (AbstractExtractNameRowComposite row : rowComposites) {
			if (!row.equals(selectedRow)) {
				row.setEnabled(false);
			}
		}
		if (!initializationPhase) {
			markDirty();
		}
	}

	/**
	 * Abstract class for encapsulating a row of the above part for selection and specification of a
	 * {@link StringValueSource}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private abstract class AbstractExtractNameRowComposite {

		/**
		 * Radio button for enabling / disabling this row controls.
		 */
		protected final Button radioButton;

		/**
		 * Label holding the info image for this row.
		 */
		protected Label infoLabelImage;

		/**
		 * Constructor.
		 *
		 * @param toolkit
		 *            FormToolkit to be used for creation of controls.
		 * @param parent
		 *            parent {@link Composite}.
		 * @param name
		 *            name of the row
		 * @param selected
		 *            initial selection state. if true, this row is initially selected.
		 */
		AbstractExtractNameRowComposite(FormToolkit toolkit, Composite parent, String name, boolean selected) {
			radioButton = toolkit.createButton(parent, name, SWT.RADIO);
			int rowSpan = hasControlsInRow() ? 1 : NUM_COLUMNS - 1;
			radioButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, rowSpan, 1));
			radioButton.setSelection(selected);
			radioButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (radioButton.getSelection()) {
						rowSelectionChanged(AbstractExtractNameRowComposite.this);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});
		}

		/**
		 * Indicates whether this row is activated or not.
		 *
		 * @return true if activated.
		 */
		public boolean isActive() {
			return radioButton.getSelection();
		}

		/**
		 * Sets the enabled state of the radio button and info image.
		 *
		 * @param enabled
		 *            enabled state
		 */
		public void setRadioEnabled(boolean enabled) {
			radioButton.setEnabled(enabled);
			infoLabelImage.setEnabled(enabled);
		}

		/**
		 * Sets the enabled state for the core controls of this row.
		 *
		 * @param enabled
		 *            enabled state
		 */
		protected abstract void setEnabled(boolean enabled);

		/**
		 * Constructs a {@link StringValueSource} instance from the contents of this row's controls.
		 *
		 * @return a {@link StringValueSource} instance.
		 */
		protected abstract StringValueSource constructStringValueSource();

		/**
		 * Initializes the contents of the controls of this row based on the passed
		 * {@link StringValueSource} instance.
		 *
		 * @param stringValueSource
		 *            the {@link StringValueSource} instance to retrieve the contents from.
		 */
		public void initialize(StringValueSource stringValueSource) {
			if (initializationPhase) {
				init(stringValueSource);
			} else {
				initializationPhase = true;
				init(stringValueSource);
				initializationPhase = false;
			}
		}

		/**
		 * Initializes the contents of the controls of this row based on the passed
		 * {@link StringValueSource} instance. Internal use only within the class hierarchy.
		 *
		 * @param stringValueSource
		 *            the {@link StringValueSource} instance to retrieve the contents from.
		 */
		protected abstract void init(StringValueSource stringValueSource);

		/**
		 * Resets the contents of the controls of this row.
		 */
		protected void reset() {
			radioButton.setSelection(false);
		}

		/**
		 * Returns the number of controls in the row of the specific implementation.
		 *
		 * @return the number of controls
		 */
		protected abstract boolean hasControlsInRow();
	}

	/**
	 * Row for specifying a {@link HttpUriValueSource}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class HttpUriNameExtractionRowComposite extends AbstractExtractNameRowComposite {

		/**
		 * Description text.
		 */
		private static final String DESCRIPTION = "Extract name from the URI of a request.";

		/**
		 * Constructor.
		 *
		 * @param toolkit
		 *            FormToolkit to be used for creation of controls.
		 * @param parent
		 *            parent {@link Composite}.
		 * @param selected
		 *            initial selection state. if true, this row is initially selected.
		 */
		HttpUriNameExtractionRowComposite(FormToolkit toolkit, Composite parent, boolean selected) {
			super(toolkit, parent, "HTTP URI", selected);
			infoLabelImage = toolkit.createLabel(parent, "");
			infoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			infoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			infoLabelImage.setToolTipText(DESCRIPTION);
			setEnabled(selected);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setEnabled(boolean enabled) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StringValueSource constructStringValueSource() {
			return new HttpUriValueSource();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init(StringValueSource stringValueSource) {
			if (stringValueSource instanceof HttpUriValueSource) {
				radioButton.setSelection(true);
			} else {
				HttpUriNameExtractionRowComposite.this.reset();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean hasControlsInRow() {
			return false;
		}

	}

	/**
	 * Row for specifying a {@link HttpParameterValueSource}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class HttpParameterNameExtractionRowComposite extends AbstractExtractNameRowComposite {
		/**
		 * Description text.
		 */
		private static final String DESCRIPTION = "Extract name from the value of the specified HTTP parameter. Specify the HTTP parameter name.\n" + "\nWildcards are not allowed here.";

		/**
		 * Label for parameter name.
		 */
		private final Label parameterNameLabel;

		/**
		 * Text control for editing the parameter name.
		 */
		private final Text parameterNameText;

		/**
		 * Constructor.
		 *
		 * @param toolkit
		 *            FormToolkit to be used for creation of controls.
		 * @param parent
		 *            parent {@link Composite}.
		 * @param selected
		 *            initial selection state. if true, this row is initially selected.
		 */
		HttpParameterNameExtractionRowComposite(FormToolkit toolkit, Composite parent, boolean selected) {
			super(toolkit, parent, "HTTP Parameter", selected);
			parameterNameLabel = toolkit.createLabel(parent, "parameter name:");
			parameterNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			parameterNameText = toolkit.createText(parent, "");
			parameterNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, NUM_COLUMNS - 3, 1));
			parameterNameText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (!initializationPhase) {
						markDirty();
					}
				}
			});
			infoLabelImage = toolkit.createLabel(parent, "");
			infoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			infoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			infoLabelImage.setToolTipText(DESCRIPTION);
			setEnabled(selected);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setEnabled(boolean enabled) {
			parameterNameLabel.setEnabled(enabled);
			parameterNameText.setEnabled(enabled);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StringValueSource constructStringValueSource() {
			return new HttpParameterValueSource(parameterNameText.getText());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init(StringValueSource stringValueSource) {
			if (stringValueSource instanceof HttpParameterValueSource) {
				radioButton.setSelection(true);
				String parameterName = ((HttpParameterValueSource) stringValueSource).getParameterName();
				parameterNameText.setText(parameterName);
			} else {
				this.reset();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void reset() {
			super.reset();
			parameterNameText.setText("");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean hasControlsInRow() {
			return true;
		}
	}

	/**
	 * Row for specifying a {@link MethodSignatureValueSource}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class MethodSignatureNameExtractionRowComposite extends AbstractExtractNameRowComposite {
		/**
		 * Description text.
		 */
		private static final String DESCRIPTION = "Extract name from method signature.";

		/**
		 * Constructor.
		 *
		 * @param toolkit
		 *            FormToolkit to be used for creation of controls.
		 * @param parent
		 *            parent {@link Composite}.
		 * @param selected
		 *            initial selection state. if true, this row is initially selected.
		 */
		MethodSignatureNameExtractionRowComposite(FormToolkit toolkit, Composite parent, boolean selected) {
			super(toolkit, parent, "Method Signature", selected);
			infoLabelImage = toolkit.createLabel(parent, "");
			infoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			infoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			infoLabelImage.setToolTipText(DESCRIPTION);
			setEnabled(selected);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setEnabled(boolean enabled) {

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StringValueSource constructStringValueSource() {
			return new MethodSignatureValueSource();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init(StringValueSource stringValueSource) {
			if (stringValueSource instanceof MethodSignatureValueSource) {
				radioButton.setSelection(true);
			} else {
				MethodSignatureNameExtractionRowComposite.this.reset();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean hasControlsInRow() {
			return false;
		}

	}

	/**
	 * Row for specifying a {@link MethodParameterValueSource}.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class MethodParameterNameExtractionRowComposite extends AbstractExtractNameRowComposite {
		/**
		 * Description text.
		 */
		private static final String DESCRIPTION = "Extract name from the value of the specified method parameter.\n"
				+ "Specify the fully qualified method signature and the parameter index (starting with 0).\n" + "\nWildcards are not allowed here.";

		/**
		 * Label for method signature.
		 */
		private final Label methodSignatureLabel;
		/**
		 * Text control for editing the method signature.
		 */
		private final Text methodSignatureText;

		/**
		 * Label for parameter index.
		 */
		private final Label parameterIndexLabel;

		/**
		 * Spinner control for editing parameter index.
		 */
		private final Spinner parameterIndexSpinner;

		/**
		 * Constructor.
		 *
		 * @param toolkit
		 *            FormToolkit to be used for creation of controls.
		 * @param parent
		 *            parent {@link Composite}.
		 * @param selected
		 *            initial selection state. if true, this row is initially selected.
		 */
		MethodParameterNameExtractionRowComposite(FormToolkit toolkit, Composite parent, boolean selected) {
			super(toolkit, parent, "Method Parameter", selected);
			methodSignatureLabel = toolkit.createLabel(parent, "method signature:");
			methodSignatureLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			methodSignatureText = toolkit.createText(parent, "");
			methodSignatureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			methodSignatureText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (!initializationPhase) {
						markDirty();
					}
				}
			});
			parameterIndexLabel = toolkit.createLabel(parent, "parameter index:");
			parameterIndexLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			parameterIndexSpinner = new Spinner(parent, SWT.BORDER);
			parameterIndexSpinner.setMinimum(0);
			parameterIndexSpinner.setMaximum(50);
			parameterIndexSpinner.setSelection(0);
			parameterIndexSpinner.setIncrement(1);
			parameterIndexSpinner.setPageIncrement(5);
			parameterIndexSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			parameterIndexSpinner.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (!initializationPhase) {
						markDirty();
					}
				}
			});
			infoLabelImage = toolkit.createLabel(parent, "");
			infoLabelImage.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			infoLabelImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			infoLabelImage.setToolTipText(DESCRIPTION);
			setEnabled(selected);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setEnabled(boolean enabled) {
			parameterIndexLabel.setEnabled(enabled);
			parameterIndexSpinner.setEnabled(enabled);
			methodSignatureLabel.setEnabled(enabled);
			methodSignatureText.setEnabled(enabled);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StringValueSource constructStringValueSource() {
			return new MethodParameterValueSource(parameterIndexSpinner.getSelection(), methodSignatureText.getText());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init(StringValueSource stringValueSource) {
			if (stringValueSource instanceof MethodParameterValueSource) {
				radioButton.setSelection(true);
				MethodParameterValueSource methodParameterValueSource = (MethodParameterValueSource) stringValueSource;
				String methodSignature = methodParameterValueSource.getMethodSignature();
				int parIndex = methodParameterValueSource.getParameterIndex();
				methodSignatureText.setText(methodSignature);
				parameterIndexSpinner.setSelection(parIndex);
			} else {
				this.reset();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void reset() {
			super.reset();
			methodSignatureText.setText("");
			parameterIndexSpinner.setSelection(0);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean hasControlsInRow() {
			return true;
		}
	}
}
