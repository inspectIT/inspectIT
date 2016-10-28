package rocks.inspectit.shared.all.tracing.data;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;

/**
 * Our client span.
 *
 * @author Ivan Senic
 *
 */
public class ClientSpan extends AbstractSpan {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5415646396600896897L;

	/**
	 * Reference type.
	 */
	private ReferenceType referenceType;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceType getReferenceType() {
		return referenceType;
	}

	/**
	 * Sets {@link #ReferenceType}.
	 *
	 * @param referenceType
	 *            New value for {@link #ReferenceType}
	 */
	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCaller() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.referenceType == null) ? 0 : this.referenceType.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ClientSpan other = (ClientSpan) obj;
		if (this.referenceType != other.referenceType) {
			return false;
		}
		return true;
	}

}
