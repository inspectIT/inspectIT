package info.novatec.inspectit.kryonet;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.KryoSerialization;

/**
 * Represents the local end point of a connection.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from <a
 * href="https://github.com/EsotericSoftware/kryonet">kryonet</a>. Original author is Nathan Sweet.
 * License info can be found <a
 * href="https://github.com/EsotericSoftware/kryonet/blob/master/license.txt">here</a>.
 * 
 * @author Nathan Sweet <misc@n4te.com>
 */
@SuppressWarnings("all")
// NOCHKALL
public interface EndPoint extends Runnable {
	/** Gets the serialization instance that will be used to serialize and deserialize objects. */
	public Serialization getSerialization();

	/** If the listener already exists, it is not added again. */
	public void addListener(Listener listener);

	public void removeListener(Listener listener);

	/** Continually updates this end point until {@link #stop()} is called. */
	public void run();

	/** Starts a new thread that calls {@link #run()}. */
	public void start();

	/** Closes this end point and causes {@link #run()} to return. */
	public void stop();

	/**
	 * @see Client
	 * @see Server
	 */
	public void close();

	/**
	 * @see Client#update(int)
	 * @see Server#update(int)
	 */
	public void update(int timeout) throws IOException;

	/**
	 * Returns the last thread that called {@link #update(int)} for this end point. This can be
	 * useful to detect when long running code will be run on the update thread.
	 */
	public Thread getUpdateThread();

	/**
	 * Gets the Kryo instance that will be used to serialize and deserialize objects. This is only
	 * valid if {@link KryoSerialization} is being used, which is the default.
	 */
	public Kryo getKryo();
}