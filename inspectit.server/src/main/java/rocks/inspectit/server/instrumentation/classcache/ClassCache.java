package rocks.inspectit.server.instrumentation.classcache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.instrumentation.classcache.events.INodeChangeListener;
import rocks.inspectit.server.instrumentation.classcache.events.NodeEvent;
import rocks.inspectit.server.instrumentation.classcache.events.ReferenceEvent;

/**
 * The <code>ClassCache</code> holds the server-side representation of class structures. Each class
 * cache comes with its modification service and lookup service.
 *
 * <b> Only one write at a time. </b><br />
 * The <code>ClassCache</code> ensures that only one writer can be active at one given time.
 *
 * <b> Multiple queries. </b> <br />
 * The lookup facility supports parallel reads. Also the returned model elements allow to be read by
 * multiple threads.
 *
 * <b> "Simulated" safety by hiding. </b> <br />
 * The <code>ClassCache</code> uses package access methods in order to ensure that only the core
 * classes within the class cache can access modification methods that would leak live instances. We
 * know that this is not the safest approach but as the structure is only used internally, it seems
 * to us to be a good alternative.
 *
 * @author Stefan Siegl
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ClassCache {

	/**
	 * The modification service.
	 */
	@Autowired
	private ClassCacheModification modificationService;

	/**
	 * The lookup service.
	 */
	@Autowired
	private ClassCacheLookup lookupService;

	/**
	 * The instrumentation service.
	 */
	@Autowired
	private ClassCacheInstrumentation instrumentationService;

	/**
	 * List of listeners that are informed about changes to the model elements of the class cache.
	 * Most likely indexing structures register themselves here.
	 */
	private final List<INodeChangeListener> nodeChangeListeners = new ArrayList<>();

	/**
	 * Lock of the structure.
	 */
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	/**
	 * Read lock of the structure.
	 */
	private final Lock readLock = rwl.readLock();

	/**
	 * Write lock of the structure.
	 */
	private final Lock writeLock = rwl.writeLock();

	/**
	 * Initializes the services to the class cache. It is absolutely necessary to call this method
	 * before using the class cache.
	 *
	 * As the services need a reference to the class cache instance building of the services cannot
	 * happen within the constructor as we could then leak the "this" reference prior to having the
	 * class cache built up completely.
	 */
	@PostConstruct
	void init() {
		lookupService.init(this);
		instrumentationService.init(this);
		modificationService.init(this);
	}

	/**
	 * Executes given {@link Callable} with the read lock of the class cache. Result of the
	 * {@link Callable} operation will be return. If exception occurs during call exception will be
	 * propagated.
	 * <p>
	 * Note that this is a synchronous operation.
	 *
	 * @param <T>
	 *            type of result
	 * @param callable
	 *            {@link Callable} to call.
	 * @return Result of {@link Callable} call.
	 * @throws Exception
	 *             If {@link Exception} occurs during call method.
	 */
	public <T> T executeWithReadLock(Callable<T> callable) throws Exception {
		readLock.lock();
		try {
			return callable.call();
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Executes given {@link Callable} with the write lock of the class cache. Result of the
	 * {@link Callable} operation will be return. If exception occurs during call exception will be
	 * propagated.
	 * <p>
	 * Note that this is synchronous operation.
	 *
	 * @param <T>
	 *            type of result
	 * @param callable
	 *            {@link Callable} to call.
	 * @return Result of {@link Callable} call.
	 * @throws Exception
	 *             If {@link Exception} occurs during call method.
	 */
	public <T> T executeWithWriteLock(Callable<T> callable) throws Exception {
		writeLock.lock();
		try {
			return callable.call();
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Inform the registered listeners about changes to the class node structure.
	 *
	 * Note that this method is synchronized. As it is ensured that only one thread can actively
	 * change the structure this synchronization does not really hinder us. But is provides an
	 * additional level of safety as the indexers can assume that they are only called single
	 * threaded for sure.
	 *
	 * @param e
	 *            the changes to the structure.
	 */
	void informNodeChange(NodeEvent e) {
		for (INodeChangeListener listener : nodeChangeListeners) {
			listener.informNodeChange(e);
		}
	}

	/**
	 * Inform the registered listeners about changes to the class reference structure.
	 *
	 * Note that this method is synchronized. As it is ensured that only one thread can actively
	 * change the structure this synchronization does not really hinder us. But is provides an
	 * additional level of safety as the indexers can assume that they are only called single
	 * threaded for sure.
	 *
	 * @param e
	 *            the changes to the structure.
	 */
	void informReferenceChange(ReferenceEvent e) {
		for (INodeChangeListener listener : nodeChangeListeners) {
			listener.informReferenceChange(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void registerNodeChangeListener(INodeChangeListener listener) {
		nodeChangeListeners.add(listener);
	}

	/**
	 * Returns the modification service. The modification service provides a set of services that
	 * allow to change the class cache in a safe way.
	 *
	 * @return the modification service
	 */
	public ClassCacheModification getModificationService() {
		return modificationService;
	}

	/**
	 * Returns the lookup service, which allows to search for specific entries within the class
	 * cache.
	 *
	 * @return the lookup service, which allows to search for specific entries within the class
	 *         cache.
	 */
	public ClassCacheLookup getLookupService() {
		return lookupService;
	}

	/**
	 * Returns the instrumentation service, which allows to perform instrumentation operations
	 * within the class cache.
	 *
	 * @return the instrumentation service, which allows to perform instrumentation operations
	 *         within the class cache.
	 */
	public ClassCacheInstrumentation getInstrumentationService() {
		return instrumentationService;
	}

}
