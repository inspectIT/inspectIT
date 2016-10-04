package rocks.inspectit.ui.rcp.model.ci;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.eclipse.core.runtime.Assert;

import com.google.common.base.Objects;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.model.Leaf;
import rocks.inspectit.ui.rcp.provider.IAlertDefinitionProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Alert definition leaf for displaying in the table.
 *
 * @author Alexander Wert
 *
 */
public class AlertDefinitionLeaf extends Leaf implements IAlertDefinitionProvider {
	/**
	 * {@link AlertingDefinition}.
	 */
	private AlertingDefinition alertDefinition;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 *
	 * @param alertDefinition
	 *            {@link AlertingDefinition}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public AlertDefinitionLeaf(AlertingDefinition alertDefinition, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super();
		Assert.isNotNull(alertDefinition);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.alertDefinition = alertDefinition;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setName(alertDefinition.getName());
		this.setTooltip(alertDefinition.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlertingDefinition getAlertDefinition() {
		return alertDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(IAlertDefinitionProvider o) {
		AlertingDefinition other = o.getAlertDefinition();
		return new CompareToBuilder().append(alertDefinition.getName(), other.getName()).toComparison();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(alertDefinition, cmrRepositoryDefinition);
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
		AlertDefinitionLeaf that = (AlertDefinitionLeaf) object;
		return Objects.equal(this.alertDefinition, that.alertDefinition) && Objects.equal(this.cmrRepositoryDefinition, that.cmrRepositoryDefinition);
	}
}
