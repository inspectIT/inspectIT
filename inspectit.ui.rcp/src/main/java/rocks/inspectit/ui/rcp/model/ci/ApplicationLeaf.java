package rocks.inspectit.ui.rcp.model.ci;

import java.util.List;

import org.eclipse.core.runtime.Assert;

import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.model.Leaf;
import rocks.inspectit.ui.rcp.provider.IApplicationProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Application leaf for displaying in the tree.
 *
 * @author Alexander Wert
 *
 */
public class ApplicationLeaf extends Leaf implements IApplicationProvider {
	/**
	 * {@link ApplicationDefinition}.
	 */
	private ApplicationDefinition application;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private final CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * The parent list containing this {@link ApplicationLeaf} instance. This list is used to
	 * retrieve the position of this {@link ApplicationLeaf} instance.
	 */
	private final List<? extends IApplicationProvider> parentList;

	/**
	 * Default constructor.
	 *
	 * @param application
	 *            {@link ApplicationDefinition}
	 * @param parentList
	 *            The parent list containing the {@link ApplicationDefinition} instance represented
	 *            by this {@link ApplicationLeaf}.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public ApplicationLeaf(ApplicationDefinition application, List<? extends IApplicationProvider> parentList, CmrRepositoryDefinition cmrRepositoryDefinition) {
		Assert.isNotNull(application);
		Assert.isNotNull(cmrRepositoryDefinition);
		Assert.isNotNull(parentList);
		this.setApplication(application);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setName(application.getApplicationName());
		this.setTooltip(application.getApplicationName());
		this.parentList = parentList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIndexInParentList() {
		return getParentList().indexOf(this);
	}

	@Override
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationDefinition getApplication() {
		return application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<? extends IApplicationProvider> getParentList() {
		return parentList;
	}

	/**
	 * Sets {@link #application}.
	 *
	 * @param application
	 *            New value for {@link #application}
	 */
	public void setApplication(ApplicationDefinition application) {
		this.application = application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((application == null) ? 0 : application.getId());
		result = prime * result + ((cmrRepositoryDefinition == null) ? 0 : cmrRepositoryDefinition.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ApplicationLeaf other = (ApplicationLeaf) obj;
		if (application == null) {
			if (other.application != null) {
				return false;
			}
		} else if (application.getId() != other.application.getId()) {
			return false;
		}
		if (cmrRepositoryDefinition == null) {
			if (other.cmrRepositoryDefinition != null) {
				return false;
			}
		} else if (!cmrRepositoryDefinition.equals(other.cmrRepositoryDefinition)) {
			return false;
		}
		return true;
	}
}
