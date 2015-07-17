package info.novatec.inspectit.rcp.filter;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

/**
 * A composite that displays a text box with {@link SWT#ICON_SEARCH} and {@link SWT#ICON_CANCEL}
 * properties. The text box has a default text that is displayed in gray color. This text can be
 * defined in the constructor.
 * <p>
 * The subclasses should implement two methods, one for executing the filter and one for canceling
 * the filtering.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class FilterComposite extends Composite {

	/**
	 * Time in milliseconds that will be used to wait for another input character before filter is
	 * executed.
	 */
	private static final int FILTER_KEYRELEASED_DELAY = 300;

	/**
	 * Boolean that will keep if the filter was executed. This will help to determine if the
	 * subclasses should be informed via {@link #executeCancel()}.
	 */
	private boolean filterExecuted = false;

	/**
	 * Filter text box.
	 */
	private Text filterText;

	/**
	 * Default text.
	 */
	private String defaultText;

	/**
	 * {@link UIJob} that is executing the filter with delay.
	 */
	private UIJob filterJob;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            A widget which will be the parent of the new instance (cannot be null).
	 * @param style
	 *            The style of widget to construct.
	 * @param defaultText
	 *            Text to be displayed in the text box when no filtering is active.
	 * @see Composite#Composite(Composite, int)
	 */
	public FilterComposite(Composite parent, int style, String defaultText) {
		super(parent, style);
		this.defaultText = defaultText;
		filterJob = new UIJob("Filter Storage") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				executeFilterInternal();
				return Status.OK_STATUS;
			}
		};
		filterJob.setUser(false);
		init();
	}

	/**
	 * This method is called when filtering should be canceled. Subclasses should implement proper
	 * actions.
	 */
	protected abstract void executeCancel();

	/**
	 * This method is called when filtering should occur. Subclasses should implement proper
	 * actions.
	 * 
	 * @param filterString
	 *            String that was entered as a criteria in the filter text box.
	 */
	protected abstract void executeFilter(String filterString);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		filterText.setEnabled(enabled);
	}

	/**
	 * Initializes the widget.
	 */
	private void init() {
		setLayout(new GridLayout(1, false));

		filterText = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setDefaultText();
		filterText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					if (!Objects.equals(filterText.getText(), defaultText)) {
						executeCancelInternal();
					}
				}
			}
		});
		filterText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (Objects.equals(filterText.getText(), defaultText)) {
					setEmptyText();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (Objects.equals(filterText.getText(), "")) {
					setDefaultText();
				}
			}
		});
		filterText.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					filterJob.cancel();
					executeCancelInternal();
				} else if (e.detail == SWT.TRAVERSE_RETURN) {
					filterJob.cancel();
					executeFilterInternal();
				}
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (Character.isLetterOrDigit(e.character) || e.keyCode == SWT.BS || e.keyCode == SWT.DEL) {
					filterJob.cancel();
					filterJob.schedule(FILTER_KEYRELEASED_DELAY);
				}
			}
		});

	}

	/**
	 * Executes cancel internally.
	 */
	private void executeCancelInternal() {
		if (filterExecuted) {
			filterJob.cancel();
			executeCancel();
			filterExecuted = false;
		}
		setDefaultText();
		this.forceFocus();
	}

	/**
	 * Executes filter internally.
	 */
	private void executeFilterInternal() {
		String filterString = filterText.getText().trim();
		executeFilter(filterString);
		filterExecuted = true;
	}

	/**
	 * Sets default text in gray color.
	 */
	private void setDefaultText() {
		filterText.setText(defaultText);
		filterText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
	}

	/**
	 * Empties the text box and set font color to black.
	 */
	private void setEmptyText() {
		filterText.setText("");
		filterText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
	}
}