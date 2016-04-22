package rocks.inspectit.shared.cs.ci.profile.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;

/**
 * Profile data for the exclude rules.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "exclude-rules-profile-data")
public class ExcludeRulesProfileData extends AbstractProfileData<List<ExcludeRule>> {

	/**
	 * Name.
	 */
	private static final String NAME = "Exclude Rules";

	/**
	 * {@link ExcludeRule}s.
	 */
	@XmlElementRefs({ @XmlElementRef(type = ExcludeRule.class) })
	private List<ExcludeRule> excludeRules;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ExcludeRule> getData() {
		return excludeRules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Gets {@link #excludeRules}.
	 *
	 * @return {@link #excludeRules}
	 */
	public List<ExcludeRule> getExcludeRules() {
		return excludeRules;
	}

	/**
	 * Sets {@link #excludeRules}.
	 *
	 * @param excludeRules
	 *            New value for {@link #excludeRules}
	 */
	public void setExcludeRules(List<ExcludeRule> excludeRules) {
		this.excludeRules = excludeRules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((excludeRules == null) ? 0 : excludeRules.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExcludeRulesProfileData other = (ExcludeRulesProfileData) obj;
		if (excludeRules == null) {
			if (other.excludeRules != null) {
				return false;
			}
		} else if (!excludeRules.equals(other.excludeRules)) {
			return false;
		}
		return true;
	}

}
