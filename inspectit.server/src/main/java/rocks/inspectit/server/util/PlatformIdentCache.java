package rocks.inspectit.server.util;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

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
	private Map<Long, PlatformIdent> cleanPlatformIdents = new ConcurrentHashMap<>();


	/**
	 * Marks platform ident dirty if the one with given ID is known to the cache.
	 *
	 * @param platformIdentId
	 *            {@link PlatformIdent} id.
	 */
	public void markDirty(long platformIdentId) {
		cleanPlatformIdents.remove(platformIdentId);
	}

	/**
	 * Marks platform ident dirty.
	 *
	 * @param platformIdent
	 *            {@link PlatformIdent}.
	 */
	public void markDirty(PlatformIdent platformIdent) {
		markDirty(platformIdent.getId());
	}

	/**
	 * Marks platform ident clean. If the marker with this {@link PlatformIdent} already exists, its
	 * {@link PlatformIdent} object will be changed with the supplied clean one.
	 *
	 * @param platformIdent
	 *            {@link PlatformIdent}.
	 */
	public void markClean(PlatformIdent platformIdent) {
		cleanPlatformIdents.put(platformIdent.getId(), platformIdent);
	}

	/**
	 * Remove {@link PlatformIdent} from cache.
	 *
	 * @param platformIdent
	 *            {@link PlatformIdent}.
	 */
	public void remove(PlatformIdent platformIdent) {
		cleanPlatformIdents.remove(platformIdent.getId());
	}

	/**
	 * Returns clean {@link PlatformIdent}s. This one can be transfered to the UI directly.
	 *
	 * @return Returns clean {@link PlatformIdent}s.
	 */
	public Collection<PlatformIdent> getCleanPlatformIdents() {
		return cleanPlatformIdents.values();
	}

}
