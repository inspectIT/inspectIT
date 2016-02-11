package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.cmr.model.PlatformIdent;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Bean for caching the {@link PlatformIdent} objects, so that they don't have to be loaded from the
 * database all the time.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class PlatformIdentCache {

	/**
	 * Clean set.
	 */
	private Map<Long, PlatformIdent> cleanPlatformIdents = new ConcurrentHashMap<Long, PlatformIdent>();

	/**
	 * Dirty set.
	 */
	private Map<Long, PlatformIdent> dirtyPlatformIdents = new ConcurrentHashMap<Long, PlatformIdent>();

	/**
	 * Marks platform ident dirty.
	 * 
	 * @param platformIdent
	 *            {@link PlatformIdent}.
	 */
	public void markDirty(PlatformIdent platformIdent) {
		mark(platformIdent, true);
	}

	/**
	 * Marks platform ident clean. If the marker with this {@link PlatformIdent} already exists, its
	 * {@link PlatformIdent} object will be changed with the supplied clean one.
	 * 
	 * @param platformIdent
	 *            {@link PlatformIdent}.
	 */
	public void markClean(PlatformIdent platformIdent) {
		mark(platformIdent, false);
	}

	/**
	 * Remove {@link PlatformIdent} from cache.
	 * 
	 * @param platformIdent
	 *            {@link PlatformIdent}.
	 */
	public void remove(PlatformIdent platformIdent) {
		cleanPlatformIdents.remove(platformIdent.getId());
		dirtyPlatformIdents.remove(platformIdent.getId());
	}

	/**
	 * Returns clean {@link PlatformIdent}s. This one can be transfered to the UI directly.
	 * 
	 * @return Returns clean {@link PlatformIdent}s.
	 */
	public Collection<PlatformIdent> getCleanPlatformIdents() {
		return getPlatformIdents(false);
	}

	/**
	 * Returns dirty {@link PlatformIdent}s. This one can not be transfered to the UI.
	 * 
	 * @return Returns dirty {@link PlatformIdent}s.
	 */
	public Collection<PlatformIdent> getDirtyPlatformIdents() {
		return getPlatformIdents(true);
	}

	/**
	 * @return Returns number of {@link PlatformIdent} obejcts in the cache.
	 */
	public int getSize() {
		return cleanPlatformIdents.size() + dirtyPlatformIdents.size();
	}

	/**
	 * Provides the list of clean or dirty {@link PlatformIdent}s.
	 * 
	 * @param dirty
	 *            Should idents be dirty or not.
	 * @return List of {@link PlatformIdent}s.
	 */
	private Collection<PlatformIdent> getPlatformIdents(boolean dirty) {
		if (dirty) {
			return dirtyPlatformIdents.values();
		} else {
			return cleanPlatformIdents.values();
		}
	}

	/**
	 * Marks a {@link PlatformIdent} dirty or clean.
	 * 
	 * @param platformIdent
	 *            {@link PlatformIdent} to mark.
	 * @param dirty
	 *            Is it dirty.
	 */
	private void mark(PlatformIdent platformIdent, boolean dirty) {
		cleanPlatformIdents.remove(platformIdent.getId());
		dirtyPlatformIdents.remove(platformIdent.getId());
		if (dirty) {
			dirtyPlatformIdents.put(platformIdent.getId(), platformIdent);
		} else {
			cleanPlatformIdents.put(platformIdent.getId(), platformIdent);
		}
	}

}
