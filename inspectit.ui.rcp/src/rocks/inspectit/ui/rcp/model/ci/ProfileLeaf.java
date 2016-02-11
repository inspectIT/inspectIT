package rocks.inspectit.ui.rcp.model.ci;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.eclipse.core.runtime.Assert;

import com.google.common.base.Objects;

import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.model.Leaf;
import rocks.inspectit.ui.rcp.provider.IProfileProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Profile leaf for displaying the the tree.
 * 
 * @author Ivan Senic
 * 
 */
public class ProfileLeaf extends Leaf implements IProfileProvider {

	/**
	 * {@link Profile}.
	 */
	private Profile profile;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public ProfileLeaf(Profile profile, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super();
		Assert.isNotNull(profile);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.profile = profile;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setName(profile.getName());
		this.setTooltip(profile.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	public Profile getProfile() {
		return profile;
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
	public int compareTo(IProfileProvider o) {
		Profile other = o.getProfile();
		return new CompareToBuilder().append(profile.isCommonProfile(), other.isCommonProfile()).append(profile.getName(), other.getName()).toComparison();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(profile, cmrRepositoryDefinition);
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
		ProfileLeaf that = (ProfileLeaf) object;
		return Objects.equal(this.profile, that.profile) && Objects.equal(this.cmrRepositoryDefinition, that.cmrRepositoryDefinition);
	}

}
