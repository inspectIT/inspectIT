package rocks.inspectit.server.instrumentation.classcache;

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

import rocks.inspectit.server.instrumentation.classcache.index.FqnIndexer;
import rocks.inspectit.server.instrumentation.classcache.index.HashIndexer;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableAnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableInterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.PatternFactory;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Lookup service for the {@link ClassCache}.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ClassCacheLookup {

	/**
	 * Log of the class.
	 */
	@Log
	Logger log;

	/**
	 * Indexer to help searching with the fully qualified class name and wild-cards.
	 */
	@Autowired
	private FqnIndexer<Type> fqnIndexer;

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
	 * Finds {@link ImmutableType} by the exact fully qualified name of the type.
	 *
	 * @param fqn
	 *            Fully qualified name of the type
	 * @return Found type or <code>null</code> if one does not exists.
	 */
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
	 * Finds {@link ImmutableType} by the hash value of the type.
	 *
	 * @param hash
	 *            Type hash
	 * @return Found type or <code>null</code> if one does not exists.
	 */
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
	 * Returns all {@link ImmutableType}s from the FQN indexer.
	 *
	 * @return Returns all {@link ImmutableType}s from the FQN indexer.
	 */
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
	 * Returns all {@link ImmutableType}s from the FQN indexer that apply for the given pattern.
	 * Pattern can be with wild cards.
	 *
	 * @param fqnPattern
	 *            FQN pattern that can be complete FQN or pattern with wild cards (*).
	 * @param onlyInitialized
	 *            Include only initialized types.
	 * @return Returns all {@link ImmutableType}s from the FQN indexer that apply for the given
	 *         pattern. Patter can be with wild cards.
	 */
	public Collection<? extends ImmutableType> findByPattern(final String fqnPattern, boolean onlyInitialized) {
		try {
			final IMatchPattern pattern = PatternFactory.getPattern(fqnPattern);
			Collection<? extends ImmutableType> results = classcache.executeWithReadLock(new Callable<Collection<Type>>() {
				@Override
				public Collection<Type> call() throws Exception {
					return fqnIndexer.findByPattern(pattern);
				}
			});

			for (Iterator<? extends ImmutableType> it = results.iterator(); it.hasNext();) {
				ImmutableType immutableType = it.next();
				if (onlyInitialized && !immutableType.isInitialized()) {
					it.remove();
				}
			}

			return results;
		} catch (Exception e) {
			log.warn("Unexpected exception occurred during read from the FQN indexer", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Returns all {@link ImmutableClassType} from the FQN indexer that apply for the given pattern.
	 * Pattern can be with wild cards.
	 *
	 * @param fqnPattern
	 *            FQN pattern that can be complete FQN or pattern with wild cards (*).
	 * @param onlyInitialized
	 *            Include only initialized types.
	 * @return Returns all {@link ImmutableClassType}s from the FQN indexer that apply for the given
	 *         pattern. Patter can be with wild cards.
	 */
	public Collection<? extends ImmutableClassType> findClassTypesByPattern(String fqnPattern, boolean onlyInitialized) {
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
	 * Returns all {@link ImmutableInterfaceType} from the FQN indexer that apply for the given
	 * pattern. Pattern can be with wild cards.
	 *
	 * @param fqnPattern
	 *            FQN pattern that can be complete FQN or pattern with wild cards (*).
	 * @param onlyInitialized
	 *            Include only initialized types.
	 * @return Returns all {@link ImmutableInterfaceType}s from the FQN indexer that apply for the
	 *         given pattern. Patter can be with wild cards.
	 */
	public Collection<? extends ImmutableInterfaceType> findInterfaceTypesByPattern(String fqnPattern, boolean onlyInitialized) {
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
	 * Returns all {@link ImmutableAnnotationType} from the FQN indexer that apply for the given
	 * pattern. Pattern can be with wild cards.
	 *
	 * @param fqnPattern
	 *            FQN pattern that can be complete FQN or pattern with wild cards (*).
	 * @param onlyInitialized
	 *            Include only initialized types.
	 * @return Returns all {@link ImmutableAnnotationType}s from the FQN indexer that apply for the
	 *         given pattern. Patter can be with wild cards.
	 */
	public Collection<? extends ImmutableAnnotationType> findAnnotationTypesByPattern(String fqnPattern, boolean onlyInitialized) {
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
	 * Returns all {@link ImmutableClassType} that are exceptions from the FQN indexer and that
	 * apply for the given pattern. Pattern can be with wild cards.
	 *
	 * @param fqnPattern
	 *            FQN pattern that can be complete FQN or pattern with wild cards (*).
	 * @param onlyInitialized
	 *            Include only initialized types.
	 * @return Returns all {@link ImmutableClassType}s that are exceptions from the FQN indexer and
	 *         that apply for the given pattern. Patter can be with wild cards.
	 */
	public Collection<? extends ImmutableClassType> findExceptionTypesByPattern(String fqnPattern, boolean onlyInitialized) {
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
