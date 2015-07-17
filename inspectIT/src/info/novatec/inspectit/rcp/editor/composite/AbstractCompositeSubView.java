package info.novatec.inspectit.rcp.editor.composite;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Some general methods for composite views are implemented in here.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractCompositeSubView extends AbstractSubView {

	/**
	 * The list containing all the sub-views which are painted in this composite sub-view.
	 */
	private List<ISubView> subViews = new ArrayList<ISubView>();

	/**
	 * Maximizes the given {@link ISubView}. The {@link ISubView} has to contained in this composite
	 * sub-view.
	 * 
	 * @param subView
	 *            Sub-view to maximize.
	 */
	public abstract void maximizeSubView(ISubView subView);

	/**
	 * Minimizes the given {@link ISubView}. The {@link ISubView} has to contained in this composite
	 * sub-view.
	 */
	public abstract void restoreMaximization();

	/**
	 * Layouts the sub-views.
	 */
	public abstract void layout();

	/**
	 * Adds a new sub-view to this composite view.
	 * 
	 * @param subView
	 *            The {@link ISubView} which will be added.
	 */
	public void addSubView(ISubView subView) {
		subViews.add(subView);
	}

	/**
	 * @return the subViews
	 */
	public List<ISubView> getSubViews() {
		// makes the list unmodifiable so that it can not be edited.
		return Collections.unmodifiableList(subViews);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends ISubView> E getSubView(Class<E> clazz) {
		E view = super.getSubView(clazz);
		if (null != view) {
			return view;
		} else {
			for (ISubView subView : getSubViews()) {
				view = subView.getSubView(clazz);
				if (null != view) {
					return view;
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISubView getSubViewWithInputController(Class<?> inputControllerClass) {
		for (ISubView subView : getSubViews()) {
			ISubView view = subView.getSubViewWithInputController(inputControllerClass);
			if (null != view) {
				return view;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates the select to all sub views. Implementing classes can override.
	 */
	@Override
	public void select(ISubView subView) {
		for (ISubView containedSubView : getSubViews()) {
			containedSubView.select(subView);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferenceIds = EnumSet.noneOf(PreferenceId.class);

		for (ISubView subView : subViews) {
			preferenceIds.addAll(subView.getPreferenceIds());
		}

		return preferenceIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.doRefresh();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.preferenceEventFired(preferenceEvent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.setDataInput(data);
		}
		layout();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		for (ISubView subView : subViews) {
			subView.init();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRootEditor(AbstractRootEditor rootEditor) {
		super.setRootEditor(rootEditor);

		for (ISubView subView : subViews) {
			subView.setRootEditor(rootEditor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.dispose();
		}
	}

}