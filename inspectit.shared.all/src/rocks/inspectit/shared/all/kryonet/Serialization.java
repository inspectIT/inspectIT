package rocks.inspectit.shared.all.kryonet;

import java.nio.ByteBuffer;

/**
 * Controls how objects are transmitted over the network.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from <a
 * href="https://github.com/EsotericSoftware/kryonet">kryonet</a>. Original author is Nathan Sweet.
 * License info can be found <a
 * href="https://github.com/EsotericSoftware/kryonet/blob/master/license.txt">here</a>.
 * */
@SuppressWarnings("all")
// NOCHKALL
public interface Serialization {
	/**
	 * @param connection
	 *            May be null.
	 */
	public void write(Connection connection, ByteBuffer buffer, Object object);

	public Object read(Connection connection, ByteBuffer buffer);

	/**
	 * The fixed number of bytes that will be written by {@link #writeLength(ByteBuffer, int)} and
	 * read by {@link #readLength(ByteBuffer)}.
	 */
	public int getLengthLength();

	public void writeLength(ByteBuffer buffer, int length);

	public int readLength(ByteBuffer buffer);

}
