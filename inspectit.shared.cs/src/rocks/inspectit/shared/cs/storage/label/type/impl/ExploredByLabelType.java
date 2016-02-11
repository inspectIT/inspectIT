package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Type of the label to denote the User that have explored the Storage.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
@DiscriminatorValue("EBLT")
public class ExploredByLabelType extends AbstractStorageLabelType<String> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2675258191208645291L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOnePerStorage() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValueReusable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMultiType() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueClass() {
		return String.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEditable() {
		return false;
	}

}
