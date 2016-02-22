package rocks.inspectit.server.cache.impl;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.cache.IBuffer;

/**
 * Thread that invokes the {@link IBuffer#analyzeNext()} method constantly.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class BufferAnalyzer extends BufferWorker {

	/**
	 * Default constructor. Just calls super class constructor.
	 * 
	 * @param buffer
	 *            Buffer to work on.
	 */
	@Autowired
	public BufferAnalyzer(IBuffer<?> buffer) {
		super(buffer, "buffer-analyzing-thread");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void work() throws InterruptedException {
		getBuffer().analyzeNext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PostConstruct
	public synchronized void start() {
		super.start();
	}

}
