package info.novatec.inspectit.rcp.editor.search;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchCriteria;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchResult;
import info.novatec.inspectit.util.ObjectUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Control that is displayed for search purposes.
 * 
 * @author Ivan Senic
 * 
 */
public class SearchControl {

	/**
	 * {@link info.novatec.inspectit.rcp.editor.ISubView} to notify about search.
	 */
	private ISearchExecutor searchExecutor;

	/**
	 * Main composite for showing the components.
	 */
	private Composite mainComposite;

	/**
	 * Search text box.
	 */
	private Text searchTextBox;

	/**
	 * Is case sensitive.
	 */
	private ToolItem caseSensitiveButton;

	/**
	 * Close button.
	 */
	private ToolItem closeButton;

	/**
	 * Shell that we will create to display the search.
	 */
	private Shell shell;

	/**
	 * Next button.
	 */
	private ToolItem next;

	/**
	 * Previous button.
	 */
	private ToolItem previous;

	/**
	 * Last {@link SearchResult}.
	 */
	private SearchResult lastSearchResult;

	/**
	 * Default constructor.
	 * 
	 * @param searchExecutor
	 *            The implementation that will be pass the search string.
	 * @param parentShell
	 *            Shell where the search will be created.
	 * @param paintRelativeControl
	 *            Control where the paint of the search box should occur.
	 * @param editor
	 *            Editor where the search will be painted. This is needed because of the editor
	 *            closing, hiding, etc action.
	 */
	public SearchControl(ISearchExecutor searchExecutor, Shell parentShell, Control paintRelativeControl, IEditorPart editor) {
		this.searchExecutor = searchExecutor;
		createSearchShell(parentShell, paintRelativeControl, editor);
	}

	/**
	 * Creates the control. The control will be painted in the top-right corner of the
	 * controlToPaint.
	 * 
	 * @param parentShell
	 *            Parent shell.
	 * @param paintRelativeControl
	 *            Control where the paint of the search box should occur.
	 * @param editor
	 *            Editor where the search will be painted. This is needed because of the editor
	 *            closing, hiding, etc action.
	 */
	private void createSearchShell(Shell parentShell, final Control paintRelativeControl, final IEditorPart editor) {
		Display display = Display.getDefault();
		FormColors formColors = new FormColors(display);
		FormToolkit toolkit = new FormToolkit(formColors);
		shell = new Shell(parentShell, SWT.BORDER | SWT.TOOL);

		mainComposite = toolkit.createComposite(shell);
		GridLayout layout = new GridLayout(3, false);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		searchTextBox = toolkit.createText(mainComposite, null, SWT.BORDER);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.minimumWidth = 200;

		searchTextBox.setLayoutData(gd);
		searchTextBox.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					executeSearch();
				}
			}
		});
		searchTextBox.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (null != lastSearchResult) {
					String string = lastSearchResult.getCurrentOccurence() + " of " + lastSearchResult.getTotalOccurrences();
					if (lastSearchResult.getTotalOccurrences() > 0) {
						paintString(e, string, Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
					} else {
						paintString(e, string, Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}
				}
			}

			private void paintString(PaintEvent e, String string, Color color) {
				Point point = searchTextBox.getSize();

				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * string.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(color);
				e.gc.drawString(string, point.x - width - searchTextBox.getBorderWidth() - 2, (point.y - height - searchTextBox.getBorderWidth() * 2) / 2, true);
			}
		});

		ToolBar toolBar = new ToolBar(mainComposite, SWT.FLAT);
		toolBar.setBackground(formColors.getBackground());

		previous = new ToolItem(toolBar, SWT.PUSH | SWT.NO_BACKGROUND);
		previous.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_PREVIOUS));
		previous.setEnabled(false);
		previous.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executePrevious();
			}
		});

		next = new ToolItem(toolBar, SWT.PUSH | SWT.NO_BACKGROUND);
		next.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_NEXT));
		next.setEnabled(false);
		next.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executeNext();
			}
		});

		caseSensitiveButton = new ToolItem(toolBar, SWT.CHECK | SWT.NO_BACKGROUND);
		caseSensitiveButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_FONT));
		caseSensitiveButton.setToolTipText("Case sensitive");
		caseSensitiveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executeSearch();
			}
		});

		// added additional composite to the right, so that minimizing and maximizing the window
		// can look better
		Composite helpComposite = toolkit.createComposite(mainComposite);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.minimumWidth = 0;
		gd.heightHint = 0;
		gd.widthHint = 0;
		helpComposite.setLayoutData(gd);

		closeButton = new ToolItem(toolBar, SWT.PUSH | SWT.NO_BACKGROUND);
		closeButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CLOSE));
		closeButton.setToolTipText("Close");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchExecutor.clearSearch();
				closeControl();
			}
		});

		KeyAdapter keyCloseAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					searchExecutor.clearSearch();
					closeControl();
				}
			}
		};

		mainComposite.addKeyListener(keyCloseAdapter);
		for (Control child : mainComposite.getChildren()) {
			child.addKeyListener(keyCloseAdapter);
		}

		searchTextBox.forceFocus();

		mainComposite.pack();
		mainComposite.setSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		final Point shellSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point controlPosition = paintRelativeControl.toDisplay(0, 0);
		Point controlSize = paintRelativeControl.getSize();

		int xPosition = controlPosition.x + controlSize.x - shellSize.x - paintRelativeControl.getBorderWidth();
		int yPosition = controlPosition.y + controlSize.y - shellSize.y - paintRelativeControl.getBorderWidth();

		shell.setLocation(xPosition, yPosition);
		shell.setSize(shellSize);
		shell.open();

		paintRelativeControl.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (!shell.isDisposed()) {
					Point controlPosition = paintRelativeControl.toDisplay(0, 0);
					Point controlSize = paintRelativeControl.getSize();
					int xPosition = controlPosition.x + controlSize.x - shellSize.x - paintRelativeControl.getBorderWidth();
					int yPosition = controlPosition.y + controlSize.y - shellSize.y - paintRelativeControl.getBorderWidth();

					shell.setLocation(xPosition, yPosition);
				}
			}
		});

		editor.getSite().getPage().addPartListener(new IPartListener() {
			@Override
			public void partOpened(IWorkbenchPart part) {
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
				if (ObjectUtils.equals(editor, part)) {
					closeControl();
				}
			}

			@Override
			public void partClosed(IWorkbenchPart part) {
				if (ObjectUtils.equals(editor, part)) {
					closeControl();
				}
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}

			@Override
			public void partActivated(IWorkbenchPart part) {
			}
		});

		OpenedSearchControlCache.register(searchExecutor, this);
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				searchExecutor.clearSearch();
				OpenedSearchControlCache.unregister(searchExecutor);
			}
		});

		toolkit.dispose();
	}

	/**
	 * Closes the control.
	 */
	public final void closeControl() {
		if (!shell.isDisposed()) {
			shell.close();
		}
	}

	/**
	 * Executes the search.
	 */
	private void executeSearch() {
		String searchString = searchTextBox.getText().trim();
		if (!searchString.isEmpty()) {
			SearchCriteria searchCriteria = new SearchCriteria(searchString, caseSensitiveButton.getSelection());
			lastSearchResult = searchExecutor.executeSearch(searchCriteria);
			processSearchResult(lastSearchResult);
		} else {
			searchExecutor.clearSearch();
			lastSearchResult = null; // NOPMD
			searchTextBox.redraw();
		}
	}

	/**
	 * Executes next functionality.
	 */
	private void executeNext() {
		lastSearchResult = searchExecutor.next();
		processSearchResult(lastSearchResult);
	}

	/**
	 * Execute previous functionality.
	 */
	private void executePrevious() {
		lastSearchResult = searchExecutor.previous();
		processSearchResult(lastSearchResult);
	}

	/**
	 * Processes the {@link SearchResult}.
	 * 
	 * @param result
	 *            {@link SearchResult}.
	 */
	private void processSearchResult(SearchResult result) {
		searchTextBox.redraw();
		if (null != result) {
			next.setEnabled(result.isCanShowNext());
			previous.setEnabled(result.isCanShowPrevious());
		}
	}
}
