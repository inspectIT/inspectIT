package info.novatec.inspectit.indexing.restriction.impl;

import info.novatec.inspectit.indexing.restriction.AbstractIndexQueryRestriction;

/**
 * Abstract class for all restrictions that use object as restriction values.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class ObjectIndexQueryRestriction extends AbstractIndexQueryRestriction {

	/**
	 * Restriction value.
	 */
	private Object restrictionValue;

	/**
	 * Default constructor.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value.
	 */
	public ObjectIndexQueryRestriction(String fieldName, Object restrictionValue) {
		super(fieldName);
		this.restrictionValue = restrictionValue;
	}

	/**
	 * 
	 * @return Restriction value.
	 */
	protected Object getRestrictionValue() {
		return restrictionValue;
	}

}
