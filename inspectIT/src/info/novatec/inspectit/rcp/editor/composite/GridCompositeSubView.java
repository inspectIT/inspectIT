package info.novatec.inspectit.rcp.editor.composite;

import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This implementation of a composite sub view uses the {@link GridLayout} to layout the associated
 * children.
 * 
 * @author Patrice Bouillet
 * 
 */
public class GridCompositeSubView extends AbstractCompositeSubView {

	/**
	 * The map contains the layout data objects.
	 */
	private Map<ISubView, Object> layoutDataMap = new HashMap<ISubView, Object>();

	/**
	 * The composite of this sub-view.
	 */
	private Composite composite;

	/**
	 * The layout of the contained composite.
	 */
	private GridLayout gridLayout;

	/**
	 * Default constructor which calls
	 * {@link GridCompositeSubView#GridCompositeSubView(int, boolean)} with values <code>1</code>
	 * for the number of columns and <code>false</code> if the columns should have an equal width.
	 */
	public GridCompositeSubView() {
		this(1, false);
	}

	/**
	 * Constructor which constructs the {@link GridLayout} object with the passed values.
	 * 
	 * @param numColumns
	 *            The number of columns.
	 * @param makeColumnsEqualWidth
	 *            If the columns should have an equal width.
	 * @see GridLayout
	 */
	public GridCompositeSubView(int numColumns, boolean makeColumnsEqualWidth) {
		gridLayout = new GridLayout(numColumns, makeColumnsEqualWidth);
	}

	/**
	 * Adds a new sub-view with the specified layout data to this composite view.
	 * 
	 * @param subView
	 *            The {@link ISubView} which will be added.
	 * @param layoutData
	 *            The layout data of the corresponding sub-view.
	 */
	public void addSubView(ISubView subView, Object layoutData) {
		super.addSubView(subView);
		layoutDataMap.put(subView, layoutData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		composite = toolkit.createComposite(parent);
		composite.setLayout(gridLayout);

		for (final ISubView subView : getSubViews()) {
			subView.createPartControl(composite, toolkit);
			if (layoutDataMap.containsKey(subView)) {
				subView.getControl().setLayoutData(layoutDataMap.get(subView));
			}

			subView.getControl().addFocusListener(new FocusAdapter() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public void focusGained(FocusEvent e) {
					getRootEditor().setActiveSubView(subView);
				}
			});

			if (null != subView.getSelectionProvider()) {
				ISelectionProvider prov = subView.getSelectionProvider();
				prov.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						getRootEditor().setSelection(event.getSelection());
					}
				});
				prov.addSelectionChangedListener(getRootEditor().getSelectionChangedListener());
				if (prov instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) prov).addPostSelectionChangedListener(getRootEditor().getPostSelectionChangedListener());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void maximizeSubView(ISubView subView) {
		ISubView maximizeSubView = subView;
		if (maximizeSubView == null) {
			maximizeSubView = getSubViews().get(0);
		}

		for (ISubView view : getSubViews()) {
			if (ObjectUtils.equals(view, maximizeSubView)) {
				view.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			} else {
				GridData gd = new GridData();
				gd.exclude = true;
				view.getControl().setLayoutData(gd);
			}
		}
		layout();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restoreMaximization() {
		for (ISubView view : getSubViews()) {
			view.getControl().setLayoutData(layoutDataMap.get(view));
		}
		layout();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void layout() {
		composite.layout();
	}

}
