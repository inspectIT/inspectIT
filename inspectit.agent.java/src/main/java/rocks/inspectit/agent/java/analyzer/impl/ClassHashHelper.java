package rocks.inspectit.agent.java.analyzer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.io.FileResolver;
import rocks.inspectit.agent.java.spring.PrototypesProvider;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.storage.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.storage.serializer.provider.SerializationManagerProvider;

/**
 * Implementation of the {@link IClassHashHelper} that holds all data in one concurrent map. Keys in
 * this map are class hashes, while entries are {@link ClassHashEntry} and they define answers to
 * all the provided questions.
 *
 * @author Ivan Senic
 *
 */
// we must depend on PlatformManager to make sure that initial instrumentations are in the configuration
@Component
@DependsOn("platformManager")
public class ClassHashHelper implements InitializingBean, DisposableBean {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Configuration storage to read the configuration properties.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * Core service.
	 */
	@Autowired
	private ICoreService coreService;

	/**
	 * {@link SerializationManagerProvider} for the serialization.
	 */
	@Autowired
	private PrototypesProvider prototypesProvider;

	/**
	 * {@link FileResolver}.
	 */
	@Autowired
	private FileResolver fileResolver;

	/**
	 * Serialization manager to use when storing loading from disk.
	 */
	private SerializationManager serializationManager;

	/**
	 * Map holding class hash entries.
	 * <p>
	 * Initial capacity to 4K, as we don't really expect less amount of classes in average.
	 */
	private final ConcurrentHashMap<String, ClassHashEntry> map = new ConcurrentHashMap<String, ClassHashEntry>(4096);

	/**
	 * Returns if the class with given hash has been sent to the CMR. Only hashes that are
	 * registered with {@link #registerSent(String)} are considered as sent ones.
	 *
	 * @param hash
	 *            Hash to check
	 *
	 *
	 * @return Returns if the class with given hash has been sent to the CMR.
	 */
	public boolean isSent(String hash) {
		return map.containsKey(hash);
	}

	/**
	 * Registers the class with given hash as being sent to the CMR.
	 * <p>
	 * Sets the instrumentation result for the class with the given hash. Result will be set only if
	 * its not <code>null</code> and {@link InstrumentationDefinition#isEmpty()} is false, otherwise
	 * there is no reason to cache the result.
	 *
	 * @param hash
	 *            Class hash
	 * @param instrumentationResult
	 *            Instrumentation result
	 */

	public void register(String hash, InstrumentationDefinition instrumentationResult) {
		ClassHashEntry entry = getOrCreateEntry(hash);
		if (null != instrumentationResult && !instrumentationResult.isEmpty()) {
			entry.setInstrumentationResult(instrumentationResult);
		} else {
			entry.setInstrumentationResult(null); // NOPMD
		}
	}

	/**
	 * Returns the {@link InstrumentationDefinition} for the class with given hash if the one was
	 * been set with the {@link #registerInstrumentationResult(String, InstrumentationDefinition)}.
	 *
	 * @param hash
	 *            Class hash
	 * @return {@link InstrumentationDefinition} or <code>null</code> if no result was set for given
	 *         hash.
	 */

	public InstrumentationDefinition getInstrumentationResult(String hash) {
		ClassHashEntry entry = map.get(hash);
		return entry != null ? entry.getInstrumentationResult() : null;
	}


	/**
	 * Creates new entry in the map in the atomic fashion.
	 *
	 * @param hash
	 *            key
	 * @return Created or existing entry
	 */
	private ClassHashEntry getOrCreateEntry(String hash) {
		ClassHashEntry entry = map.get(hash);
		if (null == entry) {
			entry = new ClassHashEntry();
			ClassHashEntry old = map.putIfAbsent(hash, entry);
			if (null != old) {
				entry = old;
			}
		}
		return entry;
	}

	/**
	 * Returns if no class has been cached with this helper.
	 *
	 * @return Returns if no class has been cached with this helper.
	 */
	boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 * <P>
	 * Loads the possible existing class cache from the disk if CMR reports to know classes from
	 * this agent.
	 */
	public void afterPropertiesSet() throws Exception {
		serializationManager = prototypesProvider.createSerializer();

		// only load if configuration says that the class cache exists on the CMR
		if (configurationStorage.isClassCacheExistsOnCmr()) {
			loadCacheFromDisk();
		} else {
			deleteCacheFromDisk();
		}

		// check if there are any initial instrumentation points in configuration
		Map<Collection<String>, InstrumentationDefinition> initInstrumentations = configurationStorage.getInitialInstrumentationResults();
		if (MapUtils.isNotEmpty(initInstrumentations)) {
			for (Entry<Collection<String>, InstrumentationDefinition> entry : initInstrumentations.entrySet()) {
				InstrumentationDefinition instrumentationResult = entry.getValue();
				for (String hash : entry.getKey()) {
					register(hash, instrumentationResult);
				}
			}
		}

		Runnable saveCacheToDiskRunnable = new Runnable() {
			public void run() {
				saveCacheToDisk();
			}
		};
		coreService.getExecutorService().scheduleAtFixedRate(saveCacheToDiskRunnable, 30, 300, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() throws Exception {
		// save when bean is destroyed, ensure save is always done on finishing
		saveCacheToDisk();
	}

	/**
	 * Load sent classes from disk.
	 */
	@SuppressWarnings("unchecked")
	private void loadCacheFromDisk() {
		File file = fileResolver.getClassHashCacheFile().getAbsoluteFile();

		if (file.exists()) {
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				Input input = new Input(fileInputStream);

				Collection<String> hashes = (Collection<String>) serializationManager.deserialize(input);
				for (String hash : hashes) {
					register(hash, null);
				}
			} catch (Throwable t) { // NOPMD
				log.warn("Unable to load sending classes cache from disk.", t);
			} finally {
				if (null != fileInputStream) {
					try {
						fileInputStream.close();
					} catch (IOException e) { // NOPMD //NOCHK
						// ignore
					}
				}
			}
		}
	}

	/**
	 * Deletes the current cache file from disk.
	 */
	private void deleteCacheFromDisk() {
		File file = fileResolver.getClassHashCacheFile().getAbsoluteFile();

		if (file.exists()) {
			if (!file.delete()) {
				log.warn("Unable to delete the existing class cache file: " + file.getAbsolutePath());
			}
		}

	}

	/**
	 * Save cache to disk.
	 */
	private void saveCacheToDisk() {
		File file = fileResolver.getClassHashCacheFile().getAbsoluteFile();

		if (file.exists()) {
			if (!file.delete()) {
				log.warn("Unable to delete the existing class cache file: " + file.getAbsolutePath());
			}
		} else {
			File parentDir = file.getParentFile();
			if (!parentDir.exists()) {
				if (!parentDir.mkdirs()) {
					log.warn("Unable to create needed directory for the cache file: " + file.getParentFile().getAbsolutePath());
				}
			}
		}

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			Output output = new Output(fileOutputStream);

			// save only the ones being set to the CMR
			Collection<String> hashes = new ArrayList<String>();
			for (Entry<String, ClassHashEntry> entry : map.entrySet()) {
				hashes.add(entry.getKey());
			}

			serializationManager.serialize(hashes, output);
		} catch (Throwable t) { // NOPMD
			log.warn("Unable to save sending classes cache to disk.", t);
		} finally {
			if (null != fileOutputStream) {
				try {
					fileOutputStream.close();
				} catch (IOException e) { // NOPMD //NOCHK
					// ignore
				}
			}
		}
	}


	/**
	 * Simple entry class that should hold the {@link InstrumentationDefinition} and collection of class
	 * loaders for one class hash.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class ClassHashEntry {

		/**
		 * {@link InstrumentationDefinition}. <code>null</code> if one does not exist.
		 */
		private volatile InstrumentationDefinition instrumentationResult;

		/**
		 * Gets {@link #instrumentationResult}.
		 *
		 * @return {@link #instrumentationResult}
		 */
		public InstrumentationDefinition getInstrumentationResult() {
			return instrumentationResult;
		}

		/**
		 * Sets {@link #instrumentationResult}.
		 *
		 * @param instrumentationResult
		 *            New value for {@link #instrumentationResult}
		 */
		public void setInstrumentationResult(InstrumentationDefinition instrumentationResult) {
			this.instrumentationResult = instrumentationResult;
		}

	}

}
