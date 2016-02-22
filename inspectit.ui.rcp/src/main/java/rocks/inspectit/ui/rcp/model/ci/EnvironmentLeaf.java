package rocks.inspectit.ui.rcp.model.ci;

import org.eclipse.core.runtime.Assert;

import com.google.common.base.Objects;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.model.Leaf;
import rocks.inspectit.ui.rcp.provider.IEnvironmentProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Environment leaf for displaying in the tree.
 * 
 * @author Ivan Senic
 * 
 */
public class EnvironmentLeaf extends Leaf implements IEnvironmentProvider {

	/**
	 * {@link Environment}.
	 */
	private Environment environment;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 * 
	 * @param environment
	 *            {@link Environment}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public EnvironmentLeaf(Environment environment, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super();
		Assert.isNotNull(environment);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.environment = environment;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setName(environment.getName());
		this.setTooltip(environment.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * {@inheritDoc}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(environment, cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		EnvironmentLeaf that = (EnvironmentLeaf) object;
		return Objects.equal(this.environment, that.environment) && Objects.equal(this.cmrRepositoryDefinition, that.cmrRepositoryDefinition);
	}

}
