package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassHashHelper;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.io.FileResolver;
import info.novatec.inspectit.agent.spring.PrototypesProvider;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationDefinition;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.serializer.impl.SerializationManager;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Objects;

/**
 * Implementation of the {@link IClassHashHelper} that holds all data in one concurrent map. Keys in
 * this map are class hashes, while entries are {@link ClassHashEntry} and they define answers to
 * all the provided questions.
 *
 * @author Ivan Senic
 *
 */
// we must depend on IdManager to make sure that initial instrumentations are in the configuration
@Component
@DependsOn("idManager")
public class ClassHashHelper implements IClassHashHelper, InitializingBean, DisposableBean {

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
	 * Atomic reference holding the array of class loaders each packed in the {@link WeakReference}.
	 * <p>
	 * Must be weak reference as we don't want to influence the garbage collection of the class
	 * loaders.
	 */
	@SuppressWarnings("unchecked")
	private final AtomicReference<WeakReference<ClassLoader>[]> loaderWeakArrayReference = new AtomicReference<WeakReference<ClassLoader>[]>(new WeakReference[0]);

	/**
	 * {@inheritDoc}
	 */
	public boolean isSent(String hash) {
		return map.containsKey(hash);
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	public InstrumentationDefinition getInstrumentationResult(String hash) {
		ClassHashEntry entry = map.get(hash);
		return entry != null ? entry.getInstrumentationResult() : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerLoaded(String hash, ClassLoader classLoader) {
		ClassHashEntry entry = getOrCreateEntry(hash);
		WeakReference<ClassLoader> reference = getOrCreateClassLoaderWeakReference(classLoader);
		entry.addClassLoaderReference(reference);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<ClassLoader> getClassLoaders(String hash) {
		ClassHashEntry entry = map.get(hash);
		if (null == entry) {
			return Collections.emptyList();
		}

		WeakReference<ClassLoader>[] classLoaderReferences = entry.getClassLoaderReferences();
		if (ArrayUtils.isEmpty(classLoaderReferences)) {
			return Collections.emptyList();
		}

		List<ClassLoader> classLoaders = new ArrayList<ClassLoader>(classLoaderReferences.length);
		for (WeakReference<ClassLoader> reference : classLoaderReferences) {
			ClassLoader loader = reference.get();
			CollectionUtils.addIgnoreNull(classLoaders, loader);
		}
		return classLoaders;
	}

	/**
	 * Creates a new WeakReference for the class loader if one is not yet existing in the
	 * {@link #loaderWeakArrayReference}. If one exists, they existing reference is returned.
	 *
	 * @param classLoader
	 *            ClassLoader to consider.
	 * @return WeakReference for given class loader that can be used in the {@link ClassHashEntry}.
	 */
	@SuppressWarnings("unchecked")
	private WeakReference<ClassLoader> getOrCreateClassLoaderWeakReference(ClassLoader classLoader) {
		while (true) {
			// load current array
			WeakReference<ClassLoader>[] referenceArray = loaderWeakArrayReference.get();
			int size = referenceArray.length;

			// try to locate existing one
			for (int i = 0; i < size; i++) {
				WeakReference<ClassLoader> reference = referenceArray[i];
				ClassLoader existing = reference.get();
				if (Objects.equal(existing, classLoader)) {
					return reference;
				}
			}

			// if not existing create new array and add
			WeakReference<ClassLoader> newReference = new WeakReference<ClassLoader>(classLoader);
			WeakReference<ClassLoader>[] newReferenceArray = new WeakReference[size + 1];
			System.arraycopy(referenceArray, 0, newReferenceArray, 0, size);
			newReferenceArray[size] = newReference;

			// then update the atomic reference with CAS operation
			if (loaderWeakArrayReference.compareAndSet(referenceArray, newReferenceArray)) {
				return newReference;
			}
		}
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
		 *
		 */
		private volatile WeakReference<ClassLoader>[] classLoaderReferences;

		/**
		 * @param reference
		 *            Reference to add
		 */
		@SuppressWarnings("unchecked")
		public void addClassLoaderReference(WeakReference<ClassLoader> reference) {
			synchronized (this) {
				if (!ArrayUtils.contains(classLoaderReferences, reference)) {
					classLoaderReferences = (WeakReference<ClassLoader>[]) ArrayUtils.add(classLoaderReferences, reference);
				}
			}
		}

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
		 * Gets {@link #classLoaderReferences}.
		 *
		 * @return {@link #classLoaderReferences}
		 */
		public WeakReference<ClassLoader>[] getClassLoaderReferences() {
			return classLoaderReferences;
		}

	}

}
