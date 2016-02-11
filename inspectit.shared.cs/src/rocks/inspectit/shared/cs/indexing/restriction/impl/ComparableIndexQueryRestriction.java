package info.novatec.inspectit.indexing.restriction.impl;

import info.novatec.inspectit.indexing.restriction.AbstractIndexQueryRestriction;

/**
 * Abstract class for all restrictions that use comparable as restriction values.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class ComparableIndexQueryRestriction extends AbstractIndexQueryRestriction {

	/**
	 * Restriction value.
	 */
	@SuppressWarnings("rawtypes")
	private Comparable restrictionValue;

	/**
	 * Default constructor.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value.
	 */
	@SuppressWarnings("rawtypes")
	public ComparableIndexQueryRestriction(String fieldName, Comparable restrictionValue) {
		super(fieldName);
		this.restrictionValue = restrictionValue;
	}

	/**
	 * 
	 * @return Restriction value.
	 */
	@SuppressWarnings("rawtypes")
	protected Comparable getRestrictionValue() {
		return restrictionValue;
	}

}
