package info.novatec.inspectit.cmr.instrumentation.classcache;

import info.novatec.inspectit.instrumentation.classcache.ImmutableAnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableInterfaceType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;

import java.util.Collection;

/**
 * Interface for class cache lookup.
 * 
 * @author Ivan Senic
 * 
 */
public interface IClassCacheLookup {

	/**
	 * Finds {@link ImmutableType} by the exact fully qualified name of the type.
	 * 
	 * @param fqn
	 *            Fully qualified name of the type
	 * @return Found type or <code>null</code> if one does not exists.
	 */
	ImmutableType findByFQN(String fqn);

	/**
	 * Finds {@link ImmutableType} by the hash value of the type.
	 * 
	 * @param hash
	 *            Type hash
	 * @return Found type or <code>null</code> if one does not exists.
	 */
	ImmutableType findByHash(String hash);

	/**
	 * Returns all {@link ImmutableType}s from the FQN indexer.
	 * 
	 * @return Returns all {@link ImmutableType}s from the FQN indexer.
	 */
	Collection<? extends ImmutableType> findAll();

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
	Collection<? extends ImmutableType> findByPattern(final String fqnPattern, boolean onlyInitialized);

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
	Collection<? extends ImmutableClassType> findClassTypesByPattern(final String fqnPattern, boolean onlyInitialized);

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
	Collection<? extends ImmutableInterfaceType> findInterfaceTypesByPattern(final String fqnPattern, boolean onlyInitialized);

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
	Collection<? extends ImmutableAnnotationType> findAnnotationTypesByPattern(final String fqnPattern, boolean onlyInitialized);

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
	Collection<? extends ImmutableClassType> findExceptionTypesByPattern(final String fqnPattern, boolean onlyInitialized);

}
