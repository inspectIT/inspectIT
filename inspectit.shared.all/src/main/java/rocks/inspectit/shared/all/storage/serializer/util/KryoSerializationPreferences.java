package rocks.inspectit.shared.all.storage.serializer.util;

/**
 * Class where all keys to the serialization preferences are listed.
 *
 * @author Ivan Senic
 *
 */
public final class KryoSerializationPreferences {

	/**
	 * Key that defines if the affiliation data information should be written with the
	 * {@link rocks.inspectit.shared.all.communication.data.InvocationAwareData} objects.
	 */
	public static final String WRITE_INVOCATION_AFFILIATION_DATA = "WRITE_INVOCATION_AFFILIATION_DATA";

	/**
	 * Private constructor.
	 */
	private KryoSerializationPreferences() {
	}
}
