package info.novatec.inspectit.cmr.instrumentation.classcache;

import info.novatec.inspectit.cmr.instrumentation.classcache.index.FQNIndexer;
import info.novatec.inspectit.cmr.instrumentation.classcache.index.HashIndexer;
import info.novatec.inspectit.instrumentation.classcache.ImmutableAnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableInterfaceType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.pattern.IMatchPattern;
import info.novatec.inspectit.pattern.WildcardMatchPattern;
import info.novatec.inspectit.spring.logger.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Lookup service for the {@link ClassCache}.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ClassCacheLookup implements IClassCacheLookup {

	/**
	 * Log of the class.
	 */
	@Log
	Logger log;

	/**
	 * Indexer to help searching with the fully qualified class name and wild-cards.
	 */
	@Autowired
	private FQNIndexer<Type> fqnIndexer;

	/**
	 * Indexer for that hash code and types.
	 */
	@Autowired
	private HashIndexer hashIndexer;

	/**
	 * {@link ClassCache} lookup belongs to.
	 */
	private ClassCache classcache;

	/**
	 * Init the {@link ClassCacheLookup}.
	 *
	 * @param classcache
	 *            {@link ClassCache} it belongs to.
	 */
	public void init(ClassCache classcache) {
		this.classcache = classcache;

		classcache.registerNodeChangeListener(fqnIndexer);
		classcache.registerNodeChangeListener(hashIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableType findByFQN(final String fqn) {
		try {
			return classcache.executeWithReadLock(new Callable<ImmutableType>() {
				@Override
				public ImmutableType call() throws Exception {
					return fqnIndexer.lookup(fqn);
				}
			});
		} catch (Exception e) {
			log.warn("Unexpected exception occurred during read from the FQN indexer", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableType findByHash(final String hash) {
		try {
			return classcache.executeWithReadLock(new Callable<ImmutableType>() {
				@Override
				public ImmutableType call() throws Exception {
					return hashIndexer.lookup(hash);
				}
			});
		} catch (Exception e) {
			log.warn("Unexpected exception occurred during read from the Hash indexer", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableType> findAll() {
		try {
			return classcache.executeWithReadLock(new Callable<Collection<Type>>() {
				@Override
				public Collection<Type> call() throws Exception {
					return fqnIndexer.findAll();
				}
			});
		} catch (Exception e) {
			log.warn("Unexpected exception occurred during read from the FQN indexer", e);
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableType> findByPattern(final String fqnPattern, boolean onlyInitialized) {
		try {
			if (WildcardMatchPattern.isPattern(fqnPattern)) {
				// if it s wild card search with starts with
				final String startsWithCriteria = fqnPattern.substring(0, fqnPattern.indexOf('*'));
				Collection<? extends ImmutableType> results = classcache.executeWithReadLock(new Callable<Collection<Type>>() {
					@Override
					public Collection<Type> call() throws Exception {
						return fqnIndexer.findStartsWith(startsWithCriteria);
					}
				});

				// then filter by whole pattern
				if (CollectionUtils.isNotEmpty(results)) {
					IMatchPattern matchPattern = new WildcardMatchPattern(fqnPattern);
					for (Iterator<? extends ImmutableType> it = results.iterator(); it.hasNext();) {
						ImmutableType immutableType = it.next();
						if (onlyInitialized && !immutableType.isInitialized()) {
							it.remove();
						} else if (!matchPattern.match(immutableType.getFQN())) {
							it.remove();
						}
					}
				}
				return results;
			} else {
				// if it s not a wild card pattern just search for directly
				Type type = (Type) classcache.executeWithReadLock(new Callable<ImmutableType>() {
					@Override
					public ImmutableType call() throws Exception {
						return fqnIndexer.lookup(fqnPattern);
					}
				});
				if (null != type && !(onlyInitialized && !type.isInitialized())) {
					Collection<ImmutableType> results = new ArrayList<>();
					results.add(type);
					return results;
				} else {
					return Collections.emptyList();
				}
			}
		} catch (Exception e) {
			log.warn("Unexpected exception occurred during read from the FQN indexer", e);
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableClassType> findClassTypesByPattern(final String fqnPattern, boolean onlyInitialized) {
		// first search for all
		Collection<? extends ImmutableType> results = findByPattern(fqnPattern, onlyInitialized);

		// if empty return
		if (CollectionUtils.isEmpty(results)) {
			return Collections.emptyList();
		}

		// otherwise filter only for classes
		Collection<ImmutableClassType> classTypes = new ArrayList<>();
		for (ImmutableType immutableType : results) {
			if (immutableType.isClass()) {
				classTypes.add(immutableType.castToClass());
			}
		}
		return classTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableInterfaceType> findInterfaceTypesByPattern(final String fqnPattern, boolean onlyInitialized) {
		// first search for all
		Collection<? extends ImmutableType> results = findByPattern(fqnPattern, onlyInitialized);

		// if empty return
		if (CollectionUtils.isEmpty(results)) {
			return Collections.emptyList();
		}

		// otherwise filter only for classes
		Collection<ImmutableInterfaceType> interfaceTypes = new ArrayList<>();
		for (ImmutableType immutableType : results) {
			if (immutableType.isInterface()) {
				interfaceTypes.add(immutableType.castToInterface());
			}
		}
		return interfaceTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableAnnotationType> findAnnotationTypesByPattern(final String fqnPattern, boolean onlyInitialized) {
		// first search for all
		Collection<? extends ImmutableType> results = findByPattern(fqnPattern, onlyInitialized);

		// if empty return
		if (CollectionUtils.isEmpty(results)) {
			return Collections.emptyList();
		}

		// otherwise filter only for classes
		Collection<ImmutableAnnotationType> annotationTypes = new ArrayList<>();
		for (ImmutableType immutableType : results) {
			if (immutableType.isAnnotation()) {
				annotationTypes.add(immutableType.castToAnnotation());
			}
		}
		return annotationTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableClassType> findExceptionTypesByPattern(final String fqnPattern, boolean onlyInitialized) {
		// first search for all
		Collection<? extends ImmutableType> results = findByPattern(fqnPattern, onlyInitialized);

		// if empty return
		if (CollectionUtils.isEmpty(results)) {
			return Collections.emptyList();
		}

		// otherwise filter only for classes
		Collection<ImmutableClassType> exceptionTypes = new ArrayList<>();
		for (ImmutableType immutableType : results) {
			if (immutableType.isClass()) {
				ImmutableClassType immutableClassType = immutableType.castToClass();
				if (immutableClassType.isException()) {
					exceptionTypes.add(immutableClassType);
				}
			}
		}
		return exceptionTypes;
	}
}
