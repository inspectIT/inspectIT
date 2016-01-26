package rocks.inspectit.agent.java.analyzer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
 * this map are class FQNs, while entries are {@link ClassEntry}s and they define answers to all the
 * provided questions.
 *
 * @author Ivan Senic
 *
 */
// we must depend on PlatformManager to make sure that initial instrumentations are in the
// configuration
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
	 * Map holding class entries. Key is FQN of the class.
	 * <p>
	 * Initial capacity to 4K, as we don't really expect less amount of classes in average.
	 */
	private final ConcurrentHashMap<String, ClassEntry> fqnToClassEntryMap = new ConcurrentHashMap<String, ClassEntry>(4096);

	/**
	 * Registers that the given class was analyzed.
	 *
	 * @param fqn
	 *            Class fqn
	 *
	 */
	public void registerAnalyzed(String fqn) {
		getOrCreateEntry(fqn);
	}

	/**
	 * Returns if the class with given FQN was analyzed.
	 *
	 * @param fqn
	 *            Class fqn
	 *
	 * @return Returns if the class with given FQN was analyzed.
	 */
	public boolean isAnalyzed(String fqn) {
		ClassEntry entry = fqnToClassEntryMap.get(fqn);
		return entry != null ? true : false;
	}

	/**
	 * Registers the class with given fqn/hash as being sent to the CMR.
	 *
	 * @param fqn
	 *            Class fully qualified name.
	 * @param hash
	 *            Class hash
	 */
	public void registerSent(String fqn, String hash) {
		ClassEntry entry = getOrCreateEntry(fqn);
		entry.addHash(hash);
	}

	/**
	 * Returns if the class with given fqn and hash has been sent to the CMR. Only hashes that are
	 * registered with {@link #registerSent(String)} are considered as sent ones.
	 *
	 * @param fqn
	 *            Class fully qualified name.
	 * @param hash
	 *            Hash to check
	 *
	 *
	 * @return Returns if the class with given hash has been sent to the CMR.
	 */
	public boolean isSent(String fqn, String hash) {
		ClassEntry entry = fqnToClassEntryMap.get(fqn);
		return entry != null ? entry.containsHash(hash) : false;
	}

	/**
	 * Registers the instrumentation result for the class with the given FQn.
	 *
	 * @param fqn
	 *            Class fully qualified name.
	 * @param instrumentationResult
	 *            {@link InstrumentationDefinition}
	 */
	public void registerInstrumentationDefinition(String fqn, InstrumentationDefinition instrumentationResult) {
		ClassEntry entry = getOrCreateEntry(fqn);
		if (null != instrumentationResult && !instrumentationResult.isEmpty()) {
			entry.setInstrumentationResult(instrumentationResult);
		} else {
			entry.setInstrumentationResult(null); // NOPMD
		}
	}


	/**
	 * Returns the {@link InstrumentationDefinition} for the class with given FQN if the one was
	 * been set with the
	 * {@link #registerInstrumentationDefinition(String, InstrumentationDefinition)}.
	 *
	 * @param fqn
	 *            Class fqn
	 * @return {@link InstrumentationDefinition} or <code>null</code> if no result was set for given
	 *         hash.
	 */
	public InstrumentationDefinition getInstrumentationDefinition(String fqn) {
		ClassEntry entry = fqnToClassEntryMap.get(fqn);
		return entry != null ? entry.getInstrumentationResult() : null;
	}

	/**
	 * Creates new entry in the map in the atomic fashion.
	 *
	 * @param fqn
	 *            key
	 * @return Created or existing entry
	 */
	private ClassEntry getOrCreateEntry(String fqn) {
		ClassEntry entry = fqnToClassEntryMap.get(fqn);
		if (null == entry) {
			entry = new ClassEntry();
			ClassEntry old = fqnToClassEntryMap.putIfAbsent(fqn, entry);
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
		return fqnToClassEntryMap.isEmpty();
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
				String fqn = instrumentationResult.getClassName();
				registerInstrumentationDefinition(fqn, instrumentationResult);
				for (String hash : entry.getKey()) {
					registerSent(fqn, hash);
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

		fqnToClassEntryMap.clear();
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

				Map<String, Collection<String>> fqnWithHashes = (Map<String, Collection<String>>) serializationManager.deserialize(input);
				for (Entry<String, Collection<String>> entry : fqnWithHashes.entrySet()) {
					ClassEntry classEntry = getOrCreateEntry(entry.getKey());
					for (String hash : entry.getValue()) {
						classEntry.addHash(hash);
					}
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
			Map<String, Collection<String>> fqnWithHashes = new HashMap<String, Collection<String>>();
			for (Entry<String, ClassEntry> entry : fqnToClassEntryMap.entrySet()) {
				fqnWithHashes.put(entry.getKey(), entry.getValue().getHashes());
			}

			serializationManager.serialize(fqnWithHashes, output);
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
	 * Simple entry class that should hold the {@link InstrumentationDefinition} and collection of
	 * class loaders for one class hash.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class ClassEntry {

		/**
		 * {@link InstrumentationDefinition}. <code>null</code> if one does not exist.
		 */
		private volatile InstrumentationDefinition instrumentationResult;

		/**
		 * Known hashes for this class.
		 */
		private final CopyOnWriteArrayList<String> hashes = new CopyOnWriteArrayList<String>();

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

		/**
		 * Adds hash to the {@link #hashes} if it does not exist.
		 *
		 * @param hash
		 *            of the class
		 */
		public void addHash(String hash) {
			if (null != hash) {
				hashes.addIfAbsent(hash);
			}
		}

		/**
		 * Returns if the hash is contained in the {@link #hashes}.
		 *
		 * @param hash
		 *            of the class
		 * @return Returns if the hash is contained in the {@link #hashes}.
		 */
		public boolean containsHash(String hash) {
			if (null != hash) {
				return hashes.contains(hash);
			}
			return false;
		}

		/**
		 * Gets {@link #hashes}.
		 *
		 * @return {@link #hashes}
		 */
		public Collection<String> getHashes() {
			return Collections.unmodifiableList(hashes);
		}

	}

}
