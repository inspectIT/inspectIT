package rocks.inspectit.shared.cs.ci.exclude;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines a class name or pattern to exclude from instrumentation.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "exclude-rule")
public class ExcludeRule {

	/**
	 * No-arg constructor.
	 */
	public ExcludeRule() {
	}

	/**
	 * Creates new {@link ExcludeRule}.
	 *
	 * @param className
	 *            Name of the class or pattern.
	 */
	public ExcludeRule(String className) {
		this.className = className;
	}

	/**
	 * Class name/pattern.
	 */
	@XmlAttribute(name = "class-name", required = true)
	private String className;

	/**
	 * Exceptions to the exclude class name or patter. Exceptions can also be FQN or patterns.
	 */
	@XmlAttribute(name = "exceptions")
	private List<String> exceptions = new ArrayList<>(0);

	/**
	 * Gets {@link #className}.
	 *
	 * @return {@link #className}
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets {@link #className}.
	 *
	 * @param className
	 *            New value for {@link #className}
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Gets {@link #exceptions}.
	 *
	 * @return {@link #exceptions}
	 */
	public List<String> getExceptions() {
		return this.exceptions;
	}

	/**
	 * Sets {@link #exceptions}.
	 *
	 * @param exceptions
	 *            New value for {@link #exceptions}
	 */
	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.className == null) ? 0 : this.className.hashCode());
		result = (prime * result) + ((this.exceptions == null) ? 0 : this.exceptions.hashCode());
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
		ExcludeRule other = (ExcludeRule) obj;
		if (this.className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!this.className.equals(other.className)) {
			return false;
		}
		if (this.exceptions == null) {
			if (other.exceptions != null) {
				return false;
			}
		} else if (!this.exceptions.equals(other.exceptions)) {
			return false;
		}
		return true;
	}

}
