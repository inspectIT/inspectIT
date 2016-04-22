package rocks.inspectit.ui.rcp.editor;

import java.util.Objects;

import org.eclipse.core.runtime.Assert;

import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Common abstract class for all sub-views.
 *
 * @author Patrice Bouillet
 *
 */
public abstract class AbstractSubView implements ISubView {

	/**
	 * The root editor.
	 */
	private AbstractRootEditor rootEditor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRootEditor(AbstractRootEditor rootEditor) {
		Assert.isNotNull(rootEditor);

		this.rootEditor = rootEditor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractRootEditor getRootEditor() {
		Assert.isNotNull(rootEditor);

		return rootEditor;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Default implementation does nothing.
	 */
	@Override
	public void select(ISubView subView) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Checks if the view is of the given class and if so returns itself.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends ISubView> E getSubView(Class<E> clazz) {
		if (Objects.equals(clazz, this.getClass())) {
			return (E) this;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISubView getSubViewWithInputController(Class<?> inputControllerClass) {
		return null;
	}

	/**
	 * @return Returns the string for the data retrieving job.
	 */
	protected String getDataLoadingJobName() {
		RepositoryDefinition repositoryDefinition = getRootEditor().getInputDefinition().getRepositoryDefinition();
		return "Retrieving data from " + repositoryDefinition.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

}
