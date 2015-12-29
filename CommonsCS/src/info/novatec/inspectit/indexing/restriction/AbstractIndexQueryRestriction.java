package info.novatec.inspectit.indexing.restriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract class for all index query restriction classes.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractIndexQueryRestriction implements IIndexQueryRestriction {

	/**
	 * Getter methods names that needs to be invoked in order to get the object to check the
	 * restriction on.
	 */
	private final List<String> methodNames;

	/**
	 * Default constructor.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to. If you need navigation use the
	 *            '.' to separate fields. For example person.age will navigate to the age field to
	 *            execute the restriction on.
	 */
	public AbstractIndexQueryRestriction(String fieldName) {
		if (null == fieldName) {
			throw new IllegalArgumentException();
		}

		String[] split = StringUtils.splitPreserveAllTokens(fieldName, '.');
		methodNames = new ArrayList<String>(split.length);

		for (int i = 0, size = split.length; i < size; i++) {
			methodNames.add(getMethodName(split[i]));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getQualifiedMethodNames() {
		return Collections.unmodifiableList(methodNames);
	}

	/**
	 * Returns getter method name based on the field name.
	 * 
	 * @param fieldName
	 *            name of the field
	 * @return getter method name
	 */
	private String getMethodName(String fieldName) {
		return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodNames == null) ? 0 : methodNames.hashCode());
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
		AbstractIndexQueryRestriction other = (AbstractIndexQueryRestriction) obj;
		if (methodNames == null) {
			if (other.methodNames != null) {
				return false;
			}
		} else if (!methodNames.equals(other.methodNames)) {
			return false;
		}
		return true;
	}

}
