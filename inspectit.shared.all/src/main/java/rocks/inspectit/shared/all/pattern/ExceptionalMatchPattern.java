package rocks.inspectit.shared.all.pattern;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;

/**
 * Exceptional match pattern has ability to specify one main pattern to match and in addition
 * exceptional patterns. If any of the exceptional patterns is matching, then {@link #match(String)}
 * returns <code>false</code>. Otherwise the result of the {@link #match(String)} depends on the
 * main {@link #pattern}.
 *
 * @author Ivan Senic
 *
 */
public class ExceptionalMatchPattern implements IMatchPattern {

	/**
	 * Main patter that should be matched.
	 */
	private IMatchPattern pattern;

	/**
	 * Exceptional patterns that should not be matched. If any of the exceptional patterns is
	 * matching, then {@link #match(String)} returns <code>false</code> no matter of
	 * {@link #pattern}.
	 */
	private Collection<IMatchPattern> exceptions;

	/**
	 * No-arg constructor.
	 */
	protected ExceptionalMatchPattern() {
	}

	/**
	 * Default constructor.
	 *
	 * @param pattern
	 *            Main patter that should be matched.
	 * @param exceptions
	 *            Exceptional patterns that should not be matched.
	 */
	public ExceptionalMatchPattern(IMatchPattern pattern, Collection<IMatchPattern> exceptions) {
		if (null == pattern) {
			throw new IllegalArgumentException("Pattern can not be null.");
		}

		this.pattern = pattern;
		this.exceptions = exceptions;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Checks if the main {@link #pattern} matches and then checks that none of the
	 * {@link #exceptions} patterns is not matching.
	 */
	@Override
	public boolean match(String match) {
		boolean matched = pattern.match(match);
		if (matched && CollectionUtils.isNotEmpty(exceptions)) {
			for (IMatchPattern exceptionPattern : exceptions) {
				if (exceptionPattern.match(match)) {
					matched = false;
					break;
				}
			}
		}
		return matched;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@link #pattern#getPattern()}.
	 */
	@Override
	public String getPattern() {
		return pattern.getPattern();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.exceptions == null) ? 0 : this.exceptions.hashCode());
		result = (prime * result) + ((this.pattern == null) ? 0 : this.pattern.hashCode());
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
		ExceptionalMatchPattern other = (ExceptionalMatchPattern) obj;
		if (this.exceptions == null) {
			if (other.exceptions != null) {
				return false;
			}
		} else if (!this.exceptions.equals(other.exceptions)) {
			return false;
		}
		if (this.pattern == null) {
			if (other.pattern != null) {
				return false;
			}
		} else if (!this.pattern.equals(other.pattern)) {
			return false;
		}
		return true;
	}

}
