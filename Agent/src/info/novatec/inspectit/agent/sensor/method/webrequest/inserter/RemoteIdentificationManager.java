package info.novatec.inspectit.agent.sensor.method.webrequest.inserter;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * The RemoteIdentificationManager is used to generate unique IDs in the JVM.
 * 
 * @author Thomas Kluge
 *
 */
@Component
public class RemoteIdentificationManager implements InitializingBean, DisposableBean {

	/**
	 * Counter to generate unique IDs.
	 */
	private AtomicLong identification;

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
		return identification.incrementAndGet();
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() throws Exception {

	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
	}
}