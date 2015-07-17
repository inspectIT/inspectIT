package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Thread that invokes the {@link IBuffer#indexNext()} method constantly.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class BufferIndexer extends BufferWorker {

	/**
	 * Default constructor. Just calls super class constructor.
	 * 
	 * @param buffer
	 *            Buffer to work on.
	 */
	@Autowired
	public BufferIndexer(IBuffer<?> buffer) {
		super(buffer, "buffer-indexing-thread");
		setPriority(NORM_PRIORITY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void work() throws InterruptedException {
		getBuffer().indexNext();
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
