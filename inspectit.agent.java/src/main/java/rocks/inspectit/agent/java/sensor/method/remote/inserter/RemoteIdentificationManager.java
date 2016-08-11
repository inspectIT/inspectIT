package rocks.inspectit.agent.java.sensor.method.remote.inserter;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * The RemoteIdentificationManager is used to generate unique IDs in the JVM.
 *
 * @author Thomas Kluge
 *
 */
@Component
public class RemoteIdentificationManager {

	/**
	 * Counter to generate unique IDs.
	 */
	private final AtomicLong identification;

	/**
	 * Default Constructor.
	 */
	public RemoteIdentificationManager() {
		identification = new AtomicLong(1);
	}

	/**
	 *
	 * @return The next unique ID.
	 */
	public long getNextIdentification() {
		return identification.getAndIncrement();
	}
}