package rocks.inspectit.ui.rcp.editor.composite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.editor.ISubView;

/**
 * The sash composite can create a composite sub-view which lays out its children either vertical or
 * horizontal. The behavior can be set by passing the style {@link SWT#HORIZONTAL} or
 * {@link SWT#VERTICAL} to the constructor. The default is {@link SWT#VERTICAL} + {@link SWT#SMOOTH}
 * ;
 * 
 * @author Patrice Bouillet
 * 
 */
public class SashCompositeSubView extends AbstractCompositeSubView {

	/**
	 * The style of the sash form.
	 */
	private int sashFormStyle = SWT.VERTICAL | SWT.SMOOTH;

	/**
	 * The generated composite of this sub-view.
	 */
	private SashForm sashForm;

	/**
	 * The weight mapping.
	 */
	private Map<ISubView, Integer> weightMapping = new HashMap<ISubView, Integer>();

	/**
	 * Default constructor which takes no arguments.
	 */
	public SashCompositeSubView() {
	}

	/**
	 * Additional constructor which can specify the style of the sash form.
	 * <p>
	 * <dl>
	 * <b>Styles:</b> HORIZONTAL, VERTICAL, SMOOTH
	 * </dl>
	 * </p>
	 * 
	 * @param style
	 *            The style of sash form to construct.
	 */
	public SashCompositeSubView(int style) {
		this.sashFormStyle = style;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		sashForm = new SashForm(parent, sashFormStyle);
		sashForm.setLayout(new GridLayout(1, false));

		List<ISubView> subViews = getSubViews();

		for (final ISubView subView : subViews) {
			subView.createPartControl(sashForm, toolkit);
			subView.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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

		if (!weightMapping.isEmpty()) {
			int[] weights = new int[subViews.size()];
			for (int i = 0; i < subViews.size(); i++) {
				if (weightMapping.containsKey(subViews.get(i))) {
					weights[i] = weightMapping.get(subViews.get(i));
				}
			}
			sashForm.setWeights(weights);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSubView(ISubView subView, int weight) {
		super.addSubView(subView);

		weightMapping.put(subView, weight);
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return sashForm;
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

		int[] weights = new int[getSubViews().size()];
		int i = 0;
		for (ISubView view : getSubViews()) {
			if (ObjectUtils.equals(view, maximizeSubView)) {
				view.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				weights[i] = 1;
			} else {
				GridData gd = new GridData();
				gd.exclude = true;
				view.getControl().setLayoutData(gd);
				weights[i] = 0;
			}
			i++;
		}
		sashForm.setWeights(weights);
		layout();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restoreMaximization() {
		int[] weights = new int[getSubViews().size()];
		int i = 0;
		for (ISubView view : getSubViews()) {
			view.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			weights[i] = weightMapping.get(view);
			i++;
		}
		sashForm.setWeights(weights);
		layout();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void layout() {
		sashForm.layout();
	}

}
