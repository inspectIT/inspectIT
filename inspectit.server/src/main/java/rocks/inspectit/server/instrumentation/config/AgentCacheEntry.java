package rocks.inspectit.server.instrumentation.config;

import rocks.inspectit.server.instrumentation.classcache.ClassCache;

/**
 * Agent cache entry saved by the service.
 *
 * @author Ivan Senic
 *
 */
public class AgentCacheEntry {

	/**
	 * Id of the agent.
	 */
	private final long id;

	/**
	 * Class cache for the agent. Can not be <code>null</code>.
	 */
	private final ClassCache classCache;

	/**
	 * Configuration holder for the class cache. Can not be <code>null</code>.
	 */
	private final ConfigurationHolder configurationHolder;

	/**
	 * Default constructor.
	 *
	 * @param id
	 *            Agent id.
	 * @param classCache
	 *            Class cache to use.
	 * @param configurationHolder
	 *            {@link ConfigurationHolder}
	 */
	public AgentCacheEntry(long id, ClassCache classCache, ConfigurationHolder configurationHolder) {
		if (null == classCache) {
			throw new IllegalArgumentException("ClassCache instance can not be null.");
		}
		if (null == configurationHolder) {
			throw new IllegalArgumentException("ConfigurationHolder instance can not be null.");
		}
		this.id = id;
		this.classCache = classCache;
		this.configurationHolder = configurationHolder;
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public long getId() {
		return id;
	}

	/**
	 * Gets {@link #classCache}.
	 *
	 * @return {@link #classCache}
	 */
	public ClassCache getClassCache() {
		return classCache;
	}

	/**
	 * Gets {@link #configurationHolder}.
	 *
	 * @return {@link #configurationHolder}
	 */
	public ConfigurationHolder getConfigurationHolder() {
		return configurationHolder;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Mapped only by id.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Mapped only by id.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AgentCacheEntry other = (AgentCacheEntry) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

}