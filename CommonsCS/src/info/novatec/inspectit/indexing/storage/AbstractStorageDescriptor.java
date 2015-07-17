package info.novatec.inspectit.indexing.storage;

/**
 * Abstract descriptor for handling comparing.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractStorageDescriptor implements IStorageDescriptor {

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(IStorageDescriptor other) {
		if (this.getChannelId() - other.getChannelId() != 0) {
			return this.getChannelId() - other.getChannelId();
		}
		if (this.getPosition() - other.getPosition() != 0) {
			return (int) (this.getPosition() - other.getPosition());
		}
		if (this.getSize() - other.getSize() != 0) {
			return (int) (this.getSize() - other.getSize());
		}
		return 0;
	}

}
