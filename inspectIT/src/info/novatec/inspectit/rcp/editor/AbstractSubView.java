package info.novatec.inspectit.rcp.editor;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.Objects;

import org.eclipse.core.runtime.Assert;

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
	public void setRootEditor(AbstractRootEditor rootEditor) {
		Assert.isNotNull(rootEditor);

		this.rootEditor = rootEditor;
	}

	/**
	 * {@inheritDoc}
	 */
	public AbstractRootEditor getRootEditor() {
		Assert.isNotNull(rootEditor);

		return rootEditor;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Default implementation does nothing.
	 */
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
	public void dispose() {
	}

}
