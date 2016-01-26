package rocks.inspectit.shared.all.pattern;

import com.google.common.base.Objects;

/**
 * Matching pattern on the equals basis.
 *
 * @author Ivan Senic
 *
 */
public class EqualsMatchPattern implements IMatchPattern {

	/**
	 * Template to match.
	 */
	private String template;

	/**
	 * No-arg constructor for serialization.
	 */
	public EqualsMatchPattern() {
	}

	/**
	 * Default constructor.
	 *
	 * @param template
	 *            Template to match.
	 */
	public EqualsMatchPattern(String template) {
		this.template = template;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean match(String match) {
		return Objects.equal(template, match);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPattern() {
		return template;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((template == null) ? 0 : template.hashCode());
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
		EqualsMatchPattern other = (EqualsMatchPattern) obj;
		if (template == null) {
			if (other.template != null) {
				return false;
			}
		} else if (!template.equals(other.template)) {
			return false;
		}
		return true;
	}

}
